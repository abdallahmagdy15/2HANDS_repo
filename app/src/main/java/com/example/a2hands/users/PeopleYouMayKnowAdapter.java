package com.example.a2hands.users;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.List;


public class PeopleYouMayKnowAdapter extends RecyclerView.Adapter<PeopleYouMayKnowAdapter.ViewHolder> {

    private final List<User> usersList;
    private String curr_uid;
    private Context context;

    public PeopleYouMayKnowAdapter(List<User> items) {
        usersList = items;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_you_may_know, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final CardView userFollowCardV;
        public final TextView userFollowTxt;
        public final TextView userYouMayKnowName;
        public final ImageView userYouMayKnowPic;

        public ViewHolder(View view) {
            super(view);
            context = view.getContext();
            mView = view;
            userFollowCardV = view.findViewById(R.id.userYouMayKnowFollowCardView);
            userFollowTxt = view.findViewById(R.id.userYouMayKnowFollowBtnTxt);
            userYouMayKnowPic = view.findViewById(R.id.userYouMayKnowProfilePic);
            userYouMayKnowName = view.findViewById(R.id.userYouMayKnowName);
            curr_uid = FirebaseAuth.getInstance().getUid();
        }
    }

    @Override
    public void onBindViewHolder(@NotNull final ViewHolder holder, int position) {
        final User user = usersList.get(position);
        DecimalFormat df = new DecimalFormat("##.##");
        holder.userYouMayKnowName.setText(user.full_name);

        //check if there is a pro pic
        if(!user.profile_pic.equals("")){
            FirebaseFirestore.getInstance().collection("users/").document(user.user_id)
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        User user = task.getResult().toObject(User.class);
                        Picasso.with(context).load(Uri.parse(user.profile_pic)).into(holder.userYouMayKnowPic);
                    }
                }
            });
        }

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

    private void checkFollowingState(final ViewHolder h, final User user){
        //check if follow or un follow
        FirebaseDatabase.getInstance().getReference("followings").child(curr_uid).child(user.user_id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //if curr_uid following uid
                        if (dataSnapshot.exists()) {
                            //change style of follow btn to followed
                            h.userFollowTxt.setTextColor(context.getResources().getColor(R.color.colorPureWhite));
                            h.userFollowTxt.setText(context.getResources().getString(R.string.following));
                            h.userFollowCardV.setCardBackgroundColor(context.getResources().getColor(R.color.colorAccent));

                            setFollowListener(h,user);
                        } else {
                            //change style of follow btn to unFollowed
                            h.userFollowTxt.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
                            h.userFollowTxt.setText(context.getResources().getString(R.string.follow));
                            h.userFollowCardV.setCardBackgroundColor(context.getResources().getColor(R.color.colorWhite));

                            setUnfollowListener(h,user);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    private void setFollowListener(final ViewHolder h, final User user){
        h.userFollowCardV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //confirm unFollow
                new AlertDialog.Builder(context)
                        .setTitle(context.getResources().getString(R.string.areYouSureYouWantToUnFollow))
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

    private void setUnfollowListener(final ViewHolder h, final User user) {
        h.userFollowCardV.setOnClickListener(new View.OnClickListener() {
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
