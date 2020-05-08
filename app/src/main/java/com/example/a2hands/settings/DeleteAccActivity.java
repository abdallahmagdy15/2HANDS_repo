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


public class DeleteAccActivity extends AppCompatActivity implements TextWatcher {

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private TextInputLayout currPassLayout;
    private TextInputEditText currentPass;
    private Button deleteAcc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeLocale.loadLocale(getBaseContext());
        setContentView(R.layout.activity_delete_acc);

        Toolbar toolbar = findViewById(R.id.deleteAccToolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.deleteAccount));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        currPassLayout = findViewById(R.id.deleteAcc_currentPassLayout);
        currentPass = findViewById(R.id.editTxt_deleteAcc_currentPass);
        deleteAcc = findViewById(R.id.deleteEmail_btnReal);

        currentPass.addTextChangedListener(this);

        deleteAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(currentPass.getText().toString())){
                    currPassLayout.setError(getResources().getString(R.string.enterYourPassword));
                } else {
                    AuthCredential credential = EmailAuthProvider
                            .getCredential(user.getEmail(), currentPass.getText().toString());
                    // Prompt the user to re-provide their sign-in credentials
                    user.reauthenticate(credential)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("", "User re-authenticated.");
                                    //----------------Code for Deleting Account----------\\
                                    user.delete()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d("deleteAcc", "Done");
                                                        Toast.makeText(DeleteAccActivity.this, "Account Deleted", Toast.LENGTH_LONG).show();
                                                        startActivity(new Intent(DeleteAccActivity.this , LoginActivity.class));
                                                        finish();
                                                    }
                                                }
                                            });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("deleteAcc", e.toString());
                            currPassLayout.setError(getResources().getString(R.string.wrongPassword));
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
            currPassLayout.setError(null);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
