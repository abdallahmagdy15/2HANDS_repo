package com.example.a2hands.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a2hands.ImagePreview;
import com.example.a2hands.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.core.FirestoreClient;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    Toolbar toolbar;
    ImageView cover;
    ImageView addCover;
    CircleImageView addPic;
    TextView saveBtn;
    EditText nameEditor;
    EditText jobEditor;
    EditText bioEditor;

    String uid;
    String coverNewUri;
    String picNewUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        toolbar = findViewById(R.id.editProfile_toolbar);
        cover = findViewById(R.id.editProfile_cover);
        addCover = findViewById(R.id.editProfile_addCover);
        addPic = findViewById(R.id.editProfile_addPic);
        saveBtn = findViewById(R.id.editProfile_save);
        nameEditor = findViewById(R.id.editProfile_name);
        jobEditor = findViewById(R.id.editProfile_job);
        bioEditor = findViewById(R.id.editProfile_bio);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        String name= getIntent().getStringExtra("NAME");
        String job= getIntent().getStringExtra("JOB");
        String bio= getIntent().getStringExtra("BIO");
        String coverPath= getIntent().getStringExtra("COVER_PATH");
        String picPath= getIntent().getStringExtra("PIC_PATH");

        uid= getIntent().getStringExtra("UID");

        nameEditor.setText(name);
        jobEditor.setText(job);
        bioEditor.setText(bio);

        //Picasso.get().load(Uri.parse(coverPath)).into(cover);
        //Picasso.get().load(Uri.parse(coverPath)).into();

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });
    }

    public void updateProfile(){
        FirebaseFirestore.getInstance().collection("users").document(uid)
                .update("full_name",nameEditor.getText().toString(),
                        "job_title",jobEditor.getText().toString(),
                        "bio",bioEditor.getText().toString(),
                        "profile_pic",picNewUri,
                        "profile_cover",coverNewUri)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        saveBtn.setEnabled(false);
                        saveBtn.setTextColor(R.color.colorDisabled);
                        Toast.makeText(EditProfileActivity.this,"Profile updated!",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }
}
