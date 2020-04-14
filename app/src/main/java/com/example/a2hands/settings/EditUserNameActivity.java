package com.example.a2hands.settings;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditUserNameActivity extends AppCompatActivity {

    EditText userName;

    //firebase
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final String USERNAME_PATTERN = "^(?=.{8,20}$)(?![_.])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])$";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeLocale.loadLocale(getBaseContext());
        setContentView(R.layout.activity_edit_user_name);

        Toolbar toolbar = findViewById(R.id.editUserNameToolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.editUserName));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        userName = findViewById(R.id.editTxt_changeUserName);
        Button saveUserName = findViewById(R.id.saveNewUserName_btn);



        saveUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(userName.getText().toString().trim())) {
                    userName.setError("Enter your new Username");
                }else {
                    checkIfValidUsernameAndUpdate(userName.getText().toString().trim());
                }

            }
        });

    }


    //username validation
    public void checkIfValidUsernameAndUpdate(final String username){

        final Query usernameQuery = FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("user_name", username);

        Pattern usernamePattern;
        Matcher usernameMatcher;
        usernamePattern = Pattern.compile(USERNAME_PATTERN);
        usernameMatcher = usernamePattern.matcher(username);

        if(usernameMatcher.matches()){
            usernameQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()){
                        for (DocumentSnapshot ds: task.getResult()){
                            String userNames = ds.getString("user_name");
                            if (username.equals(userNames)) {
                                userName.setError("Username is already used");
                            }
                        }
                    }
                    if (task.getResult().size() == 0){
                        Map<String, Object> newUserName = new HashMap<>();
                        newUserName.put("user_name", userName.getText().toString().trim());

                        db.collection("users").document(user.getUid()).update(newUserName)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("saveNewUserName", "Done");
                                        startActivity(new Intent(EditUserNameActivity.this , SettingsActivity.class));
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("saveNewUserName", e.toString());
                                        startActivity(new Intent(EditUserNameActivity.this , SettingsActivity.class));
                                        finish();
                                    }
                                });
                    }
                }
            });
        } else {
            userName.setError("Username is not valid");
        }

    }// end of checkIfValidUsername


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


}
