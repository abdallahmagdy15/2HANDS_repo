package com.example.a2hands.settingsPackage;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.a2hands.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EditEmailActivity extends AppCompatActivity {

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private EditText email, currentPass;
    private Button saveEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_email);

        email = findViewById(R.id.editTxt_changeEmail);
        currentPass = findViewById(R.id.editTxt_changeEmail_currentPass);
        saveEmail = findViewById(R.id.saveNewEmail_btn);

        saveEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(email.getText().toString().trim())){
                    email.setError("Enter your Email");
                    //Toast.makeText(RegisterActivity.this, "Empty Email or Password!", Toast.LENGTH_SHORT).show();
                } else if(! Patterns.EMAIL_ADDRESS.matcher(email.getText().toString().trim()).matches()){
                    email.setError("Email is not valid");
                } else if(TextUtils.isEmpty(currentPass.getText().toString())){
                    currentPass.setError("Enter your Password");
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
                                    currentPass.setError("Wrong Password");
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
}
