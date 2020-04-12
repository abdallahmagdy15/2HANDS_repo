package com.example.a2hands.home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.widget.FrameLayout;

import com.example.a2hands.ChangeLocale;
import com.example.a2hands.R;
import com.example.a2hands.users.UsersFragment;


public class LikesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeLocale.loadLocale(getBaseContext());
        setContentView(R.layout.activity_likes);

        Toolbar toolbar = findViewById(R.id.likesToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        String postId = getIntent().getStringExtra("POST_ID");

        FrameLayout likesContainer = findViewById(R.id.likesContainer);

        Fragment frg = new UsersFragment();
        Bundle bundle = new Bundle();
        bundle.putString("ID", postId);
        bundle.putString("FOR", "LIKES");
        frg.setArguments(bundle);
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.likesContainer,frg).addToBackStack(null);
        ft.commit();
    }


}
