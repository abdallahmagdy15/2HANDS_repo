package com.example.a2hands;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.a2hands.home.posts.MyPostRecyclerViewAdapter;
import com.example.a2hands.locationsearch.SearchLocation;
import com.example.a2hands.home.posts.Post;
import com.example.a2hands.home.posts.PostCounter;
import com.example.a2hands.home.posts.PostsFragment;

import com.example.a2hands.notifications.NotificationHelper;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;


public class CreatePostActivity extends AppCompatActivity {

    TextView submitPost;
    Spinner catSpinner;
    EditText createdPostText;
    Switch createdPostIsAnon;
    DatabaseReference db;
    ImageView ownerPic;
    final Post post = new Post();
    ListView mentionSuggestionsList;
    View postSharedPreview;
    CardView cardView;
    String curr_uid;
    String shared_post_id;
    LinearLayout createPostAttachContainer;

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

    //upload Video
    public static final int VIDEO_REQUEST_CODE = 3;
    VideoView selectedVideo ;
    ImageView videobtn;
    Uri videoUri;
    String videoUrl = "";
    MediaController mc;
    String videoName;

    //Search Location button
    Button postLocation;
    public static final int LOCATION_REQUEST_CODE = 5;
    String location;

    //mention
    final ArrayList<String> usersSuggNames = new ArrayList<>();
    final ArrayList<User> usersSugg = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeLocale.loadLocale(getBaseContext());
        setContentView(R.layout.activity_create_post);

        Toolbar toolbar = findViewById(R.id.createPost_toolbar);

        // setup
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        submitPost = findViewById(R.id.submitPost);
        catSpinner = findViewById(R.id.catSpinner);
        createdPostText = findViewById(R.id.createdPostText);
        createdPostIsAnon = findViewById(R.id.createdPostIsAnon);
        db = FirebaseDatabase.getInstance().getReference();
        ownerPic = findViewById(R.id.postOwnerPic);
        mentionSuggestionsList= findViewById(R.id.mentionSuggestionsList);
        curr_uid = FirebaseAuth.getInstance().getUid();
        createPostAttachContainer = findViewById(R.id.createPostAttachContainer);
        cardView = findViewById(R.id.createPost_cardView);

        checkSharedPostExits();
        setLocationSelector();
        loadProfilePic();
        setMediaAttachListener();
        setMentionUserListener();

        submitPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
            }
        });
    }

    private void checkSharedPostExits(){
        //check if sharing a post
        shared_post_id = getIntent().getStringExtra("shared_post_id");
        if(shared_post_id != null){
            createPostAttachContainer.setVisibility(View.GONE);
            postSharedPreview = findViewById(R.id.postSharedPreview);
            cardView.setVisibility(View.VISIBLE);
            postSharedPreview.findViewById(R.id.postOptions).setVisibility(View.GONE);
            postSharedPreview.findViewById(R.id.postReactContainer).setVisibility(View.GONE);
            MyPostRecyclerViewAdapter myPostRecyclerViewAdapter = new MyPostRecyclerViewAdapter(null);
            MyPostRecyclerViewAdapter.ViewHolder vh = myPostRecyclerViewAdapter.new ViewHolder(postSharedPreview);
            getSharedPost(shared_post_id,myPostRecyclerViewAdapter,vh);

        }
    }

    private void getSharedPost(
            final String shared_post_id ,
            final MyPostRecyclerViewAdapter myPostRecyclerViewAdapter,
            final MyPostRecyclerViewAdapter.ViewHolder vh)
    {
        FirebaseFirestore.getInstance().collection("posts").document(shared_post_id)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    myPostRecyclerViewAdapter.setupPostData(vh,task.getResult().toObject(Post.class),true);
                }
            }
        });
    }


    void setLocationSelector(){
        postLocation = findViewById(R.id.createdPostLocation);
        postLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(CreatePostActivity.this, SearchLocation.class), LOCATION_REQUEST_CODE);
            }
        });
    }

    void loadProfilePic(){
        FirebaseFirestore.getInstance().collection("users/").document(curr_uid)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                User user = task.getResult().toObject(User.class);
                FirebaseStorage.getInstance().getReference().child("Profile_Pics/"+curr_uid+"/"+user.profile_pic).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
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
    }

    void setMediaAttachListener(){
        ////---camera and video upload - waleed
        camerabtn = findViewById(R.id.createPostWithImageCamera);
        camerabtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askCameraPermissions();
            }
        });

        //upload Video
        videobtn = findViewById(R.id.createPostWithVideo);
        selectedVideo = findViewById(R.id.selectedVideo);

        selectedVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mc = new MediaController(CreatePostActivity.this);
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

        ///--------end

    }

    void setMentionUserListener(){
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
                    //get mention text after @
                    final String mentioned = (lastWord.length()>1)? lastWord.substring(1):"";
                    //if @ only we will show random suggestions
                    if(mentioned.equals("")){
                        FirebaseFirestore.getInstance().collection("/users")
                                .limit(10)
                                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                if(task.isSuccessful())
                                    for (QueryDocumentSnapshot doc :task.getResult()) {
                                        User user = doc.toObject(User.class);
                                        usersSugg.add(user);
                                        usersSuggNames.add(user.full_name);
                                    }
                                ////update mention suggestions list
                                ArrayAdapter<String> userNames = new ArrayAdapter<String>(CreatePostActivity.this,
                                        R.layout.mention_suggestions_layout,R.id.suggName,usersSuggNames);
                                mentionSuggestionsList.setAdapter(userNames);
                                ///set listener for items in suggestion list
                                mentionSuggestionsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        //replace @text with @username
                                        String oldtxt = createdPostText.getText().toString();
                                        String userName = "@"+ usersSugg.get(position).user_name;
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
                                        usersSuggNames.add(user.full_name);
                                    }
                                ////update mention suggestions list
                                ArrayAdapter<String> userNames = new ArrayAdapter<String>(CreatePostActivity.this,
                                        R.layout.mention_suggestions_layout,R.id.suggName,usersSuggNames);
                                mentionSuggestionsList.setAdapter(userNames);
                                ///set listener for items in suggestion list
                                mentionSuggestionsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        //replace @text with @firstName_lastName
                                        String oldtxt = createdPostText.getText().toString();
                                        String userName = "@"+ usersSugg.get(position).user_name;
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
    }

    public void submitPost() {
        submitPost.setEnabled(false);
        submitPost.setTextColor(getResources().getColor(R.color.colorDisabled));
        post.category = catSpinner.getSelectedItem().toString();
        post.content_text = createdPostText.getText().toString();
        if(shared_post_id != null) post.shared_id = shared_post_id;
        //check if location not empty
        if(location != null)
            post.location = location;
        else{
            FirebaseFirestore.getInstance().collection("/users")
                    .document(curr_uid).get()
                    .addOnCompleteListener(
                            new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    User user = task.getResult().toObject(User.class);
                                    post.location = loadCountryUsingItsISO(user.country);
                                }
                            }
                    );
        }

        post.visibility = !createdPostIsAnon.isChecked();
        post.state = true;
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Add a new document with a generated ID
        PostsFragment.getUser(new Callback() {
            @Override
            public void callbackUser(User u) {
                post.postOwner = u.full_name;
                post.user_id = uid;
                post.profile_pic = u.profile_pic;
                uploadMedia();
            }
        },uid);


    }

    void uploadMedia(){
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
                        savePost();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CreatePostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        savePost();
                    }
                    else {
                        Toast.makeText(CreatePostActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CreatePostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else{
            savePost();
        }
    }

    void savePost(){
        CollectionReference ref =FirebaseFirestore.getInstance().collection("/posts");
        String postid = ref.document().getId();
        post.post_id = postid;
        //set empty post counter
        PostCounter postCounter = new PostCounter();
        FirebaseDatabase.getInstance().getReference().child("counter").child(postid).setValue(postCounter);
        ref.document(postid)
                .set(post).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(CreatePostActivity.this, getResources().getString(R.string.postCreatedSuccessfully), Toast.LENGTH_LONG).show();
                //check if shared post to send notifi
                if(shared_post_id != null){
                    final NotificationHelper nh = new NotificationHelper(CreatePostActivity.this);
                    PostsFragment.getUser(new Callback() {
                        @Override
                        public void callbackUser(User user) {
                            nh.sendSharingNotifi(user,shared_post_id);
                        }
                    },curr_uid);
                }
                finish();
            }
        });
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

        if (requestCode == LOCATION_REQUEST_CODE && resultCode == RESULT_OK){
            location = data.getStringExtra("SELECTED_LOCATION");
            postLocation.setText(location);
        }
        else if (requestCode == VIDEO_REQUEST_CODE && resultCode == RESULT_OK && data != null){
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
            startActivity(new Intent(CreatePostActivity.this, LoginActivity.class));
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
                ".jpg",   /* suffix */
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
                Toast.makeText(this, getResources().getString(R.string.cameraPermissionIsRequired), Toast.LENGTH_SHORT).show();
            }
        }
    }


    // loading JSON file of countries and states from assets folder
    public String loadCountryStateJSONFromAsset() {
        String json = null;
        try {
            InputStream inputStreanm = this.getAssets().open("countriesandstates.json");
            int size = inputStreanm.available();
            byte[] buffer = new byte[size];
            inputStreanm.read(buffer);
            inputStreanm.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public String loadCountryUsingItsISO(String countryCode){
        try {
            JSONObject obj = new JSONObject(loadCountryStateJSONFromAsset());
            JSONArray countries_arr = obj.getJSONArray("countries");

            Map<String,String> countries_code_name = new HashMap<>();

            for (int i = 0; i < countries_arr.length(); i++) {
                JSONObject jo_inside = countries_arr.getJSONObject(i);
                String iso2 = jo_inside.getString("iso2");
                String country_name = jo_inside.getString("name");

                countries_code_name.put(iso2,country_name);
            }
            return countries_code_name.get(countryCode);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


}