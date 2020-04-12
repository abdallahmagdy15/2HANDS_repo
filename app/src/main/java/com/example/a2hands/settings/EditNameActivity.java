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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditNameActivity extends AppCompatActivity {

    EditText fullName;

    //firebase
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeLocale.loadLocale(getBaseContext());
        setContentView(R.layout.activity_edit_name);

        Toolbar toolbar = findViewById(R.id.editNameToolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.editName));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        fullName = findViewById(R.id.editTxt_changeFullName);
        Button saveName = findViewById(R.id.saveNewName_btn);



        saveName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> newName = new HashMap<>();
                newName.put("full_name", fullName.getText().toString().trim());

                if(TextUtils.isEmpty(fullName.getText().toString().trim())) {
                    fullName.setError("Enter your First Name");
                }else {
                    db.collection("users").document(user.getUid()).update(newName)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("saveNewName", "Done");
                                    startActivity(new Intent(EditNameActivity.this , SettingsActivity.class));
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("saveNewName", e.toString());
                                    startActivity(new Intent(EditNameActivity.this , SettingsActivity.class));
                                    finish();
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
