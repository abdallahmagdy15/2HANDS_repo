package com.example.a2hands;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import org.w3c.dom.Document;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@IgnoreExtraProperties
public class Post {

     public String category;
     public String check_in;
     public String content_text;
     public @ServerTimestamp Date date;
     public List<String> videos;
     public int likes_count = 0;
     public List<String> images;
     public boolean state;
     public boolean visibility;
     public String location;
     public String postOwner;
     public String user_id;

    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

     public Post(String category, String check_in, String content_text,
                 Date date, List<String> videos, int likes_count,
                 List<String> images, boolean state,
                 boolean visibility , String location , String user_id) {
         this.category = category;
         this.check_in = check_in;
         this.content_text = content_text;
         this.date = date;
         this.videos = videos;
         this.likes_count = likes_count;
         this.images = images;
         this.state = state;
         this.visibility = visibility;
         this.location = location;
         this.user_id = user_id;
     }
}
