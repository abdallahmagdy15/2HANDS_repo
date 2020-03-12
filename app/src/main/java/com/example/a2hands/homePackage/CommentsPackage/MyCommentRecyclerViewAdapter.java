package com.example.a2hands.homePackage.CommentsPackage;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.a2hands.ProfileActivity;
import com.example.a2hands.R;
import com.example.a2hands.homePackage.CommentsPackage.CommentsFragment.OnListFragmentInteractionListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MyCommentRecyclerViewAdapter extends RecyclerView.Adapter<MyCommentRecyclerViewAdapter.ViewHolder> {

    private final List<Comment> commentsList;
    private final OnListFragmentInteractionListener mListener;


    public MyCommentRecyclerViewAdapter(List<Comment> items, OnListFragmentInteractionListener listener) {
        commentsList = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_comment, parent, false);
        return new ViewHolder(view);
    }
    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView commentOwner;
        public final TextView commentLikeBtn;
        public final TextView commentContent;
        public final CircleImageView commentOwnerPic;
        public final TextView commentReplyBtn;
        public final TextView commentTime;
        public final Context context;
        public final String uid = FirebaseAuth.getInstance().getUid();

        public ViewHolder(View view) {
            super(view);
            context = view.getContext();
            commentOwner = view.findViewById(R.id.commentOwner);
            commentLikeBtn = view.findViewById(R.id.commentLikeBtn);
            commentContent = view.findViewById(R.id.commentContent);
            commentOwnerPic = view.findViewById(R.id.commentOwnerPic);
            commentReplyBtn = view.findViewById(R.id.commentReplyBtn);
            commentTime = view.findViewById(R.id.commentTime);


        }

    }
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Comment curr_comment=commentsList.get(position);
        holder.commentContent.setText(curr_comment.comment_content);
        holder.commentOwner.setText(curr_comment.name);
        PrettyTime p = new PrettyTime();
        holder.commentTime.setText(p.format(curr_comment.date));
        FirebaseStorage.getInstance().getReference("Profile_Pics")
                .child(curr_comment.publisher_id+"/"+curr_comment.publisher_pic).getDownloadUrl()
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        Picasso.get().load( task.getResult()).into(holder.commentOwnerPic);
                    }
                });
        holder.commentContent.setText(curr_comment.comment_content);
        holder.commentOwnerPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(holder.context, ProfileActivity.class);
                i.putExtra("uid",curr_comment.publisher_id);
                holder.context.startActivity(i);
            }
        });

        //check if the current user liked the post or not
        try {
            isliked(curr_comment.post_id, curr_comment.comment_id, holder.uid, holder.commentLikeBtn);
        } catch (Exception e) {
            e.printStackTrace();
        }


        //Like comment button chnage color if it clicked by current user
        try {
            holder.commentLikeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.commentLikeBtn.getTag().equals("like")) {
                        //update likes with users
                        FirebaseDatabase.getInstance().getReference().child("likesComments").child(curr_comment.post_id).child(curr_comment.comment_id)
                                .child(holder.uid).setValue(true);
                    } else {
                        FirebaseDatabase.getInstance().getReference().child("likesComments")
                                .child(curr_comment.post_id).child(curr_comment.comment_id).child(holder.uid).removeValue();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    //method to check it the use like the comment
    private void isliked(String postid, String commentid, final String uid, final TextView textView) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("likesComments").child(postid).child(commentid);
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(uid).exists()) {
                    textView.setTextColor(Color.parseColor("#00BCD4"));
                    textView.setTag("liked");
                } else {
                    textView.setTextColor(R.color.colorDisabled);
                    textView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

}
