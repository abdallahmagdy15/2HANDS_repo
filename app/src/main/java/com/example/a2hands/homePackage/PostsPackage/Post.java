package com.example.a2hands.homePackage.PostsPackage;

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
    public List<String> images;
    public List<String> mentions;
    public boolean state;
    public boolean visibility;
    public String location;
    public String postOwner;
    public String user_id;
    public String profile_pic;
    public String post_id;
    public String shared_id;

    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Post(String category, String check_in, String content_text,
                Date date, List<String> videos, String post_id,
                List<String> images, boolean state, String profile_pic ,
                boolean visibility , String location , String user_id,List<String> mentions ,
               String shared_id
    ) {
        this.category = category;
        this.check_in = check_in;
        this.content_text = content_text;
        this.date = date;
        this.videos = videos;
        this.images = images;
        this.state = state;
        this.visibility = visibility;
        this.location = location;
        this.user_id = user_id;
        this.profile_pic = profile_pic;
        this.post_id = post_id;
        this.shared_id = shared_id;
        this.mentions = mentions;
    }
}
