package com.example.a2hands.home.comments;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

@IgnoreExtraProperties
public class Comment {
    public String comment_content;
    public @ServerTimestamp
    Date date;
    public String post_id;
    public String comment_id;
    public String publisher_id;
    public List<String> mentions;
    public String publisher_pic;
    public String image;
    public String video;
    public String name;

    public Comment(String comment_content, Date date, String post_id, String comment_id, String publisher_id, List<String> mentions, String publisher_pic, String image, String video, String name) {
        this.comment_content = comment_content;
        this.date = date;
        this.post_id = post_id;
        this.comment_id = comment_id;
        this.publisher_id = publisher_id;
        this.mentions = mentions;
        this.publisher_pic = publisher_pic;
        this.image = image;
        this.video = video;
        this.name = name;
    }

    public Comment(){}
}
