package com.example.a2hands.rating;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;

import com.example.a2hands.R;

public class RatingsActivity extends AppCompatActivity  {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ratings);
        Toolbar toolbar = findViewById(R.id.ratingsToolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Ratings");

        Intent i = getIntent();
        String postId  = i.getStringExtra("postId");

        Fragment frg = new RatingFragment();
        Bundle bundle = new Bundle();
        bundle.putString("postId",postId);
        bundle.putString("for","home");

        frg.setArguments(bundle);
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.ratingsContainer,frg);
        ft.commit();
    }


}
