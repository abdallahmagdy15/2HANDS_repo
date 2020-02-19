package com.example.a2hands.homePackage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a2hands.HelpRequest;
import com.example.a2hands.Notification;
import com.example.a2hands.Post;
import com.example.a2hands.ProfileActivity;
import com.example.a2hands.Rating;
import com.example.a2hands.User;
import com.example.a2hands.homePackage.PostFragment.OnListFragmentInteractionListener;
import com.example.a2hands.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.net.UnknownServiceException;
import java.util.List;
import org.ocpsoft.prettytime.PrettyTime;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostRecyclerViewAdapter extends RecyclerView.Adapter<MyPostRecyclerViewAdapter.ViewHolder> {

    private final List<Post> postsList;
    private final OnListFragmentInteractionListener mListener;
    private  Context context;

    public MyPostRecyclerViewAdapter(List<Post> posts, OnListFragmentInteractionListener listener) {
        postsList = posts;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_post, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView postOwner;
        public final TextView postContent;
        public final TextView time;
        public final TextView location;
        public final TextView category;
        public final CircleImageView postOwnerPic;
        public final Button helpBtn;
        public final TextView postUserId;
        public final Button ratingsBtn;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            time=view.findViewById(R.id.postTime);
            location =view.findViewById(R.id.postLocation);
            postOwner = view.findViewById(R.id.postOwner);
            postContent =  view.findViewById(R.id.content);
            category = view.findViewById(R.id.postCategory);
            postOwnerPic = view.findViewById(R.id.postOwnerPic);
            postUserId = view.findViewById(R.id.postUserId);
            ratingsBtn = view.findViewById(R.id.ratingsBtn);
            helpBtn = view.findViewById(R.id.helpBtn);
        }


    }
    @Override
    public void onBindViewHolder(final ViewHolder holder,final int position) {
        holder.postContent.setText(postsList.get(position).content_text);
        PrettyTime p = new PrettyTime();
        holder.time.setText(p.format(postsList.get(position).date));
        holder.location.setText(postsList.get(position).location);
        String cat = postsList.get(position).category;
        holder.category.setText((!cat.equals("General"))?cat:"");

        if(!postsList.get(position).visibility){
            holder.postOwner.setText("Anonymous");
            holder.postOwnerPic.setImageResource(R.drawable.ic_person_black_24dp);
        }
        else {
            holder.postOwner.setText(postsList.get(position).postOwner);
            holder.postOwnerPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(v.getContext(), ProfileActivity.class);
                    i.putExtra("uid", postsList.get(position).user_id);
                    v.getContext().startActivity(i);
                }
            });
            FirebaseStorage.getInstance().getReference()
                    .child("Profile_Pics/" +postsList.get(position).user_id+ "/"
                            + postsList.get(position).profile_pic)
                    .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri.toString()).into(holder.postOwnerPic);
                }
            });
        }

        holder.ratingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context,RatingsActivity.class);
                i.putExtra("postId",postsList.get(position).post_id);
                context.startActivity(i);
            }
        });

        //set listener for help button in any post
        holder.helpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if post owner clicks on help button
                final String uid = FirebaseAuth.getInstance().getUid();
                if (uid.equals(postsList.get(position).user_id))
                    Toast.makeText(context, "Can't send help request to yourself!", Toast.LENGTH_LONG).show();
                else {
                    //fill object of help request with data
                    final HelpRequest helpReq = new HelpRequest();
                    helpReq.post_id = postsList.get(position).post_id;
                    helpReq.publisher_id = uid;
                    helpReq.subscriber_id = postsList.get(position).user_id;
                    //show confirmation dialog if u want to send help request
                    new AlertDialog.Builder(context)
                            .setTitle("Send Help Request to " +
                                    ((postsList.get(position).visibility) ? postsList.get(position).postOwner : "Anonymous") + "?")
                            .setPositiveButton("Send", new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //save help request to firestore
                                    FirebaseFirestore.getInstance()
                                            .collection("help_requests")
                                            .add(helpReq).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull final Task<DocumentReference> helpReqTask) {
                                            Toast.makeText(context, "Help Request Sent !", Toast.LENGTH_SHORT).show();
                                            //get name of current logged in user
                                            FirebaseFirestore.getInstance()
                                                    .collection("users").document(helpReq.publisher_id)
                                                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    ///////send notification to the subscriber(who notifi is sent to)
                                                    //fill notification obj with data from helpReq obj
                                                    final Notification notifi = new Notification();
                                                    User user = task.getResult().toObject(User.class);
                                                    String userName = user.first_name + " " + user.last_name;
                                                    notifi.publisher_name = userName;
                                                    notifi.content = userName + " sent you a help request";
                                                    notifi.subscriber_id = helpReq.subscriber_id;
                                                    notifi.publisher_id = helpReq.publisher_id;
                                                    notifi.publisher_pic = user.profile_pic;
                                                    notifi.type = "HELP_REQUEST";
                                                    notifi.help_request_id = helpReqTask.getResult().getId();
                                                    notifi.post_id = postsList.get(position).post_id;
                                                    /////save notifi obj to realtime
                                                    //push empty record and get its key
                                                    DatabaseReference ref = FirebaseDatabase.getInstance()
                                                            .getReference("notifications");
                                                    notifi.notification_id = ref.push().getKey();
                                                    //push the notifi obj to this id
                                                    ref.child(notifi.notification_id).setValue(notifi);

                                                }
                                            });

                                        }
                                    });
                                }
                            })
                            .setNegativeButton("Cancel", null).show();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return postsList.size();
    }
}
