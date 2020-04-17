package com.example.a2hands.chat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a2hands.ChangeLocale;
import com.example.a2hands.R;
import com.example.a2hands.User;
import com.example.a2hands.UserStatus;
import com.example.a2hands.chat.chat_notifications.APIService;
import com.example.a2hands.chat.chat_notifications.Client;
import com.example.a2hands.chat.chat_notifications.Data;
import com.example.a2hands.chat.chat_notifications.Response;
import com.example.a2hands.chat.chat_notifications.Sender;
import com.example.a2hands.chat.chat_notifications.Token;
import com.example.a2hands.home.HomeActivity;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class ChatActivity extends AppCompatActivity {

    Toolbar toolBar;
    RecyclerView recyclerView;
    ImageView profileImage,messageImage;
    TextView hisName;
    public TextView userStatus;
    EditText message;
    ImageButton sendButton,uploadImage;
    FloatingActionButton scrollDownBtn;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDbRef;
    private FirebaseFirestore db;
    private StorageReference mStorageRef;
    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;

    List<Chat> chatList;
    com.example.a2hands.chat.ChatAdapter adapterChat;

    public String hisUid;
    String myUid;
    String hisImage;

    APIService apiService;

    //UPLOAD IMAGE
    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=200;

    private static final int IMAGE_PICK_CAMERA_CODE=300;
    private static final int IMAGE_PICK_GALLERY_CODE=400;


    String[] cameraPermissions;
    String[] storagePermissions;

    Uri image_uri =null;

    ImageButton closeImageButton;

    LinearLayoutManager linearLayoutManager;


    @SuppressLint("PrivateResource")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeLocale.loadLocale(getBaseContext());
        setContentView(R.layout.activity_chat);

        getWindow().setBackgroundDrawableResource(R.drawable.chat_bg);

        toolBar = findViewById(R.id.toolbar);
        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        recyclerView = findViewById(R.id.chatrecycleview);
        profileImage = findViewById(R.id.profile_Image);

        /////UPLOAD_IMAGE
        messageImage = findViewById(R.id.messageImage);
        uploadImage = findViewById(R.id.uploadimage);
        cameraPermissions = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ///////
        hisName = findViewById(R.id.HisName);
        userStatus = findViewById(R.id.userstatus);
        message = findViewById(R.id.messageEdit);
        sendButton = findViewById(R.id.sendbutton);

        scrollDownBtn = findViewById(R.id.scrollDownFloatingActionBtn);
        scrollDownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollToBottom();
            }
        });

        SharedPreferences prefs = getSharedPreferences("settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Language", "");
        if(language.equals("ar")){
            sendButton.setScaleX(-1f);
            uploadImage.setScaleX(-1f);
        }

        final Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");

        firebaseAuth = FirebaseAuth.getInstance();
        myUid=firebaseAuth.getCurrentUser().getUid();
        firebaseDatabase = FirebaseDatabase.getInstance();
        usersDbRef = firebaseDatabase.getReference("users");
        mStorageRef = FirebaseStorage.getInstance().getReference();

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //create API service
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);

        db = FirebaseFirestore.getInstance();

        message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0){
                    updateTypingStatus("noOne");
                }else {
                    updateTypingStatus(hisUid);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        closeImageButton = findViewById(R.id.imageButton_close);
        closeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image_uri = null;
                messageImage.setVisibility(View.GONE);
                closeImageButton.setVisibility(View.GONE);
            }
        });


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Message = message.getText().toString().trim();
                sendImageMessage(Message);
                messageImage.setVisibility(View.GONE);
                closeImageButton.setVisibility(View.GONE);
            }
        });

        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });

        loadHisInfoAndchat();
        seenMessages();
        loadUserOnlineAndTypingStatus();

    }//////////////end of onCreate



    public void scrollToBottom(){
        recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollDownBtn.setVisibility(View.GONE);
                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
            }
        }, 1000);
    }



    private void seenMessages() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("chatList")
                .child(hisUid)
                .child(myUid).child("messages");

        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Chat chat= ds.getValue(Chat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)){
                        HashMap<String,Object> hasSeenHashMap=new HashMap<>();
                        hasSeenHashMap.put("isSeen",true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessages() {
        chatList =new ArrayList<>();

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("chatList")
                .child(myUid)
                .child(hisUid).child("messages");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();

                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Chat chat = ds.getValue(Chat.class);

                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)||
                            chat.getReceiver().equals(hisUid) && chat.getSender().equals(myUid)){
                        chatList.add(chat);
                    }
                    adapterChat =new ChatAdapter(ChatActivity.this,chatList,hisImage,hisUid,linearLayoutManager,scrollDownBtn);
                    adapterChat.notifyDataSetChanged();
                    //set adapter to recyclerView
                    recyclerView.setAdapter(adapterChat);
                    scrollToBottom();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendImageMessage(final String messageBody) {
        if (image_uri != null) {

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getResources().getString(R.string.sendingImage));
            progressDialog.show();

            final StorageReference ChatImageRef = FirebaseStorage.getInstance().getReference("Chat_Pics/" +
                    System.currentTimeMillis() + "." + getFileExtension(image_uri).trim());

            UploadTask uploadTask = ChatImageRef.putFile(image_uri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if(!task.isSuccessful()){
                        task.getException();
                    }
                    return ChatImageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            progressDialog.dismiss();

                            Uri downloadUri = task.getResult();

                            Calendar cal =Calendar.getInstance(Locale.ENGLISH);
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
                            String dateTime = simpleDateFormat.format(cal.getTime());

                            String MSG_ID =String.valueOf(System.currentTimeMillis());
                            String MSG_Img = downloadUri.toString();

                            final DatabaseReference myUsersList1 = FirebaseDatabase.getInstance().getReference("chatList")
                                    .child(myUid).child("myUsersList").child(hisUid);
                            final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("chatList")
                                    .child(myUid)
                                    .child(hisUid);

                            final DatabaseReference myUsersList2 = FirebaseDatabase.getInstance().getReference("chatList")
                                    .child(hisUid).child("myUsersList").child(myUid);
                            final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("chatList")
                                    .child(hisUid)
                                    .child(myUid);

                            //setup required data
                            Chat MSG = new Chat(
                                    MSG_ID,messageBody,MSG_Img,hisUid,myUid,dateTime,false,false
                            );

                            //reset editText after sending message
                            message.setText("");

                            //Notifications
                            DocumentReference firebaseFirestore =FirebaseFirestore.getInstance().collection("users").document(myUid);
                            firebaseFirestore.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    User user = task.getResult().toObject(User.class);

                                    sendNotification(hisUid,user.full_name,messageBody);
                                }
                            });

                            //Chat List
                            chatRef1.child("messages").push().setValue(MSG);
                            myUsersList1.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.exists()){
                                        myUsersList1.child("id").setValue(hisUid);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            chatRef2.child("messages").push().setValue(MSG);
                            myUsersList2.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.exists()){
                                        myUsersList2.child("id").setValue(myUid);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });

            //set imageUri to null after sending the message
            image_uri = null;
        } else {
            if(TextUtils.isEmpty(messageBody)){
                Toast.makeText(this, getResources().getString(R.string.youCanNotSendEmptyMessage), Toast.LENGTH_SHORT).show();
            }else {
                sendMessage(messageBody);
                //reset editText after sending message
                message.setText("");
            }
        }
    }

    private void sendMessage(final String messageBody) {

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
        String dateTime = simpleDateFormat.format(cal.getTime());

        String MSG_ID = String.valueOf(System.currentTimeMillis());

        Chat MSG = new Chat(
                MSG_ID,messageBody,"",hisUid,myUid,dateTime,false,false
        );

        DocumentReference firebaseFirestore =FirebaseFirestore.getInstance().collection("users").document(myUid);
        firebaseFirestore.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                User user= task.getResult().toObject(User.class);

                sendNotification(hisUid,user.full_name,messageBody);
            }
        });

        //Chat list
        final DatabaseReference myUsersList1 = FirebaseDatabase.getInstance().getReference("chatList")
                .child(myUid).child("myUsersList").child(hisUid);
        final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("chatList")
                .child(myUid)
                .child(hisUid);

        chatRef1.child("messages").push().setValue(MSG);
        myUsersList1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    myUsersList1.child("id").setValue(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        final DatabaseReference myUsersList2 = FirebaseDatabase.getInstance().getReference("chatList")
                .child(hisUid).child("myUsersList").child(myUid);
        final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("chatList")
                .child(hisUid)
                .child(myUid);

        chatRef2.child("messages").push().setValue(MSG);
        myUsersList2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    myUsersList2.child("id").setValue(myUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendNotification(final String hisUid, final String full_name, final String messagebody) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Data data =new Data(myUid,full_name+":"+messagebody,"New Message",hisUid,R.drawable.messaging2);

                    Sender sender = new Sender(data,token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
//                                    Toast.makeText(ChatActivity.this, ""+response.message(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //UPLOAD IMAGE
    private void showImagePickDialog(){
        //options {camera , Gallery} to show in dialog
        String[] options = {getResources().getString(R.string.camera),getResources().getString(R.string.gallery)};
        //dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.chooseImageFrom));
        //set options to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //item click handle
                if (which == 0){
                    //camera click
                    if (!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else {
                        pickFromCamera();
                    }
                }
                if (which == 1){
                    //Gallery click
                    if (!checkStoragePermission()){
                        requestStoragePermission();
                    }else {
                        pickFromGallery();
                    }

                }
            }
        });
        //create and show dialog
        builder.create().show();

    }

    private void pickFromGallery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }
    private void pickFromCamera(){
        //intent to pick image from camera
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE,"Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Temp Descr");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission(){
        //check if Gallery permission is enabled
        //return true if enabled
        //return false if not enabled
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==(PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        //request runtime permission
        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        //check if Camera permission is enabled
        //return true if enabled
        //return false if not enabled
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) ==(PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==(PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted & storageAccepted){
                        pickFromCamera();
                    }else {
                        Toast.makeText(this, "Camera & Storage both permissions are necessary...", Toast.LENGTH_SHORT).show();
                    }

                }else {

                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean storageAccepted =grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        pickFromGallery();
                    }else {
                        Toast.makeText(this, "Storage permissions necessary...", Toast.LENGTH_SHORT).show();
                    }

                }else {

                }
            }
            break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                image_uri = data.getData();
                messageImage.setVisibility(View.VISIBLE);
                closeImageButton.setVisibility(View.VISIBLE);

                Picasso.get().load(image_uri).into(messageImage);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE ) {
                messageImage.setVisibility(View.VISIBLE);
                messageImage.setImageURI(image_uri);
                closeImageButton.setVisibility(View.VISIBLE);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }


    @Override
    protected void onStart() {
        loadUserOnlineAndTypingStatus();
        checkUserStatus();
        super.onStart();
    }

    @Override
    protected void onPause() {
        loadUserOnlineAndTypingStatus();
        updateTypingStatus("noOne");
        userRefForSeen.removeEventListener(seenListener);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if(UserStatus.isAppIsInBackground(getApplicationContext())){
            UserStatus.updateOnlineStatus(false, myUid);
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        UserStatus.updateOnlineStatus(true, myUid);
        loadUserOnlineAndTypingStatus();
        super.onResume();
    }

    //get other user's information
    private void loadHisInfoAndchat() {
        db.collection("users/").document(hisUid)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                User user = task.getResult().toObject(User.class);
                String path = "Profile_Pics/"+hisUid+"/"+user.profile_pic;

                mStorageRef.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        hisImage=uri.toString();
                        Picasso.get().load(uri.toString()).into(profileImage);
                        readMessages();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });
                hisName.setText(user.full_name);
            }
        });
    }



    private void loadUserOnlineAndTypingStatus(){
        db.collection("users/").document(hisUid)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (documentSnapshot.exists()){
                            String onlineStatus=documentSnapshot.getString("onlineStatus");
                            String typingStatus=documentSnapshot.getString("typingTo");
                            if (typingStatus.equals(myUid)){
                                userStatus.setText(getResources().getString(R.string.typing));
                            }else {
                                if (onlineStatus.equals("online")) {
                                    userStatus.setText(getResources().getString(R.string.online));
                                } else {
                                    userStatus.setText(onlineStatus);
                                }
                            }
                        }
                    }
                });

    }

    private void updateTypingStatus(String typing) {
        db.collection("users/").document(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);
        db.collection("users/").document(myUid).update(hashMap);

    }

    private void checkUserStatus(){
        FirebaseUser user =firebaseAuth.getCurrentUser();
        if(user !=null){
            myUid=user.getUid();
        }else {
            startActivity(new Intent(ChatActivity.this, HomeActivity.class));
            finish();
        }
    }


}