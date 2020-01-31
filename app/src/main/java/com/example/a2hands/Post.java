package com.example.a2hands;

import com.google.firebase.firestore.DocumentReference;

import org.w3c.dom.Document;

import java.util.Date;
import java.util.List;


class Post {

     public String category;
     public String check_in;
     public String content_text;
     public Date date;
     public String user_id;
     public List<String> videos;
     public int likes_count = 0;
     public List<String> images;
     public String privacy;
     public boolean state;
     public String visibility;

    //public Map<String, Boolean> stars = new HashMap<>();

    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

     public Post(String category, String check_in, String content_text,
                 Date date, String user_id, List<String> videos, int likes_count,
                 List<String> images, String privacy, boolean state, String visibility) {
         this.category = category;
         this.check_in = check_in;
         this.content_text = content_text;
         this.date = date;
         this.user_id = user_id;
         this.videos = videos;
         this.likes_count = likes_count;
         this.images = images;
         this.privacy = privacy;
         this.state = state;
         this.visibility = visibility;
     }

     String getCategory() {
        return category;
    }

     String getCheck_in() {
        return check_in;
    }

     String getContent_text() {
        return content_text;
    }

     Date getDate() {
        return date;
    }

     String getUser_id() {
        return user_id;
    }

    List<String> getVideos() {
        return videos;
    }

     int getLikes_count() {
        return likes_count;
    }

    List<String> getImages() {
        return images;
    }

     String getPrivacy() {
        return privacy;
    }

     boolean getState() {
        return state;
    }

     String getVisibility() {
        return visibility;
    }
}
