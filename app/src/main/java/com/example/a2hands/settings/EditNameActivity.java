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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.a2hands.ChangeLocale;
import com.example.a2hands.R;
import com.example.a2hands.UserStatus;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditNameActivity extends AppCompatActivity implements TextWatcher {

    TextInputEditText fullName;
    TextInputLayout textInputLayout;

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

        textInputLayout = findViewById(R.id.changeFullNameLayout);

        fullName = findViewById(R.id.editTxt_changeFullName);
        Button saveName = findViewById(R.id.saveNewName_btn);

        fullName.addTextChangedListener(this);

        saveName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> newName = new HashMap<>();
                newName.put("full_name", fullName.getText().toString().trim());

                if(TextUtils.isEmpty(fullName.getText().toString().trim())) {
                    textInputLayout.setError(getResources().getString(R.string.enterYourFullName));
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
