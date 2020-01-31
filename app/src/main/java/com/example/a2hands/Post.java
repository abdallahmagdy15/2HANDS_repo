package com.example.a2hands;

import java.util.Date;


 class Post {

    private String category;
    private String check_in;
    private String content_text;
    private Date date;
    private String user_id;
    private String[] videos;
    private int likes_count = 0;
    private String[] images;
    private String privacy;
    private boolean state;
    private String visibility;

    //public Map<String, Boolean> stars = new HashMap<>();

     Post() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
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

     String[] getVideos() {
        return videos;
    }

     int getLikes_count() {
        return likes_count;
    }

     String[] getImages() {
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

     void setCategory(String category) {
        this.category = category;
    }

     void setCheck_in(String check_in) {
        this.check_in = check_in;
    }

     void setContent_text(String content_text) {
        this.content_text = content_text;
    }

     void setDate(Date date) {
        this.date = date;
    }

     void setUser_id(String user_id) {
        this.user_id = user_id;
    }

     void setVideos(String[] videos) {
        this.videos = videos;
    }

     void setLikes_count(int likes_count) {
        this.likes_count = likes_count;
    }

     void setImages(String[] images) {
        this.images = images;
    }

     void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

     void setState(boolean state) {
        this.state = state;
    }

     void setVisibility(String visibility) {
        this.visibility = visibility;
    }
}
