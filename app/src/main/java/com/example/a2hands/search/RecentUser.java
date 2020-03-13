package com.example.a2hands.search;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class RecentUser {
    public String user_id;
    public String recent_user;

    public RecentUser(){}
    public RecentUser(String user_id, String recent_user) {
        this.user_id = user_id;
        this.recent_user = recent_user;
    }
}
