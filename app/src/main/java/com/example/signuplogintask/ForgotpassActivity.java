package com.example.signuplogintask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

public class ForgotpassActivity extends AppCompatActivity {

    private EditText resetEmail;

    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpass);

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
                                        Toast.makeText(ForgotpassActivity.this,
                                                "Check your Email to reset your password",Toast.LENGTH_LONG).show();
                                    }else{
                                        Toast.makeText(ForgotpassActivity.this,
                                                task.getException().getMessage(),Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }

            }
        });

    }
}
