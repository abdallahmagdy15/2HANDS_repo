package com.example.a2hands;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.IgnoreExtraProperties;


import java.util.Date;
@IgnoreExtraProperties
public class User {
    public String user_id;

    public String full_name;
    public String user_name;
    public boolean gender;
    public Timestamp birth_date;

    public String country;
    public String region;
    public String phone;

    public double rate;
    public String bio;
    public String profile_pic;
    public String profile_cover;
    public String job_title;
    public int ratings_count;

    public String onlineStatus;
    public String typingTo;

    public User(
            String user_id,

            String full_name,
            String user_name,
            boolean gender,
            Timestamp birth_date,

            String country,
            String region,
            String phone,

            double rate,
            String bio,
            String profile_pic,
            String profile_cover,
            String job_title,
            int ratings_count,

            String onlineStatus,
            String typingTo
    ) {

        this.user_id = user_id;

        this.full_name = full_name;
        this.user_name = user_name;
        this.gender = gender;
        this.birth_date = birth_date;

        this.country = country;
        this.region = region;
        this.phone = phone;

        this.rate = rate;
        this.bio = bio;
        this.profile_pic = profile_pic;
        this.profile_cover = profile_cover;
        this.job_title = job_title;
        this.ratings_count = ratings_count;

        this.onlineStatus = onlineStatus;
        this.typingTo = typingTo;
    }

    public User(){

    }
}
