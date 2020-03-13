package com.example.a2hands.profile;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.View;

import com.example.a2hands.R;

public class FollowersActivity extends AppCompatActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Fragment frg = new userFragment();
        Bundle b = new Bundle();
        b.putString("for","followers");
        b.putString("uid",getIntent().getStringExtra("uid"));
        frg.setArguments(b);
        FragmentTransaction tr = this.getSupportFragmentManager().beginTransaction()
                .replace(R.id.followersContainer,frg,null).addToBackStack(null);
        tr.commit();
    }


}
