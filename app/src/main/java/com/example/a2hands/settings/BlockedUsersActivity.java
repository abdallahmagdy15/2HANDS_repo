package com.example.a2hands.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.a2hands.ChangeLocale;
import com.example.a2hands.R;
import com.example.a2hands.UserStatus;
import com.example.a2hands.users.UsersFragment;
import com.google.firebase.auth.FirebaseAuth;


public class BlockedUsersActivity extends AppCompatActivity {

    String myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeLocale.loadLocale(getBaseContext());
        setContentView(R.layout.activity_blocked_users);
        Toolbar toolbar = findViewById(R.id.blockedUsersToolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.blockedUsers));

        myUid = FirebaseAuth.getInstance().getUid();

        Fragment frg = new UsersFragment();
        Bundle b = new Bundle();
        b.putString("FOR","BLOCKED_USERS");
        b.putString("ID", FirebaseAuth.getInstance().getCurrentUser().getUid());
        frg.setArguments(b);
        FragmentTransaction tr = this.getSupportFragmentManager().beginTransaction()
                .replace(R.id.blockedUsersContainer,frg,null).addToBackStack(null);
        tr.commit();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(BlockedUsersActivity.this, SettingsActivity.class));
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
