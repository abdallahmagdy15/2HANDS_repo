package com.example.a2hands.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a2hands.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;


import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    Toolbar toolbar;
    ImageView cover;
    ImageView addCover;
    ImageView addPic;
    CircleImageView pic;
    TextView saveBtn;
    EditText nameEditor;
    EditText jobEditor;
    EditText bioEditor;

    String uid;
    Uri coverUri;
    Uri picUri;
    String name;
    String job;
    String bio;
    String coverPath;
    String picPath;

    StorageReference mStorageRef;
    StorageReference picRef;
    StorageReference coverRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        name     = getIntent().getStringExtra("NAME");
        job      = getIntent().getStringExtra("JOB");
        bio      = getIntent().getStringExtra("BIO");
        coverPath= getIntent().getStringExtra("COVER_PATH");
        picPath  = getIntent().getStringExtra("PIC_PATH");
        uid= getIntent().getStringExtra("UID");

        toolbar = findViewById(R.id.editProfile_toolbar);
        cover = findViewById(R.id.editProfile_cover);
        pic = findViewById(R.id.editProfile_pic);
        addCover = findViewById(R.id.editProfile_addCover);
        addPic = findViewById(R.id.editProfile_addPic);
        saveBtn = findViewById(R.id.editProfile_save);
        nameEditor = findViewById(R.id.editProfile_name);
        jobEditor = findViewById(R.id.editProfile_job);
        bioEditor = findViewById(R.id.editProfile_bio);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        picRef = FirebaseStorage.getInstance().getReference("Profile_Pics").child(uid);
        coverRef = FirebaseStorage.getInstance().getReference("Profile_Covers").child(uid);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        nameEditor.setText(name);
        jobEditor.setText(job);
        bioEditor.setText(bio);

        if(picPath!= null)
            FirebaseStorage.getInstance().getReference().child("/Profile_Pics/"+uid+"/"+picPath)
                    .getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    Picasso.get().load(task.getResult()).into(pic);
                }
            });
        if(coverPath != null)
            FirebaseStorage.getInstance().getReference().child("/Profile_Covers/"+uid+"/"+coverPath)
                    .getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    Picasso.get().load(task.getResult()).into(cover);
                }
            });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBtn.setEnabled(false);
                saveBtn.setTextColor(getResources().getColor(R.color.colorDisabled));
                updateProfile();
            }
        });
        addCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePhoto(1);
            }
        });
        addPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePhoto(2);
            }
        });
    }

    void updateProfile(){
        if(picUri != null)
            uploadPhoto(picRef,picUri,2);
        else if(coverUri != null)
            uploadPhoto(coverRef,coverUri,1);
        else
            saveData();
    }

    void uploadPhoto(StorageReference ref,Uri uri,int code){
        String newName = System.currentTimeMillis()+"."+getExtension(uri);
        if(code==1)
            coverPath = newName;
        else if(code==2)
            picPath = newName;

        ref.child(newName)
                .putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                if(code==2)
                    if(coverUri!=null)
                        uploadPhoto(coverRef,coverUri,1);
                    else
                        saveData();
                else if(code==1)
                    saveData();
            }
        });
    }

    String getExtension(Uri uri){
        ContentResolver cr= getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }

    public void saveData(){
        FirebaseFirestore.getInstance().collection("users").document(uid)
                .update("full_name",nameEditor.getText().toString(),
                        "job_title",jobEditor.getText().toString(),
                        "bio",bioEditor.getText().toString(),
                        "profile_pic",picPath,
                        "profile_cover",coverPath)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(EditProfileActivity.this,"Profile updated!",Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }
                });
    }

    void choosePhoto(int reqCode){
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i,reqCode);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode ==RESULT_OK && data != null){
            if(requestCode == 1){
                coverUri = data.getData();
                cover.setImageURI(coverUri);
            }
            else if(requestCode == 2 ) {
                picUri = data.getData();
                pic.setImageURI(picUri);
            }
        }
    }
}
