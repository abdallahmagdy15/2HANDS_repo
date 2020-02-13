package com.example.a2hands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static com.google.firebase.auth.FirebaseAuth.getInstance;

public class CreatePost extends AppCompatActivity {

    Button backBtn;
    Button submitPost;
    Spinner catSpinner;
    EditText createdPostText;
    Switch createdPostIsAnon;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        backBtn = findViewById(R.id.backBtn);
        submitPost = findViewById(R.id.submitPost);
        catSpinner = findViewById(R.id.catSpinner);
        createdPostText = findViewById(R.id.createdPostText);
        createdPostIsAnon = findViewById(R.id.createdPostIsAnon);

        db = FirebaseFirestore.getInstance();


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        submitPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
            }
        });
    }

    public void submitPost() {
        Post post = new Post();
        post.category = catSpinner.getSelectedItem().toString();
        post.content_text = createdPostText.getText().toString();
        post.location = "Egypt";
        post.visibility = !createdPostIsAnon.isChecked();
        post.state = true;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Add a new document with a generated ID
        db.collection("users/"+uid+"/posts/")
                .add(post)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(CreatePost.this, "Post created successfully!", Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

}
