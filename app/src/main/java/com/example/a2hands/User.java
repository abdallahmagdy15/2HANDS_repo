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

    public User(String country, String region, String first_name,
                boolean gender, String last_name, String job_title,
                String phone, String user_name,String profile_cover ,
                Date birth_date , int rate , String bio , String profile_pic) {
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
    }

    public User(){

    }
}
