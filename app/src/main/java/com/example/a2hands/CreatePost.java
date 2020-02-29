package com.example.a2hands;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.a2hands.homePackage.PostsPackage.Post;
import com.example.a2hands.homePackage.PostsPackage.PostFragment;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


import id.zelory.compressor.Compressor;

public class CreatePost extends AppCompatActivity {

    Button backBtn;
    Button submitPost;
    Spinner catSpinner;
    EditText createdPostText;
    Switch createdPostIsAnon;
    DatabaseReference db;
    ImageView ownerPic;
    final Post post = new Post();
    ListView mentionSuggestionsList;
    final ArrayList<String> mentionsIds = new ArrayList<>() ;

    //creating images and camera
    Uri imageUri;
    String imageUrl = "";
    StorageTask uploadTask;
    StorageReference storageReference;
    private static final int PICK_IMAGE_REQUEST = 1;
    Bitmap bitmap;
    ImageView selectedImage;
    ImageView add_image;


    //cameraRequest
    //camera request
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    ImageView camerabtn;
    String currentPhotoPath;

    //uploadVideo
    public static final int VIDEO_REQUEST_CODE = 3;
    VideoView selectedVideo ;
    ImageView videobtn;
    Uri videoUri;
    String videoUrl = "";
    MediaController mc;
    String videoName;



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
        mentionSuggestionsList= findViewById(R.id.mentionSuggestionsList);

        final String uid = getIntent().getStringExtra("uid");

        //get my profile pic
        FirebaseFirestore.getInstance().collection("users/").document(uid)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                User user = task.getResult().toObject(User.class);
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


        //////-----set listener for mentions
        createdPostText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                final String postText = createdPostText.getText().toString();
                //get the last word
                //and check if "@...."
                String lastWord = postText.substring(postText.lastIndexOf(" ")+1);
                if(lastWord.startsWith("@")){
                    //show listView
                    mentionSuggestionsList.setVisibility(View.VISIBLE);
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT
                    );
                    int marginTop = (createdPostText.getLineCount()*72)+45;
                    params.setMargins(0, marginTop, 0, 0);
                    mentionSuggestionsList.setLayoutParams(params);
                    //get mention text after @
                    final String mentioned = (lastWord.length()>1)? lastWord.substring(1):"";
                    //if @ only we will show random suggestions
                    if(mentioned.equals("")){
                        FirebaseFirestore.getInstance().collection("/users")
                                .limit(10)
                                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                final ArrayList<String> usersSuggNames = new ArrayList<>();
                                final ArrayList<User> usersSugg = new ArrayList<>();
                                if(task.isSuccessful())
                                    for (QueryDocumentSnapshot doc :task.getResult()) {
                                        User user = doc.toObject(User.class);
                                        usersSugg.add(user);
                                        usersSuggNames.add(user.first_name+" "+user.last_name);
                                    }
                                ////update mention suggestions list
                                ArrayAdapter<String> userNames = new ArrayAdapter<String>(CreatePost.this,
                                        R.layout.mention_suggestions_layout,R.id.suggName,usersSuggNames);
                                mentionSuggestionsList.setAdapter(userNames);
                                ///set listener for items in suggestion list
                                mentionSuggestionsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        //add the selected user id to the array
                                        mentionsIds.add(usersSugg.get(position).user_id);
                                        //replace @text with @firstName_lastName
                                        String oldtxt = createdPostText.getText().toString();
                                        String userName = "@"+ usersSugg.get(position).first_name+ "_"+usersSugg.get(position).last_name;
                                        int lastOccurOfAt =  oldtxt.lastIndexOf("@");
                                        createdPostText.setText(oldtxt.substring(0,lastOccurOfAt)+userName);
                                    }
                                });
                            }
                        });
                    }
                    else{
                        //show suggestion who his first or last name starts with that word
                        FirebaseFirestore.getInstance().collection("/users")
                                .whereGreaterThanOrEqualTo("first_name",mentioned)
                                .limit(15)
                                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                ArrayList<String> usersSuggNames = new ArrayList<>();
                                final ArrayList<User> usersSugg = new ArrayList<>();
                                if(task.isSuccessful())
                                    for (QueryDocumentSnapshot doc :task.getResult()) {
                                        User user = doc.toObject(User.class);
                                        usersSugg.add(user);
                                        usersSuggNames.add(user.first_name+" "+user.last_name);
                                    }
                                ////update mention suggestions list
                                ArrayAdapter<String> userNames = new ArrayAdapter<String>(CreatePost.this,
                                        R.layout.mention_suggestions_layout,R.id.suggName,usersSuggNames);
                                mentionSuggestionsList.setAdapter(userNames);
                                ///set listener for items in suggestion list
                                mentionSuggestionsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        //add the selected user id to the array
                                        mentionsIds.add(usersSugg.get(position).user_id);
                                        //replace @text with @firstName_lastName
                                        String oldtxt = createdPostText.getText().toString();
                                        String userName = "@"+ usersSugg.get(position).first_name+ "_"+usersSugg.get(position).last_name;
                                        int lastOccurOfAt =  oldtxt.lastIndexOf("@");
                                        createdPostText.setText(oldtxt.substring(0,lastOccurOfAt)+userName);
                                    }
                                });
                            }
                        });
                    }
                }
                else
                {
                    mentionSuggestionsList.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        ////---camera and video upload - waleed
        camerabtn = findViewById(R.id.createPostWithImageCamera);
        camerabtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askCameraPermissions();
            }
        });

        //uploadVideo
        videobtn = findViewById(R.id.createPostWithVideo);
        selectedVideo = findViewById(R.id.selectedVideo);

        selectedVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mc = new MediaController(CreatePost.this);
                selectedVideo.setMediaController(mc);
                mc.setAnchorView(selectedVideo);
            }
        });


        videobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openVideoChooser();
            }
        });
        //camerabtn
        //addimage decleartion
        storageReference = FirebaseStorage.getInstance().getReference("posts");
        selectedImage = findViewById(R.id.selectedImage);
        add_image = findViewById(R.id.createPostWithImage);
        add_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        ///--------end waleed task

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
        submitPost.setEnabled(false);
        submitPost.setTextColor(getResources().getColor(R.color.colorDisabled));
        post.category = catSpinner.getSelectedItem().toString();
        post.content_text = createdPostText.getText().toString();
        post.location = "Egypt";
        post.visibility = !createdPostIsAnon.isChecked();
        post.state = true;
        post.mentions = mentionsIds;
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();


        // Add a new document with a generated ID
        PostFragment.getUser(new Callback() {
            @Override
            public void callbackUser(User u) {
                post.postOwner = u.first_name+" "+u.last_name;
                post.user_id = uid;
                post.profile_pic = u.profile_pic;
                uploadPost();
            }
        },uid);




    }

    void uploadPost(){
        //////// add media to post by  --- waleed
        if (videoUri != null){
            final StorageReference fileReference = storageReference.child(videoName);
            uploadTask = fileReference.putFile(videoUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if(!task.isSuccessful()){
                        task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = (Uri) task.getResult();
                        videoUrl = downloadUri.toString();
                        post.videos = new ArrayList<>();
                        post.videos.add(videoUrl);
                        CollectionReference ref =FirebaseFirestore.getInstance().collection("/posts");
                        String postid = ref.document().getId();
                        post.post_id = postid;
                        ref.document(postid)
                                .set(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(CreatePost.this, "Post created successfully!", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        });
                        startActivity(new Intent(CreatePost.this, LoginActivity.class));
                        finish();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CreatePost.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else if (imageUri != null){
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
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
                        post.images = new ArrayList<>();
                        post.images.add(imageUrl);
                        CollectionReference ref =FirebaseFirestore.getInstance().collection("/posts");
                        String postid = ref.document().getId();
                        post.post_id = postid;
                        ref.document(postid)
                                .set(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(CreatePost.this, "Post created successfully!", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        });
                        startActivity(new Intent(CreatePost.this, LoginActivity.class));
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
            String postid = ref.document().getId();
            post.post_id = postid;
            ref.document(postid)
                    .set(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(CreatePost.this, "Post created successfully!", Toast.LENGTH_LONG).show();
                    finish();
                }
            });
            startActivity(new Intent(CreatePost.this, LoginActivity.class));
            finish();
        }
    }

    ///////////--- methods for media upload - waleed
    private void openVideoChooser() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, VIDEO_REQUEST_CODE);

    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VIDEO_REQUEST_CODE && resultCode == RESULT_OK && data != null){
            videoUri = data.getData();
            selectedVideo.setVideoURI(videoUri);
            selectedVideo.setVisibility(View.VISIBLE);
            selectedImage.setVisibility(View.GONE);
            //getting video Extension
            String mimeType = getContentResolver().getType(videoUri);
            String extension = mimeType.substring(mimeType.lastIndexOf("/")+1);
            videoName = System.currentTimeMillis() + "." + extension;
            //Toast.makeText(PostActivity.this, videoName, Toast.LENGTH_LONG).show();
        }
        else if(requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){
            File imageFile = new File(currentPhotoPath);
            try {
                bitmap = new Compressor(this).compressToBitmap(imageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            selectedImage.setImageBitmap(bitmap);
            selectedVideo.setVisibility(View.GONE);
            selectedImage.setVisibility(View.VISIBLE);

        }
        else if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK){

            imageUri = data.getData();
            selectedImage.setImageURI(imageUri);
            selectedVideo.setVisibility(View.GONE);
            selectedImage.setVisibility(View.VISIBLE);
        }
        else {
            startActivity(new Intent(CreatePost.this, LoginActivity.class));
            finish();
            selectedVideo.setVisibility(View.GONE);
            selectedImage.setVisibility(View.VISIBLE);
        }
    }


    //methods to convert bitmap to uriImage to upload it

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName;
        imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );


        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File...
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.a2hands.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }


    //camera methods
    private void askCameraPermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        }else {
            dispatchTakePictureIntent();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == CAMERA_PERM_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                dispatchTakePictureIntent();
            }else {
                Toast.makeText(this, "Camera Permission is Required to Use camera.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}