package com.example.a2hands.settings;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.a2hands.ChangeLocale;
import com.example.a2hands.R;
import com.example.a2hands.User;
import com.example.a2hands.UserStatus;
import com.example.a2hands.home.HomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {


    TextView settings_account;
    TextView settings_Pref;
    TextView settings_blockedUsers;
    TextView settings_mutedUsers;
    TextView settings_nightMode;
    TextView settings_about;
    String myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeLocale.loadLocale(getBaseContext());
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.settingsAppToolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        settings_account     =findViewById(R.id.settings_account);
        settings_Pref        =findViewById(R.id.settings_preferences);
        settings_blockedUsers=findViewById(R.id.settings_blocked_users);
        settings_mutedUsers  =findViewById(R.id.settings_muted_users);;
        settings_nightMode   =findViewById(R.id.settings_night_mode);;
        settings_about       =findViewById(R.id.settings_about);;
        myUid = FirebaseAuth.getInstance().getUid();

        settings_account       .setOnClickListener(this);
        settings_Pref          .setOnClickListener(this);
        settings_blockedUsers  .setOnClickListener(this);
        settings_mutedUsers    .setOnClickListener(this);
        settings_nightMode     .setOnClickListener(this);
        settings_about         .setOnClickListener(this);


    }// end of onCreate method


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.settings_account:
                startActivity(new Intent(SettingsActivity.this , AccountSettingsActivity.class));
                break;
            case R.id.settings_preferences:
                startActivity(new Intent(SettingsActivity.this , PreferencesSettingsActivity.class));
                break;
            case R.id.settings_blocked_users:
                startActivity(new Intent(SettingsActivity.this , BlockedUsersActivity.class));
                break;
            case R.id.settings_muted_users:
//                startActivity(new Intent(SettingsActivity.this , EditPhoneActivity.class));
                break;
            case R.id.settings_night_mode:
//                startActivity(new Intent(SettingsActivity.this , EditPassActivity.class));
                break;
            case R.id.settings_about:
                startActivity(new Intent(SettingsActivity.this , AboutAppActivity.class));
                break;
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
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
