package com.example.a2hands;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

public class ChangeLocale {

    //for changing app language
    private static void setLocale(String lang, Context context) {
        if (!"".equals(lang)){
            Locale locale = new Locale(lang);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.setLocale(locale);
            context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());

            //save the data to shared preferences
            SharedPreferences.Editor editor = context.getSharedPreferences("settings", Activity.MODE_PRIVATE).edit();
            editor.putString("My_Language", lang);
            editor.apply();
        }
    }

    public static void loadLocale (Context context){
        SharedPreferences prefs = context.getSharedPreferences("settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Language", "");
        setLocale(language, context);
    }
}
