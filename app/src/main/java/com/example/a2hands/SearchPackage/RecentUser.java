package com.example.a2hands.SearchPackage;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.List;
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
