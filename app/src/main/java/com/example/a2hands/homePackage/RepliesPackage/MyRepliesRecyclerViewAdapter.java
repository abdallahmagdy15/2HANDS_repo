package com.example.a2hands.homePackage.RepliesPackage;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.a2hands.ProfileActivity;
import com.example.a2hands.R;
import com.example.a2hands.homePackage.CommentsPackage.Comment;
import com.example.a2hands.homePackage.CommentsPackage.CommentsFragment;
import com.example.a2hands.homePackage.RepliesPackage.RepliesFragment.OnListFragmentInteractionListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyRepliesRecyclerViewAdapter extends RecyclerView.Adapter<MyRepliesRecyclerViewAdapter.ViewHolder> {

    private final List<Reply> repliesList;
    private final RepliesFragment.OnListFragmentInteractionListener mListener;

    public MyRepliesRecyclerViewAdapter(List<Reply> items, OnListFragmentInteractionListener listener) {
        repliesList = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_replies, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final Reply curr_reply = repliesList.get(position);
        holder.replyContent.setText(curr_reply.reply_content);
        holder.replyOwner.setText(curr_reply.name);
        PrettyTime p = new PrettyTime();
        holder.replyTime.setText(p.format(curr_reply.date));
        FirebaseStorage.getInstance().getReference("Profile_Pics")
                .child(curr_reply.publisher_id + "/" + curr_reply.publisher_pic).getDownloadUrl()
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        Picasso.get().load(task.getResult()).into(holder.replyOwnerPic);
                    }
                });

        holder.replyOwnerPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(holder.context, ProfileActivity.class);
                i.putExtra("uid", curr_reply.publisher_id);
                holder.context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return repliesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView replyOwner;
        public final TextView replyLikeBtn;
        public final TextView replyContent;
        public final CircleImageView replyOwnerPic;
        public final TextView mentionReplyBtn;
        public final TextView replyTime;
        public final Context context;
        public final String uid = FirebaseAuth.getInstance().getUid();

        public ViewHolder(View view) {
            super(view);
            replyOwner = view.findViewById(R.id.replyOwner);
            replyLikeBtn = view.findViewById(R.id.replyLikeBtn);
            replyContent = view.findViewById(R.id.replyContent);
            replyOwnerPic = view.findViewById(R.id.replyOwnerPic);
            mentionReplyBtn = view.findViewById(R.id.mentionReplyBtn);
            replyTime = view.findViewById(R.id.replyTime);
            context = view.getContext();
        }
    }
}
