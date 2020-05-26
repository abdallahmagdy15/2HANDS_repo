package com.example.a2hands.settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.a2hands.ChangeLocale;
import com.example.a2hands.R;
import com.google.firebase.auth.FirebaseAuth;

public class AccountSettingsActivity extends AppCompatActivity implements View.OnClickListener {

    TextView accountSettings_editName;
    TextView accountSettings_editUsername;
    TextView accountSettings_editEmail;
    TextView accountSettings_editPhone;
    TextView accountSettings_editCountry;
    TextView accountSettings_editPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeLocale.loadLocale(getBaseContext());
        setContentView(R.layout.activity_account_settings);

        Toolbar toolbar = findViewById(R.id.accountSettings_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        accountSettings_editName = findViewById(R.id.accountSettings_editName);
        accountSettings_editUsername = findViewById(R.id.accountSettings_editUsername);
        accountSettings_editEmail = findViewById(R.id.accountSettings_editEmail);
        accountSettings_editPhone = findViewById(R.id.accountSettings_editPhone);;
        accountSettings_editCountry = findViewById(R.id.accountSettings_editCountry);;
        accountSettings_editPassword = findViewById(R.id.accountSettings_editPassword);;

        accountSettings_editName.setOnClickListener(this);
        accountSettings_editUsername.setOnClickListener(this);
        accountSettings_editEmail.setOnClickListener(this);
        accountSettings_editPhone.setOnClickListener(this);
        accountSettings_editCountry.setOnClickListener(this);
        accountSettings_editPassword.setOnClickListener(this);

    }// end of onCreate method


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.accountSettings_editName:
                startActivity(new Intent(AccountSettingsActivity.this , EditNameActivity.class));
                break;
            case R.id.accountSettings_editUsername:
                startActivity(new Intent(AccountSettingsActivity.this , EditUserNameActivity.class));
                break;
            case R.id.accountSettings_editEmail:
                startActivity(new Intent(AccountSettingsActivity.this , EditEmailActivity.class));
                break;
            case R.id.accountSettings_editPhone:
                startActivity(new Intent(AccountSettingsActivity.this , EditPhoneActivity.class));
                break;
            case R.id.accountSettings_editCountry:
                startActivity(new Intent(AccountSettingsActivity.this , EditCountryActivity.class));
                break;
            case R.id.accountSettings_editPassword:
                startActivity(new Intent(AccountSettingsActivity.this , EditPassActivity.class));
                break;
        }
    }
}
