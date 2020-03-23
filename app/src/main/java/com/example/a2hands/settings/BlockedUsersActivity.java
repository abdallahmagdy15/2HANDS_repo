package com.example.a2hands.settings;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.a2hands.R;
import com.example.a2hands.users.UsersFragment;
import com.google.firebase.auth.FirebaseAuth;

public class BlockedUsersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_users);
        Toolbar toolbar = findViewById(R.id.blockedUsersToolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Blocked Users");

        Fragment frg = new UsersFragment();
        Bundle b = new Bundle();
        b.putString("FOR","BLOCKED_USERS");
        b.putString("UID", FirebaseAuth.getInstance().getCurrentUser().getUid());
        frg.setArguments(b);
        FragmentTransaction tr = this.getSupportFragmentManager().beginTransaction()
                .replace(R.id.blockedUsersContainer,frg,null).addToBackStack(null);
        tr.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
