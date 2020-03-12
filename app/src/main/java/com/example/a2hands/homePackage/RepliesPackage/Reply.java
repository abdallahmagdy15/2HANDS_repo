package com.example.a2hands.homePackage.RepliesPackage;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

public class Reply {
    public String reply_content;
    public @ServerTimestamp
    Date date;
    public String reply_id;
    public String comment_id;
    public String post_id;
    public String publisher_id;
    public List<String> mentions;
    public String publisher_pic;
    public String image;
    public String video;
    public String name;

    public Reply(String reply_content, Date date, String reply_id, String comment_id, String post_id, String publisher_id, List<String> mentions, String publisher_pic, String image, String video, String name) {
        this.reply_content = reply_content;
        this.date = date;
        this.reply_id = reply_id;
        this.comment_id = comment_id;
        this.post_id = post_id;
        this.publisher_id = publisher_id;
        this.mentions = mentions;
        this.publisher_pic = publisher_pic;
        this.image = image;
        this.video = video;
        this.name = name;
    }

    public Reply() {
    }
}
