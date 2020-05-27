package com.example.a2hands.profile;

import android.os.Bundle;

import com.example.a2hands.ChangeLocale;
import com.example.a2hands.UserStatus;
import com.example.a2hands.users.UsersFragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.a2hands.R;
import com.google.firebase.auth.FirebaseAuth;


public class FollowingsActivity extends AppCompatActivity {

    String myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeLocale.loadLocale(getBaseContext());
        setContentView(R.layout.activity_followings);

        Toolbar toolbar = findViewById(R.id.followings_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        myUid = FirebaseAuth.getInstance().getUid();

        Fragment frg = new UsersFragment();
        Bundle b = new Bundle();
        b.putString("FOR","FOLLOWINGS");
        b.putString("ID",getIntent().getStringExtra("UID"));
        frg.setArguments(b);
        FragmentTransaction tr = this.getSupportFragmentManager().beginTransaction()
                .replace(R.id.followingsContainer,frg,null);
        tr.commit();
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
