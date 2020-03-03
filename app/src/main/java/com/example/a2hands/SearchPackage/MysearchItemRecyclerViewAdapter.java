package com.example.a2hands.SearchPackage;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.a2hands.ProfileActivity;
import com.example.a2hands.R;
import com.example.a2hands.SearchPackage.searchItemFragment.OnListFragmentInteractionListener;
import com.example.a2hands.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MysearchItemRecyclerViewAdapter extends RecyclerView.Adapter<MysearchItemRecyclerViewAdapter.ViewHolder> {

    private final List<User> usersList;
    private final OnListFragmentInteractionListener mListener;

    public MysearchItemRecyclerViewAdapter(List<User> items, OnListFragmentInteractionListener listener) {
        usersList = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_searchitem, parent, false);
        return new ViewHolder(view);
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final RatingBar resultRatingBar;
        public final ImageView resultUserFollowBtn;
        public final TextView resultUserJob;
        public final TextView resultUserName;
        public final CircleImageView resultUserPic;
        public final TextView resultUserLocation;
        public final TextView resultUserRating;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            resultRatingBar = view.findViewById(R.id.resultRatingBar);
            resultUserFollowBtn = view.findViewById(R.id.resultUserFollowBtn);
            resultUserJob = view.findViewById(R.id.resultUserJob);
            resultUserName= view.findViewById(R.id.resultUserName);
            resultUserPic= view.findViewById(R.id.resultUserPic);
            resultUserLocation = view.findViewById(R.id.resultUserLocation);
            resultUserRating = view.findViewById(R.id.resultUserRating);

        }

    }
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final User user = usersList.get(position);
        holder.resultRatingBar.setRating((float)user.rate);
        holder.resultUserJob.setText(user.job_title);
        holder.resultUserName.setText(user.first_name+" "+user.last_name);
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

        holder.resultUserFollowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    Intent intent = new Intent(holder.mView.getContext(),ProfileActivity.class);
                    intent.putExtra("uid",user.user_id);
                    holder.mView.getContext().startActivity(intent);
                    // add to recent searches to the database
                     DatabaseReference ref = FirebaseDatabase.getInstance().getReference("recent_users");
                    ref.child(FirebaseAuth.getInstance().getUid()).child(user.user_id).setValue(true);
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(user);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }


}
