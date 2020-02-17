package com.example.a2hands;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class HelpRequest {
    public String publisher_id;
    public String subscriber_id;
    public String post_id;

    public HelpRequest() {}
    public HelpRequest(String publisher_id, String subscriber_id, String post_id) {
        this.publisher_id = publisher_id;
        this.subscriber_id = subscriber_id;
        this.post_id = post_id;
    }
}
