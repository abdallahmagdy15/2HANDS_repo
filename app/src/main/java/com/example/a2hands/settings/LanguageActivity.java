package com.example.a2hands.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.a2hands.ChangeLocale;
import com.example.a2hands.LoginActivity;
import com.example.a2hands.R;
import com.example.a2hands.UserStatus;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;


public class LanguageActivity extends AppCompatActivity {

    String myUid;

    private RadioGroup langGroup;
    private Button btnDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //loading locale before setting the content view
        ChangeLocale.loadLocale(getBaseContext());
        setContentView(R.layout.activity_language);

        Toolbar toolbar = findViewById(R.id.languageToolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.language));


        myUid = FirebaseAuth.getInstance().getUid();

        langGroup = findViewById(R.id.languageRadioGroup);
        btnDone = findViewById(R.id.changeLang_btn);

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (langGroup.getCheckedRadioButtonId() == -1){
                    Toast.makeText(LanguageActivity.this, getResources().getString(R.string.selectYourLanguage), Toast.LENGTH_SHORT).show();
                }else{
                    int langID = langGroup.getCheckedRadioButtonId();
                    RadioButton selectedRadioButton = findViewById(langID);

                    if(selectedRadioButton.getText().equals(getResources().getString(R.string.arabic))){
                        setLocale("ar");
                    }else if(selectedRadioButton.getText().equals(getResources().getString(R.string.english))){
                        setLocale("en");
                    }else if(selectedRadioButton.getText().equals(getResources().getString(R.string.french))){
                        setLocale("fr");
                    }else if(selectedRadioButton.getText().equals(getResources().getString(R.string.spanish))){
                        setLocale("es");
                    }else if(selectedRadioButton.getText().equals(getResources().getString(R.string.portuguese))){
                        setLocale("pt");
                    }else if(selectedRadioButton.getText().equals(getResources().getString(R.string.german))){
                        setLocale("de");
                    } else if(selectedRadioButton.getText().equals(getResources().getString(R.string.italian))){
                        setLocale("it");
                    }

                    //clear stack of activities and go to starting activity (login)
                    // which will start homeActivity (cuz user is signed in)
                    // so all activities will be recreated to inflate the layout with the right locale
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    getApplicationContext().startActivity(intent);
                }
            }
        });
    }


    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(LanguageActivity.this, PreferencesSettingsActivity.class));
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);

        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        //save the data to shared preferences
        SharedPreferences.Editor editor = getSharedPreferences("settings", MODE_PRIVATE).edit();
        editor.putString("My_Language", lang);
        editor.apply();

//        Resources resources = getResources();
//        Configuration configuration = resources.getConfiguration();
//        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
//            configuration.setLocale(locale);
//        } else{
//            configuration.locale=locale;
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
//            getApplicationContext().createConfigurationContext(configuration);
//        } else {
//            resources.updateConfiguration(configuration,displayMetrics);
//        }
//        //save the data to shared preferences
//        SharedPreferences.Editor editor = getSharedPreferences("settings", MODE_PRIVATE).edit();
//        editor.putString("My_Language", lang);
//        editor.apply();

    }

    public void loadLocale (){
        SharedPreferences prefs = getSharedPreferences("settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Language", "");
        setLocale(language);
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