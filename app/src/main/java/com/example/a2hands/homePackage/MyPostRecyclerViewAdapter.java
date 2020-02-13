package com.example.a2hands.homePackage;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.a2hands.Post;
import com.example.a2hands.ProfileActivity;
import com.example.a2hands.User;
import com.example.a2hands.homePackage.PostFragment.OnListFragmentInteractionListener;
import com.example.a2hands.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.List;
import org.ocpsoft.prettytime.PrettyTime;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostRecyclerViewAdapter extends RecyclerView.Adapter<MyPostRecyclerViewAdapter.ViewHolder> {

    private final List<Post> postsList;
    private final OnListFragmentInteractionListener mListener;

    public MyPostRecyclerViewAdapter(List<Post> posts, OnListFragmentInteractionListener listener) {
        postsList = posts;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_post, parent, false);
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
        public Post post;
        public final TextView postUserId;

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
        }

        @Override
        public String toString() {
            return super.toString() + " '" + postContent.getText() + "'";
        }
    }
    @Override
    public void onBindViewHolder(final ViewHolder holder,final int position) {
        holder.post = postsList.get(position);
        holder.postContent.setText(postsList.get(position).content_text);
        PrettyTime p = new PrettyTime();
        holder.time.setText(p.format(postsList.get(position).date));
        holder.location.setText(postsList.get(position).location);
        String cat = postsList.get(position).category;
        holder.category.setText((!cat.equals("General"))?cat:"");
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    //mListener.onListFragmentInteraction();
                }
            }
        });

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
            FirebaseFirestore.getInstance().collection("/users/")
                    .document(postsList.get(position).user_id).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot doc = task.getResult();
                                User user = doc.toObject(User.class);
                                FirebaseStorage.getInstance().getReference().child("Profile_Pics/" + user.profile_pic)
                                        .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Picasso.get().load(uri.toString()).into(holder.postOwnerPic);
                                    }
                                });
                            }
                        }
                    });
        }

    }

    @Override
    public int getItemCount() {
        return postsList.size();
    }


}
