package com.example.a2hands.home.posts;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

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
    public boolean state=true;
    public boolean visibility=true;
    public String location;
    public String user_id;
    public String profile_pic;
    public String post_id;
    public String shared_id;
    public double priority=0;
    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Post(String category, String check_in, String content_text,
                Date date, List<String> videos, List<String> images,
                List<String> mentions, boolean state, boolean visibility,
                String location, String user_id,
                String profile_pic, String post_id, String shared_id,
                double priority) {
        this.category = category;
        this.check_in = check_in;
        this.content_text = content_text;
        this.date = date;
        this.videos = videos;
        this.images = images;
        this.mentions = mentions;
        this.state = state;
        this.visibility = visibility;
        this.location = location;
        this.user_id = user_id;
        this.profile_pic = profile_pic;
        this.post_id = post_id;
        this.shared_id = shared_id;
        this.priority = priority;
    }
}
