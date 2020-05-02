package com.example.a2hands.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a2hands.ChangeLocale;
import com.example.a2hands.R;
import com.example.a2hands.UserStatus;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    Uri profileSourceUri;
    Uri profileDestinationUri;
    Uri coverSourceUri;
    Uri coverDestinationUri;
    String name;
    String job;
    String bio;
    String coverPath;
    String picPath;

    StorageReference mStorageRef;
    StorageReference picRef;
    StorageReference coverRef;
    UploadTask uploadTask;

    String myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeLocale.loadLocale(getBaseContext());
        setContentView(R.layout.activity_edit_profile);

        name     = getIntent().getStringExtra("NAME");
        job      = getIntent().getStringExtra("JOB");
        bio      = getIntent().getStringExtra("BIO");
        coverPath= getIntent().getStringExtra("COVER_PATH");
        picPath  = getIntent().getStringExtra("PIC_PATH");
        uid      = getIntent().getStringExtra("UID");

        myUid = FirebaseAuth.getInstance().getUid();

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
            Picasso.get().load(Uri.parse(picPath)).into(pic);
        if(coverPath != null)
            Picasso.get().load(Uri.parse(coverPath)).into(cover);


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

    void choosePhoto(int reqCode){
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");  // 1
        i.addCategory(Intent.CATEGORY_OPENABLE);  // 2
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String[] mimeTypes = new String[]{"image/jpeg", "image/png"};  // 3
            i.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        }
        startActivityForResult(i,reqCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode ==RESULT_OK && data != null && data.getData() != null){
            if(requestCode == 1){
                coverSourceUri = data.getData();
                try {
                    File file = createImageFile();
                    coverDestinationUri = Uri.fromFile(file);  // 3
                } catch (IOException e) {
                    e.printStackTrace();
                }

                openCropActivity(coverSourceUri, coverDestinationUri,2,1);
            }
            else if(requestCode == 2 ) {
                profileSourceUri = data.getData();
                try {
                    File file = createImageFile();
                    profileDestinationUri = Uri.fromFile(file);  // 3
                } catch (IOException e) {
                    e.printStackTrace();
                }
                openCropActivity(profileSourceUri, profileDestinationUri,1 ,1);

            }
        } else if (resultCode == RESULT_OK && data != null && requestCode == UCrop.REQUEST_CROP) {
            Uri imgUri = UCrop.getOutput(data);

            if(profileDestinationUri != null && profileDestinationUri.equals(imgUri)){
                pic.setImageURI(imgUri);

            }if(coverDestinationUri != null && coverDestinationUri.equals(imgUri)){
                cover.setImageURI(imgUri);
            }
        }

    }

    private void openCropActivity(Uri sourceUri, Uri destinationUri, int ratioX, int ratioY) {
        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        options.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        options.setToolbarColor(getResources().getColor(R.color.colorAccent));
        options.setActiveControlsWidgetColor(getResources().getColor(R.color.colorAccent));

        UCrop.of(sourceUri, destinationUri)
                .withOptions(options)
                .withAspectRatio(ratioX, ratioY)
                .withMaxResultSize(ratioX*480, ratioY*480)
                .start(EditProfileActivity.this);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = "JPEG_" + System.currentTimeMillis() + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",   /* suffix */
                storageDir      /* directory */
        );

        return image;
    }


    void updateProfile(){
        //upload profile pic or cover if found
        if(profileDestinationUri != null)
            uploadPhoto(picRef,profileDestinationUri,2);
        else if(coverDestinationUri != null)
            uploadPhoto(coverRef,coverDestinationUri,1);
        else
            saveData();
    }

    void uploadPhoto(StorageReference ref,Uri uri,int code){
        String newName = myUid +".png";

        uploadTask = ref.child(newName).putFile(uri);
        uploadTask.continueWithTask(new Continuation() {
            @Override
            public Object then(@NonNull Task task) throws Exception {
                if(!task.isSuccessful()){
                    task.getException();
                }
                return ref.child(newName).getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    Map<String,Object> newPic = new HashMap<>();

                    if(code == 2){
                        picPath = task.getResult().toString();
                        newPic.put("profile_pic", task.getResult().toString());
                        FirebaseFirestore.getInstance().collection("users").document(myUid)
                                .update(newPic);

                        //upload profile cover if found after uploading profile pic
                        if(coverDestinationUri != null)
                            uploadPhoto(coverRef,coverDestinationUri,1);
                        else
                            //save bio, name and job after uploading photos
                            saveData();
                    } else if(code == 1){
                        coverPath = task.getResult().toString();
                        newPic.put("profile_cover", task.getResult().toString());
                        FirebaseFirestore.getInstance().collection("users").document(myUid)
                                .update(newPic);

                        //save bio, name and job after uploading photos
                        saveData();
                    }
                }else {
                    Toast.makeText(EditProfileActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    public void saveData(){
        FirebaseFirestore.getInstance().collection("users").document(uid)
                .update("full_name",nameEditor.getText().toString(),
                        "job_title",jobEditor.getText().toString(),
                        "bio",bioEditor.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(EditProfileActivity.this,"Profile updated!",Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                });
    }


    @Override
    protected void onResume() {
        UserStatus.updateOnlineStatus(true, myUid);
        super.onResume();
    }

    @Override
    protected void onStop()
    {
        if(UserStatus.isAppIsInBackground(getApplicationContext())){
            UserStatus.updateOnlineStatus(false, myUid);
        }
        super.onStop();
    }

}
