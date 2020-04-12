package com.example.a2hands;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import com.example.a2hands.home.posts.PostsFragment;

import java.util.Locale;

public class SavedPostsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_saved);
        Toolbar toolbar = findViewById(R.id.savedPostsToolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.savedPosts));

        Fragment frg = new PostsFragment();
        Bundle b = new Bundle();
        b.putString("FOR","SAVED_POSTS");
        frg.setArguments(b);
        FragmentTransaction tr = this.getSupportFragmentManager().beginTransaction()
                .replace(R.id.savedPostsContainer,frg,null).addToBackStack(null);
        tr.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }



    //for changing app language
    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);

        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        //save the data to shared preferences
        SharedPreferences.Editor editor = getSharedPreferences("settings", MODE_PRIVATE).edit();
        editor.putString("My_Language", lang);
        editor.apply();
    }

    public void loadLocale (){
        SharedPreferences prefs = getSharedPreferences("settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Language", "");
        setLocale(language);
    }
}
