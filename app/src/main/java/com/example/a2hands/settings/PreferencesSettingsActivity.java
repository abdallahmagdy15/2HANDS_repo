package com.example.a2hands.settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.a2hands.ChangeLocale;
import com.example.a2hands.R;

public class PreferencesSettingsActivity extends AppCompatActivity implements View.OnClickListener {
    TextView lang;
    TextView notifi;
    TextView favCats;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeLocale.loadLocale(getBaseContext());
        setContentView(R.layout.activity_preferences_settings);

        Toolbar toolbar = findViewById(R.id.prefSettings_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        lang =findViewById(R.id.prefSettings_editLang);
        notifi=findViewById(R.id.prefSettings_editNotifis);
        favCats=findViewById(R.id.prefSettings_editFavCats);
        lang.setOnClickListener(this);
        notifi.setOnClickListener(this);
        favCats.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.prefSettings_editLang:
                startActivity(new Intent(PreferencesSettingsActivity.this, LanguageActivity.class));
                break;
            case R.id.prefSettings_editNotifis:
                startActivity(new Intent(PreferencesSettingsActivity.this, NotificationActivity.class));
                break;
            case R.id.prefSettings_editFavCats:
//                startActivity(new Intent(PreferencesSettingsActivity.this, AccountSettingsActivity.class));
                break;
        }
    }
}
