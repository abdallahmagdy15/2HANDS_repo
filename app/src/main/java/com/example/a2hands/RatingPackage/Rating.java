package com.example.a2hands.RatingPackage;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

@IgnoreExtraProperties
public class Rating {
    public String rating_id;
    public String publisher_id;
    public @ServerTimestamp Date date;
    public String subscriber_id;
    public int rate;
    public String subscriber_pic;
    public String subscriber_name;
    public String post_id;
    public String review_text;

    public Rating (){}
    public Rating(String publisher_id, Date date, String subscriber_id,String review_text,
           String rating_id, int rate, String subscriber_pic, String subscriber_name, String post_id ) {
        this.publisher_id = publisher_id;
        this.date = date;
        this.subscriber_id = subscriber_id;
        this.rate = rate;
        this.subscriber_pic = subscriber_pic;
        this.subscriber_name = subscriber_name;
        this.post_id = post_id;
        this.review_text = review_text;
        this.rating_id = rating_id;
    }
}

