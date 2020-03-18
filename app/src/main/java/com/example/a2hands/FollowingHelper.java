package com.example.a2hands;

import com.google.firebase.database.FirebaseDatabase;

public class FollowingHelper {
    String curr_uid;
    String uid;

    public FollowingHelper(String curr_uid, String uid) {
        this.curr_uid = curr_uid;
        this.uid = uid;
    }
    public void unfollow(){
        //// delete following and follower
        // followings > following > follower
        FirebaseDatabase.getInstance().getReference("followings").child(curr_uid)
                .child(uid).setValue(null);
        // followers > follower > following
        FirebaseDatabase.getInstance().getReference("followers").child(uid)
                .child(curr_uid).setValue(null);
    }

    public void follow(){
        // set new following and follower
        FirebaseDatabase.getInstance().getReference("followings").child(curr_uid)
                .child(uid).setValue(true);
        FirebaseDatabase.getInstance().getReference("followers").child(uid)
                .child(curr_uid).setValue(true);
    }
}
