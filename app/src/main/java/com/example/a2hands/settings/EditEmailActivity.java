package com.example.a2hands.settings;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.a2hands.ChangeLocale;
import com.example.a2hands.R;
import com.example.a2hands.UserStatus;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class EditEmailActivity extends AppCompatActivity implements TextWatcher {

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private TextInputLayout textInputLayout;
    private TextInputEditText email, currentPass;
    private Button saveEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeLocale.loadLocale(getBaseContext());
        setContentView(R.layout.activity_edit_email);

        Toolbar toolbar = findViewById(R.id.editEmailToolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.editEmail));


        textInputLayout = findViewById(R.id.settingsChangeEmailLayout);

        email = findViewById(R.id.editTxt_changeEmail);
        currentPass = findViewById(R.id.editTxt_changeEmail_currentPass);
        saveEmail = findViewById(R.id.saveNewEmail_btn);

        email.addTextChangedListener(this);
        currentPass.addTextChangedListener(this);

        saveEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(email.getText().toString().trim())){
                    textInputLayout = findViewById(R.id.settingsChangeEmailLayout);
                    textInputLayout.setError(getResources().getString(R.string.enterYourEmail));
                } else if(! Patterns.EMAIL_ADDRESS.matcher(email.getText().toString().trim()).matches()){
                    textInputLayout = findViewById(R.id.settingsChangeEmailLayout);
                    textInputLayout.setError(getResources().getString(R.string.emailIsNotValid));
                } else if(TextUtils.isEmpty(currentPass.getText().toString())){
                    textInputLayout = findViewById(R.id.changeEmail_currentPassLayout);
                    textInputLayout.setError(getResources().getString(R.string.enterYourPassword));
                } else {
                    AuthCredential credential = EmailAuthProvider
                            .getCredential(user.getEmail(), currentPass.getText().toString()); // Current Login Credentials \\
                    // Prompt the user to re-provide their sign-in credentials
                    user.reauthenticate(credential)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("", "User re-authenticated.");
                                    //----------------Code for Changing Email Address----------\\
                                    user.updateEmail(email.getText().toString().trim())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d("saveNewEmail", "Done");
                                                        startActivity(new Intent(EditEmailActivity.this , SettingsActivity.class));
                                                        finish();
                                                    }
                                                }
                                            });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("saveNewEmail", e.toString());
                                    textInputLayout = findViewById(R.id.changeEmail_currentPassLayout);
                                    textInputLayout.setError(getResources().getString(R.string.wrongPassword));
                                }
                            });

                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        UserStatus.updateOnlineStatus(true, user.getUid());
        super.onResume();
    }

    @Override
    protected void onStop()
    {
        if(UserStatus.isAppIsInBackground(getApplicationContext())){
            UserStatus.updateOnlineStatus(false, user.getUid());
        }
        super.onStop();
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(count>0)
            textInputLayout.setError(null);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
