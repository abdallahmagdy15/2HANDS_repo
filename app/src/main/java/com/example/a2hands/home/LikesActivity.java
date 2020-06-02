package com.example.a2hands.home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.widget.FrameLayout;

import com.example.a2hands.ChangeLocale;
import com.example.a2hands.R;
import com.example.a2hands.UserStatus;
import com.example.a2hands.users.UsersFragment;
import com.google.firebase.auth.FirebaseAuth;


public class LikesActivity extends AppCompatActivity {

    String myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeLocale.loadLocale(getBaseContext());
        setContentView(R.layout.activity_likes);

        Toolbar toolbar = findViewById(R.id.likesToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        myUid = FirebaseAuth.getInstance().getUid();
        String postId = getIntent().getStringExtra("POST_ID");

        Fragment frg = new UsersFragment();
        Bundle bundle = new Bundle();
        bundle.putString("ID", postId);
        bundle.putString("FOR", "LIKES");
        frg.setArguments(bundle);
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.likesContainer,frg);
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
