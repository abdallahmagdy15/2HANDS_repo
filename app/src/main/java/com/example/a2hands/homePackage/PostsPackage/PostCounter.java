package com.example.a2hands.homePackage.PostsPackage;

import com.google.firebase.firestore.IgnoreExtraProperties;

public class PostCounter {
    public int likes_count=0;
    public int comments_count=0;
    public int ratings_count=0;
    public int shares_count=0;
    public PostCounter(){}

    public PostCounter(int likes_count, int comments_count, int ratings_count, int shares_count) {
        this.likes_count = likes_count;
        this.comments_count = comments_count;
        this.ratings_count = ratings_count;
        this.shares_count = shares_count;
    }
}
