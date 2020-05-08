package com.example.a2hands.signup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.a2hands.ChangeLocale;
import com.example.a2hands.LoginActivity;
import com.example.a2hands.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpSetBioActivity extends AppCompatActivity {

    private TextInputEditText bio;
    private Button skipButton;
    private Button nextButton;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeLocale.loadLocale(getBaseContext());
        setContentView(R.layout.activity_signup_set_bio);

        bio = findViewById(R.id.editTextSignup_bio);
        skipButton = findViewById(R.id.signupBio_skipButton);
        nextButton = findViewById(R.id.signupBio_nextButton);

        bio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.equals("")){
                    skipButton.setVisibility(View.VISIBLE);
                    nextButton.setVisibility(View.INVISIBLE);
                } else {
                    skipButton.setVisibility(View.INVISIBLE);
                    nextButton.setVisibility(View.VISIBLE);
                }
            }
        });


        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpSetBioActivity.this , LoginActivity.class));
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String,Object> addBio = new HashMap<>();
                addBio.put("bio", bio.getText().toString().trim());

                db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).update(addBio)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("addBio", "Done");
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("addBio" , e.toString());
                            }
                        });

                startActivity(new Intent(SignUpSetBioActivity.this , LoginActivity.class));
                Toast.makeText(SignUpSetBioActivity.this, getResources().getString(R.string.checkYourEmailForVerification), Toast.LENGTH_LONG).show();
            }
        });

    }


}
