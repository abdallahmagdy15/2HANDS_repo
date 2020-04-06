package com.example.a2hands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class ForgetPasswordActivity extends AppCompatActivity {

    private EditText resetEmail;

    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_forget_password);

        Button btnResetPassword = findViewById(R.id.btnResetPassword);
        resetEmail = findViewById(R.id.forgotPassEmail);

        fAuth = FirebaseAuth.getInstance();


        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(resetEmail.getText().toString().trim())){
                    resetEmail.setError("Enter your Email");
                }else if(! Patterns.EMAIL_ADDRESS.matcher(resetEmail.getText().toString().trim()).matches()){
                    resetEmail.setError("Email is not valid");
                }else{
                    fAuth.sendPasswordResetEmail(resetEmail.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(ForgetPasswordActivity.this,
                                                "Check your Email to reset your password",Toast.LENGTH_LONG).show();
                                    }else{
                                        Toast.makeText(ForgetPasswordActivity.this,
                                                task.getException().getMessage(),Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }

            }
        });

    }


    //for changing app language
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
    }

    public void loadLocale (){
        SharedPreferences prefs = getSharedPreferences("settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Language", "");
        setLocale(language);
    }
}
