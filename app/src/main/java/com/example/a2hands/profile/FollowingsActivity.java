package com.example.a2hands.profile;

import android.os.Bundle;

import com.example.a2hands.Users.UsersFragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.a2hands.R;

public class FollowingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Fragment frg = new UsersFragment();
        Bundle b = new Bundle();
        b.putString("FOR","FOLLOWINGS");
        b.putString("UID",getIntent().getStringExtra("UID"));
        frg.setArguments(b);
        FragmentTransaction tr = this.getSupportFragmentManager().beginTransaction()
                .replace(R.id.followingsContainer,frg,null).addToBackStack(null);
        tr.commit();
    }


}
