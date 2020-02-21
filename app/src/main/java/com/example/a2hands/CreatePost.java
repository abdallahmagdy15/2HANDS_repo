package com.example.a2hands;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.example.a2hands.homePackage.PostFragment;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;


public class CreatePost extends AppCompatActivity {

    Button backBtn;
    Button submitPost;
    Spinner catSpinner;
    EditText createdPostText;
    Switch createdPostIsAnon;
    DatabaseReference db;
    ImageView ownerPic;

    //creating images and camera
    User user;
    Uri imageUri;
    String imageUrl = "";
    StorageTask uploadTask;
    StorageReference storageReference;
    private static final int PICK_IMAGE_REQUEST = 1;
    ImageView selectedImage;
    Button add_image;
    Post post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        backBtn = findViewById(R.id.backBtn);
        submitPost = findViewById(R.id.submitPost);
        catSpinner = findViewById(R.id.catSpinner);
        createdPostText = findViewById(R.id.createdPostText);
        createdPostIsAnon = findViewById(R.id.createdPostIsAnon);
        db = FirebaseDatabase.getInstance().getReference();
        ownerPic = findViewById(R.id.postOwnerPic);

        //addimage decleartion
        storageReference = FirebaseStorage.getInstance().getReference("posts");
        selectedImage = findViewById(R.id.selectedImage);
        add_image = findViewById(R.id.add_image);
        add_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        final String uid = getIntent().getStringExtra("uid");

        FirebaseFirestore.getInstance().collection("users/").document(uid)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                user = task.getResult().toObject(User.class);
                FirebaseStorage.getInstance().getReference().child("Profile_Pics/"+uid+"/"+user.profile_pic).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri.toString()).into(ownerPic);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });
            }
        });

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

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    public void submitPost() {
        post = new Post();
        post.category = catSpinner.getSelectedItem().toString();
        post.content_text = createdPostText.getText().toString();
        post.location = "Egypt";
        post.visibility = !createdPostIsAnon.isChecked();
        post.state = true;
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        post.postOwner = user.first_name+" "+user.last_name;
        post.user_id = uid;
        post.profile_pic = user.profile_pic;
        if (imageUri != null){
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + ".jpg");
            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if(!task.isSuccessful()){
                        task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        imageUrl = downloadUri.toString();
                        post.image = imageUrl;
                        CollectionReference ref =FirebaseFirestore.getInstance().collection("/posts");
                        ref.document(ref.document().getId())
                                .set(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(CreatePost.this, "Post created successfully!", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        });
                        startActivity(new Intent(CreatePost.this, MainActivity.class));
                        finish();
                    }
                    else {
                        Toast.makeText(CreatePost.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CreatePost.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else{
            CollectionReference ref =FirebaseFirestore.getInstance().collection("/posts");
            ref.document(ref.document().getId())
                    .set(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(CreatePost.this, "Post created successfully!", Toast.LENGTH_LONG).show();
                    finish();
                }
            });
            startActivity(new Intent(CreatePost.this, MainActivity.class));
            finish();
        }



        /*db.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User u = dataSnapshot.getValue(User.class);
                post.postOwner = u.first_name+" "+u.last_name;
                post.user_id = uid;
                post.profile_pic = u.profile_pic;
                db.child("posts").setValue(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(CreatePost.this, "Post created successfully!", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/
        // Add a new document with a generated ID
        /*PostFragment.getUser(new Callback() {
            @Override
            public void callbackUser(User u) {
                post.postOwner = u.first_name+" "+u.last_name;
                post.user_id = uid;
                post.profile_pic = u.profile_pic;
                CollectionReference ref =FirebaseFirestore.getInstance().collection("/posts");
                ref.document(ref.document().getId())
                        .set(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(CreatePost.this, "Post created successfully!", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
            }
        },uid);*/

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK){
            imageUri = data.getData();
            selectedImage.setImageURI(imageUri);
        }
    }
}
