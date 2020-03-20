package com.example.a2hands.Users;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.a2hands.R;
import com.example.a2hands.User;

import com.example.a2hands.profile.ProfileActivity;
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

import java.text.DecimalFormat;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyuserRecyclerViewAdapter extends RecyclerView.Adapter<MyuserRecyclerViewAdapter.ViewHolder> {

    private final List<User> usersList;
    //private final OnListFragmentInteractionListener mListener;
    private String curr_uid;
    private Context context;

    public MyuserRecyclerViewAdapter(List<User> items) {
        usersList = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_user, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final RatingBar resultRatingBar;
        public final ImageView userFollowBtn;
        public final TextView resultUserJob;
        public final TextView resultUserName;
        public final CircleImageView resultUserPic;
        public final TextView resultUserLocation;
        public final TextView resultUserRating;

        public ViewHolder(View view) {
            super(view);
            context = view.getContext();
            mView = view;
            resultRatingBar = view.findViewById(R.id.resultRatingBar);
            userFollowBtn = view.findViewById(R.id.userFollowBtn);
            resultUserJob = view.findViewById(R.id.resultUserJob);
            resultUserName= view.findViewById(R.id.resultUserName);
            resultUserPic= view.findViewById(R.id.resultUserPic);
            resultUserLocation = view.findViewById(R.id.resultUserLocation);
            resultUserRating = view.findViewById(R.id.resultUserRating);
            curr_uid = FirebaseAuth.getInstance().getUid();

        }

    }
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final User user = usersList.get(position);
        holder.resultRatingBar.setRating((float)user.rate);
        holder.resultUserJob.setText(user.job_title);
        holder.resultUserName.setText(user.full_name);
        DecimalFormat df = new DecimalFormat("##.##");
        holder.resultUserRating.setText(df.format(user.rate));
        String location = user.country+((user.region.equals(""))?"":", "+user.region);
        holder.resultUserLocation.setText(location);
        //check if there is a pro pic
        if(!user.profile_pic.equals(""))
            FirebaseStorage.getInstance().getReference("Profile_Pics/"+user.user_id+"/"+user.profile_pic).getDownloadUrl()
                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            Picasso.get().load(task.getResult()).into(holder.resultUserPic);
                        }
                    });


        //check if followed
        checkFollowingState(holder , user);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent = new Intent(holder.mView.getContext(), ProfileActivity.class);
                    intent.putExtra("uid",user.user_id);
                    holder.mView.getContext().startActivity(intent);
                    // add to recent searches to the database
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("recent_users");
                    ref.child(FirebaseAuth.getInstance().getUid()).child(user.user_id).setValue(true);


            }
        });
    }
    void checkFollowingState(final ViewHolder h , final User user){
        //check if follow or un follow
        FirebaseDatabase.getInstance().getReference("followings").child(curr_uid).child(user.user_id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //if curr_uid following uid
                        if (dataSnapshot.exists()) {
                            //change style of follow btn to followed
                            h.userFollowBtn.setImageResource(R.drawable.followed_user);

                            setFollowListener(h,user);
                        } else {
                            //change style of follow btn to unfollowed
                            h.userFollowBtn.setImageResource(R.drawable.add_user);

                            setUnfollowListener(h,user);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }
    void setFollowListener(final ViewHolder h,final User user){
        h.userFollowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //confirm unfollow
                new AlertDialog.Builder(context)
                        .setTitle("Are you sure you want to unfollow ?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //// delete following and follower
                                // followings > following > follower
                                FirebaseDatabase.getInstance().getReference("followings").child(curr_uid)
                                        .child(user.user_id).setValue(null);
                                // followers > follower > following
                                FirebaseDatabase.getInstance().getReference("followers").child(user.user_id)
                                        .child(curr_uid).setValue(null);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });
    }
    void setUnfollowListener(final ViewHolder h,final User user) {
        h.userFollowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // set new following and follower
                FirebaseDatabase.getInstance().getReference("followings").child(curr_uid)
                        .child(user.user_id).setValue(true);
                FirebaseDatabase.getInstance().getReference("followers").child(user.user_id)
                        .child(curr_uid).setValue(true);
            }
        });
    }
    @Override
    public int getItemCount() {
        return usersList.size();
    }

}
