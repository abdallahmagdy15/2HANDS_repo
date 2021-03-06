package com.example.a2hands.rating;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;

import com.example.a2hands.ChangeLocale;
import com.example.a2hands.R;
import com.example.a2hands.UserStatus;
import com.google.firebase.auth.FirebaseAuth;


public class RatingsActivity extends AppCompatActivity  {

    String myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeLocale.loadLocale(getBaseContext());
        setContentView(R.layout.activity_ratings);

        Toolbar toolbar = findViewById(R.id.ratingsToolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.ratings));

        myUid = FirebaseAuth.getInstance().getUid();

        Intent i = getIntent();
        String postId  = i.getStringExtra("postId");

        Fragment frg = new RatingFragment();
        Bundle bundle = new Bundle();
        bundle.putString("postId",postId);
        bundle.putString("FOR","HOME");

        frg.setArguments(bundle);
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.ratingsContainer,frg);
        ft.commit();
    }

    @Override
    protected void onResume() {
        UserStatus.updateOnlineStatus(true, myUid);
        super.onResume();
    }

    @Override
    protected void onStop()
    {
        if(UserStatus.isAppIsInBackground(getApplicationContext())){
            UserStatus.updateOnlineStatus(false, myUid);
        }
        super.onStop();
    }


}
