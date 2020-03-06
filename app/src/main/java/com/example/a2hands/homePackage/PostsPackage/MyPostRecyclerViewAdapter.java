package com.example.a2hands.homePackage.PostsPackage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
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

import com.example.a2hands.homePackage.CommentsPackage.CommentsActivity;
import com.example.a2hands.HelpRequest;
import com.example.a2hands.NotificationsPackage.Notification;
import com.example.a2hands.ProfileActivity;
import com.example.a2hands.SharingOptions;
import com.example.a2hands.User;
import com.example.a2hands.homePackage.PostsPackage.PostFragment.OnListFragmentInteractionListener;
import com.example.a2hands.R;
import com.example.a2hands.RatingPackage.RatingsActivity;
import com.example.a2hands.homePackage.homeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
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
        public final ImageView commentBtn;
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
            commentBtn = view.findViewById(R.id.commentBtn);
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
                            // set listener for user sharing the post to go to his profile
                            holder.postUserSharedPost.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(context,ProfileActivity.class);
                                    i.putExtra("uid",postsList.get(pos).user_id);
                                    context.startActivity(i);
                                }
                            });
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
        int[][] mentionsIndexes = new int[words.length][2];
        int i=0;
        int curr_char_index = 0;
        for (String word:words) {
            if(word.startsWith("@")){
                //del @ and seperate the name by white space
                String[] fullName = word.split("_");
                word = fullName[0].substring(1)+" "+fullName[1]+" ";
                try {
                    mentionsIndexes[i][0] = (postText.length()==0)?0:postText.length()-1;//set start of the mentioned name
                    postText += word;
                    mentionsIndexes[i][1] = postText.length()-1;//set end of the mentioned name
                } catch (Exception e) {
                    e.printStackTrace();
                }
                i++;
            }
            else
                postText+=word+" ";
        }
        SpannableString ss = new SpannableString(postText);
        for(int j =0 ; j<mentionsIndexes.length ; j++){
            final int x = j;
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    Intent intent = new Intent(context, ProfileActivity.class);
                    String uid=curr_post.mentions.get(x);
                    intent.putExtra("uid",uid);
                    context.startActivity(intent);
                }
                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                    ds.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                }
            };
            ss.setSpan(clickableSpan, mentionsIndexes[j][0], mentionsIndexes[j][1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        holder.postContent.setText(ss);
        holder.postContent.setMovementMethod(LinkMovementMethod.getInstance());
        holder.postContent.setHighlightColor(Color.TRANSPARENT);

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
                Intent i = new Intent(context, RatingsActivity.class);
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
        try {
            holder.likeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(holder.likeBtn.getTag().equals("like")){
                        //update likes with users
                        FirebaseDatabase.getInstance().getReference().child("likes").child(curr_post.post_id)
                                .child(holder.uid).setValue(true);
                        //update counter for likes
                        FirebaseDatabase.getInstance().getReference("counter").child(curr_post.post_id)
                        .child("likes_count").runTransaction(new Transaction.Handler() {
                            @NonNull
                            @Override
                            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                int curr_likes = mutableData.getValue(Integer.class);
                                mutableData.setValue(curr_likes +1);
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

                            }
                        });
                        //end update counter for likes
                    }

                    else {
                        FirebaseDatabase.getInstance().getReference().child("likes")
                                .child(curr_post.post_id).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                        //update counter for likes
                        FirebaseDatabase.getInstance().getReference("counter").child(curr_post.post_id)
                                .child("likes_count").runTransaction(new Transaction.Handler() {
                            @NonNull
                            @Override
                            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                int curr_likes = mutableData.getValue(Integer.class);
                                mutableData.setValue(curr_likes-1);
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

                            }
                        });
                        //end update counter for likes
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        //get counters for likes , comments , ratings , shares
        final int[] count = new int[4];
        DatabaseReference counterRef = FirebaseDatabase.getInstance().getReference().child("counter").child(curr_post.post_id);
        counterRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    PostCounter postCounter = dataSnapshot.getValue(PostCounter.class);
                    count[0]=postCounter.likes_count;
                    count[1]=postCounter.comments_count;
                    count[2]=postCounter.ratings_count;
                    count[3]=postCounter.shares_count;
                    updatePostWithCounter(holder,count);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });



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

        //set comment btn listener
        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CommentsActivity.class);
                intent.putExtra("post_id", curr_post.post_id);
                intent.putExtra("likes_count",count[0]);
                intent.putExtra("curr_uid",holder.uid);
                context.startActivity(intent);
            }
        });
    }

// end setupPostData --------------------------

    void updatePostWithCounter(ViewHolder holder, int[] count){
        //check if there is any likes or comm....
        if(count[0] != 0 || count[1] != 0 || count[2] != 0 || count[3] != 0){
            holder.postCounter.setVisibility(View.VISIBLE);
        }
        holder.postLikesCommentsCount.setText (
                (count[0]==0)? "" +((count[1]==0)?"":count[1]+" comments")
                        : count[0]+" likes"+((count[1]==0)?"":" • "+count[1]+" comments")
        );
        holder.postRatingsSharesCount.setText (
                (count[2]==0)? "" +((count[3]==0)?"":count[3]+" shares")
                        : count[2]+" ratings"+((count[3]==0)?"":" • "+count[3]+" shares")
        );
    }
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

}
