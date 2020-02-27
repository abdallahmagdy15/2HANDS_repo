package com.example.a2hands.homePackage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.a2hands.Callback;
import com.example.a2hands.HelpRequest;
import com.example.a2hands.Notification;
import com.example.a2hands.Post;
import com.example.a2hands.ProfileActivity;
import com.example.a2hands.SharingOptions;
import com.example.a2hands.User;
import com.example.a2hands.homePackage.PostFragment.OnListFragmentInteractionListener;
import com.example.a2hands.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;
import java.util.List;
import org.ocpsoft.prettytime.PrettyTime;
import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostRecyclerViewAdapter extends RecyclerView.Adapter<MyPostRecyclerViewAdapter.ViewHolder>
{

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
    public class ViewHolder extends RecyclerView.ViewHolder{
        public final View mView;
        public final TextView postOwner;
        public final TextView postContent;
        public final TextView time;
        public final TextView location;
        public final TextView category;
        public final CircleImageView postOwnerPic;
        public final FrameLayout videoContainer;
        public final ImageView postImage;
        public final VideoView postVideo;
        public final ImageView helpBtn;
        public final ImageView ratingsBtn;
        public final ImageView likeBtn;
        public final String uid = FirebaseAuth.getInstance().getUid();
        public final TextView postLikesCommentsCount;
        public final TextView postRatingsSharesCount;
        public final ImageView shareBtn;
        public final TextView postUserSharedPost;
        public final LinearLayout sharingContainer;
        public final LinearLayout postCounter;



        public ViewHolder(View view) {
            super(view);
            mView = view;
            time=view.findViewById(R.id.postTime);
            location =view.findViewById(R.id.postLocation);
            postOwner = view.findViewById(R.id.postOwner);
            postContent =  view.findViewById(R.id.content);
            category = view.findViewById(R.id.postCategory);
            postOwnerPic = view.findViewById(R.id.postOwnerPic);
            ratingsBtn = view.findViewById(R.id.ratingBtn);
            helpBtn = view.findViewById(R.id.helpBtn);
            videoContainer = view.findViewById(R.id.videoContainer);
            postImage = view.findViewById(R.id.postImage);
            postVideo = view.findViewById(R.id.postVideo);
            likeBtn = view.findViewById(R.id.likeBtn);
            postLikesCommentsCount = view.findViewById(R.id.postLikesCommentsCount);
            postRatingsSharesCount = view.findViewById(R.id.postRatingsSharesCount);
            shareBtn = view.findViewById(R.id.shareBtn);
            postUserSharedPost = view.findViewById(R.id.postUserSharedPost);
            sharingContainer = view.findViewById(R.id.sharingContainer);
            postCounter=view.findViewById(R.id.postCounter);
        }

    }

    //Start onBindViewHolder --------------------
    @Override
    public void onBindViewHolder(final ViewHolder holder,final int pos) {
        //check if this post is sharing another post
        if(postsList.get(pos).shared_id != null){
            FirebaseFirestore.getInstance().collection("/posts")
                    .document(postsList.get(pos).shared_id).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            final Post curr_post = task.getResult().toObject(Post.class);
                            setupPostData(holder,curr_post);
                            //enable who sharing label
                            holder.postUserSharedPost.setText(postsList.get(pos).postOwner);
                            holder.sharingContainer.setVisibility(View.VISIBLE);
                        }
                    });
        }else{
            final Post curr_post = postsList.get(pos);
            setupPostData(holder,curr_post);
        }

    }

// end onBindViewHolder------------------------


//start setup post data ---------------------

    public void setupPostData(final ViewHolder holder, final Post curr_post){
        //setup post data

        //check if there are mentions in the post
        String postText ="";
        String [] words = curr_post.content_text.split(" ");
        int i=0;
        for (String word:words) {
            if(word.startsWith("@")){
                postText += "<a href=\"https://www.fb.com/\"><b>"+curr_post.mentions.get(i)+"</b></a>"+" ";
                i++;
            }
            else
                postText+=word+" ";
        }
        holder.postContent .setText(Html.fromHtml(postText));
        holder.postContent.setMovementMethod(LinkMovementMethod.getInstance());


        PrettyTime p = new PrettyTime();
        holder.time.setText(p.format(curr_post.date));
        holder.location.setText(curr_post.location);
        String cat = curr_post.category;
        holder.category.setText((!cat.equals("General"))?cat:"");


        //check visibility
        if(!curr_post.visibility){
            holder.postOwner.setText("Anonymous");
            holder.postOwnerPic.setImageResource(R.drawable.anon);
        }
        else {
            holder.postOwner.setText(curr_post.postOwner);
            holder.postOwnerPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(v.getContext(), ProfileActivity.class);
                    i.putExtra("uid", curr_post.user_id);
                    v.getContext().startActivity(i);
                }
            });
            FirebaseStorage.getInstance().getReference()
                    .child("Profile_Pics/" +curr_post.user_id+ "/"
                            + curr_post.profile_pic)
                    .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri.toString()).into(holder.postOwnerPic);
                }
            });
        }

        ////check if there is any media attached with the post
        if(curr_post.images != null){
            holder.postImage.setVisibility(View.VISIBLE);
            Picasso.get().load(Uri.parse(curr_post.images.get(0))).into(holder.postImage);
        }
        else if(curr_post.videos != null){
            holder.videoContainer.setVisibility(View.VISIBLE);
            holder.postVideo.setVideoURI(Uri.parse(curr_post.videos.get(0)));
            holder.postVideo.requestFocus();
            MediaController mediaController = new MediaController(context);
            holder.postVideo.setMediaController(mediaController);
            mediaController.setAnchorView(holder.postVideo);
        }

        //rating btn listener
        holder.ratingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context,RatingsActivity.class);
                i.putExtra("postId",curr_post.post_id);
                context.startActivity(i);
            }
        });


        //set listener for help button in any post
        holder.helpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if post owner clicks on help button
                if (holder.uid.equals(curr_post.user_id))
                    Toast.makeText(context, "Can't send help request to yourself!", Toast.LENGTH_LONG).show();
                else {
                    //fill object of help request with data
                    final HelpRequest helpReq = new HelpRequest();
                    helpReq.post_id = curr_post.post_id;
                    helpReq.publisher_id = holder.uid;
                    helpReq.subscriber_id = curr_post.user_id;
                    //show confirmation dialog if u want to send help request
                    new AlertDialog.Builder(context)
                            .setTitle("Send Help Request to " +
                                    ((curr_post.visibility) ? curr_post.postOwner : "Anonymous") + "?")
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
                                                    notifi.post_id = curr_post.post_id;
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
        ////////////likes
        //check if post is liked by current user or not
        try {
            isliked(curr_post.post_id,holder.uid, holder.likeBtn);
        } catch (Exception e) {
            e.printStackTrace();
        }
        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.likeBtn.getTag().equals("like")){
                    FirebaseDatabase.getInstance().getReference().child("likes").child(curr_post.post_id)
                            .child(holder.uid).setValue(true);
                    updatePostLikes(curr_post.post_id);

                }
                else {
                    FirebaseDatabase.getInstance().getReference().child("likes")
                            .child(curr_post.post_id).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                    updatePostLikes(curr_post.post_id);
                }
            }
        });

        //set counters for likes , comments , ratings , shares
        int[] count = new int[4];

        count[0]=curr_post.likes_count;
        count[1]=curr_post.comments_count;
        count[2]=curr_post.ratings_count;
        count[3]=curr_post.shares_count;

        //check if there is any likes or comm....
        if(count[0] != 0 || count[1] != 0 || count[2] != 0 || count[3] != 0){
            holder.postCounter.setVisibility(View.VISIBLE);
        }
        holder.postLikesCommentsCount.setText ((count[0]==0)?"":count[0]+" likes"+(
                (count[1]==0)?"":" • "+count[1]+" comments"));

        holder.postRatingsSharesCount.setText ((count[2]==0)?"": count[2]+" ratings"+(
                (count[3]==0)?"":" • "+count[3]+" shares"));


        //set listener for share post button\
        holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharingOptions sharing_option = new SharingOptions();
                Bundle b = new Bundle();
                b.putString("post_id",curr_post.post_id);
                sharing_option.setArguments(b);
                sharing_option.show(((homeActivity)context).getSupportFragmentManager(),"");
            }
        });

    }

// end setupPostData --------------------------

    @Override
    public int getItemCount() {
        return postsList.size();
    }

    private void isliked(String postid ,final String uid, final ImageView imageView){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("likes").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(uid).exists()){
                    imageView.setImageResource(R.drawable.like_filled);
                    imageView.setTag("liked");
                }
                else {
                    imageView.setImageResource(R.drawable.like);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void updatePostLikes(final String postid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("likes")
                .child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //update post with count likes
                DocumentReference ref = FirebaseFirestore.getInstance().collection("posts").document(postid);
                ref.update("likes_count", dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
