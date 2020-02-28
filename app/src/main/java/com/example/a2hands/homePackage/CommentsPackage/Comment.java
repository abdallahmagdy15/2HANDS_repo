package com.example.a2hands.homePackage.CommentsPackage;

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
    public String publisher_id;
    public List<String> mentions;
    public String publisher_pic;
    public String image;
    public String video;
    public String name;

    public Comment(String comment_content, Date date,
                   String post_id,String name,
                   String publisher_id, List<String> mentions,
                   String publisher_pic, String image, String video) {
        this.comment_content = comment_content;
        this.date = date;
        this.post_id = post_id;
        this.publisher_id = publisher_id;
        this.mentions = mentions;
        this.publisher_pic = publisher_pic;
        this.image = image;
        this.video = video;
        this.name = name;
    }
    public Comment(){}
}
