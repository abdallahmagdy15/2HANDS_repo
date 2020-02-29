package com.example.a2hands;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.Date;
@IgnoreExtraProperties
public class User {
    public String country;
    public String region;
    public String first_name;
    public boolean gender;
    public String last_name;
    public String phone;
    public String user_name;
    public Date birth_date;
    public double rate;
    public String bio;
    public String profile_pic;
    public String profile_cover;
    public String job_title;
    public int ratings_count;
    public String user_id;

    public User(
                String first_name,
                String last_name,
                boolean gender,
                Date birth_date,
            String country,
                String phone,
                String region,
                String user_name,
                double rate,
                String bio,
                String profile_pic,
                String profile_cover,
                String job_title,
                int ratings_count,
                String user_id
    ) {
        this.country = country;
        this.region = region;
        this.first_name = first_name;
        this.gender = gender;
        this.last_name = last_name;
        this.phone = phone;
        this.user_name = user_name;
        this.birth_date = birth_date;
        this.rate = rate;
        this.bio = bio;
        this.profile_pic = profile_pic;
        this.profile_cover = profile_cover;
        this.job_title = job_title;
        this.ratings_count = ratings_count;
        this.user_id = user_id;
    }

    public User(){

    }
}
