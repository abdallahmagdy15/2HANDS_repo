package com.example.a2hands.settings;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.a2hands.ChangeLocale;
import com.example.a2hands.LoginActivity;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditPassActivity extends AppCompatActivity implements TextWatcher {

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private TextInputLayout textInputLayout;
    private TextInputEditText currentPass, newPass,  confirmPass;
    private Button savePass;

    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeLocale.loadLocale(getBaseContext());
        setContentView(R.layout.activity_edit_pass);

        Toolbar toolbar = findViewById(R.id.editPassToolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.editPassword));


        textInputLayout = findViewById(R.id.changePass_currentLayout);

        currentPass = findViewById(R.id.editTxt_changePass_current);
        newPass = findViewById(R.id.editTxt_changePass_new);
        confirmPass = findViewById(R.id.editTxt_changePass_confirm);

        savePass = findViewById(R.id.saveNewPass_btn);

        currentPass.addTextChangedListener(this);
        newPass.addTextChangedListener(this);
        confirmPass.addTextChangedListener(this);

        savePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(currentPass.getText().toString())){
                    textInputLayout = findViewById(R.id.changePass_currentLayout);
                    textInputLayout.setError(getResources().getString(R.string.enterYourPassword));
                } else if(newPass.getText().toString().length() < 8 ){
                    if(TextUtils.isEmpty(newPass.getText().toString())){
                        textInputLayout = findViewById(R.id.changePass_newLayout);
                        textInputLayout.setError(getResources().getString(R.string.enterYourPassword));
                    }else{
                        textInputLayout = findViewById(R.id.changePass_newLayout);
                        textInputLayout.setError(getResources().getString(R.string.tooShortPassword));
                    }
                } else if(TextUtils.isEmpty(confirmPass.getText().toString())){
                    textInputLayout = findViewById(R.id.changePass_confirmLayout);
                    textInputLayout.setError(getResources().getString(R.string.enterYourConfirmPassword));
                } else if(! confirmPass.getText().toString().equals(newPass.getText().toString())){
                    textInputLayout = findViewById(R.id.changePass_confirmLayout);
                    textInputLayout.setError(getResources().getString(R.string.passwordDoesnotMatch));
                } else if (! isValidPassword(confirmPass.getText().toString())){
                    textInputLayout = findViewById(R.id.changePass_confirmLayout);
                    textInputLayout.setError(getResources().getString(R.string.atLeast1Capital1Number1SpecialChar));
                } else {
                    AuthCredential credential = EmailAuthProvider
                            .getCredential(user.getEmail(), currentPass.getText().toString()); // Current Login Credentials \\
                    // Prompt the user to re-provide their sign-in credentials
                    user.reauthenticate(credential)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("", "User re-authenticated.");
                                    //----------------Code for Changing Password----------\\
                                    user.updatePassword(newPass.getText().toString().trim())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d("saveNewPass", "Done");
                                                        FirebaseAuth.getInstance().signOut();
                                                        startActivity(new Intent(EditPassActivity.this , LoginActivity.class));
                                                        finish();
                                                    }
                                                }
                                            });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("saveNewEmail", e.toString());
                            textInputLayout = findViewById(R.id.changePass_currentLayout);
                            textInputLayout.setError(getResources().getString(R.string.wrongPassword));
                        }
                    });
                }
            }
        });

    }

    //password validation
    public static boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();
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
