package com.example.a2hands;

import java.util.Date;

public class User {
    public String country;
    public String region;
    public String first_name;
    public boolean gender;
    public String last_name;
    public String phone;
    public String user_name;
    public Date birth_date;

    public User(String country, String region, String first_name,
                boolean gender, String last_name,
                String phone, String user_name,
                Date birth_date) {
        this.country = country;
        this.region = region;
        this.first_name = first_name;
        this.gender = gender;
        this.last_name = last_name;
        this.phone = phone;
        this.user_name = user_name;
        this.birth_date = birth_date;
    }

    public User(){

    }
}
