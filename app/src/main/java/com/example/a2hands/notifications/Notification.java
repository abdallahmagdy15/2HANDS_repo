package com.example.a2hands.notifications;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

@IgnoreExtraProperties
public class Notification implements Comparable<Notification> {
    public String subscriber_id;
    public String publisher_id;
    public String publisher_pic;
    public String subscriber_pic;
    public boolean is_seen=false;
    public String content;

    public Date getDate() {
        return date;
    }

    public Notification setDate(Date date) {
        this.date = date;
        return this;
    }

    public @ServerTimestamp Date date;
    public String type;
    public String help_request_id;
    public String notification_id;
    public String post_id;
    public String publisher_name;
    public String subscriber_name;
    public Notification() {
    }

    @Override
    public int compareTo(Notification notifi) {
        if (getDate() == null || notifi.getDate() == null) {
            return 0;
        }
        return getDate().compareTo(notifi.getDate());
    }

    public Notification(String subscriber_id, String publisher_id, String publisher_pic, String subscriber_name,
                        String content, Date date, String type, String post_id , String subscriber_pic, boolean is_seen,
                        String help_request_id, String notification_id , String publisher_name) {

        this.subscriber_id = subscriber_id;
        this.publisher_id = publisher_id;
        this.publisher_pic = publisher_pic;
        this.content = content;
        this.date = date;
        this.type = type;
        this.help_request_id = help_request_id;
        this.notification_id = notification_id;
        this.post_id = post_id;
        this.publisher_name = publisher_name;
        this.subscriber_pic = subscriber_pic;
        this.subscriber_name = subscriber_name;
        this.is_seen = is_seen;
    }
}
