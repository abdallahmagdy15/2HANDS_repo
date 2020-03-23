package com.example.a2hands.chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a2hands.R;
import com.example.a2hands.User;
import com.example.a2hands.home.homeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileimage;
    TextView hisname;
    public TextView userstatus;
    EditText message;
    ImageButton sendButton;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersdbref;
    private FirebaseFirestore db;
    private StorageReference mStorageRef;
    ValueEventListener seenListner;
    DatabaseReference userRefForseen;

    List<Chat> chatList;
    ChatAdapter adapterChat;

    public String hisUid;
    String myUid;
    String hisImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        toolbar=findViewById(R.id.toolbar);

        recyclerView=findViewById(R.id.chatrecycleview);
        profileimage=findViewById(R.id.profile_Image);
        hisname=findViewById(R.id.HisName);
        userstatus=findViewById(R.id.userstatus);
        message=findViewById(R.id.messageEdit);
        sendButton=findViewById(R.id.sendbutton);

        final Intent intent =getIntent();
        hisUid=intent.getStringExtra("hisUid");

        firebaseAuth = FirebaseAuth.getInstance();
        myUid=firebaseAuth.getCurrentUser().getUid();
        firebaseDatabase = FirebaseDatabase.getInstance();
        usersdbref =firebaseDatabase.getReference("users");
        mStorageRef = FirebaseStorage.getInstance().getReference();

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        db = FirebaseFirestore.getInstance();
        loadhisinfo();

        message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0){
                    updateTypingStatus("noOne");
                }else {
                    //String Message=message.getText().toString().trim();
                    updateTypingStatus(hisUid);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Message=message.getText().toString().trim();
                if(TextUtils.isEmpty(Message)){
                    Toast.makeText(ChatActivity.this, "Cannot send empty message...", Toast.LENGTH_SHORT).show();
                }else {
                    sendMessage(Message);
                }
            }
        });


        readmessage();
        seenmassege();
        loadUserOnlineAndtypingStatus();
    }

    private void seenmassege() {
        userRefForseen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListner =userRefForseen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Chat chat= ds.getValue(Chat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)){
                        HashMap<String,Object> hasSeenHashmap=new HashMap<>();
                        hasSeenHashmap.put("isSeen",true);
                        ds.getRef().updateChildren(hasSeenHashmap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readmessage() {
        chatList =new ArrayList<>();
        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("Chats");
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Chat chat=ds.getValue(Chat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)||
                            chat.getReceiver().equals(hisUid) && chat.getSender().equals(myUid)){
                        chatList.add(chat);
                    }

                    adapterChat =new ChatAdapter(ChatActivity.this,chatList,hisImage);
                    adapterChat.notifyDataSetChanged();
                    //set adapter to recyclerview
                    recyclerView.setAdapter(adapterChat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(final String messagebody) {
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference();
        Calendar cal =Calendar.getInstance(Locale.ENGLISH);
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("hh:mm a");
        String dateTime =simpleDateFormat.format(cal.getTime());

        Chat chat = new Chat(messagebody, hisUid, myUid, dateTime, false);
        databaseReference.child("Chats").push().setValue(chat);

        //reset edittext after sending message
        message.setText("");

        //Chat list
        final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(myUid)
                .child(hisUid);
        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef1.child("id").setValue(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(hisUid)
                .child(myUid);

        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef2.child("id").setValue(myUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        loadUserOnlineAndtypingStatus();
        checkUserStatus();
        super.onStart();
    }

    @Override
    protected void onPause() {
        loadUserOnlineAndtypingStatus();
        updateTypingStatus("noOne");
        userRefForseen.removeEventListener(seenListner);
        super.onPause();
    }

    @Override
    protected void onResume() {
        loadUserOnlineAndtypingStatus();
        super.onResume();
    }

    //get other user's information
    private void loadhisinfo() {
        db.collection("users/").document(hisUid)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                User user = task.getResult().toObject(User.class);
                loadPhotos(profileimage,"Profile_Pics/"+hisUid+"/"+user.profile_pic );
                hisname.setText(user.full_name);
            }
        });
    }



    private void loadUserOnlineAndtypingStatus(){
        db.collection("users/").document(hisUid)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (documentSnapshot.exists()){
                            String onlineStatus=documentSnapshot.getString("onlineStatus");
                            String typingStatus=documentSnapshot.getString("typingTo");
                            if (typingStatus.equals(myUid)){
                                userstatus.setText("typing...");
                            }else {
                                if (onlineStatus.equals("online")) {
                                    userstatus.setText("online");
                                } else {
                                    userstatus.setText("Last seen at: "+onlineStatus);
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

    ///load other user's profileImage
    void loadPhotos(final ImageView imgV , String path){

        mStorageRef.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                hisImage=uri.toString();
                Picasso.get().load(uri.toString()).into(imgV);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    private void checkUserStatus(){
        FirebaseUser user =firebaseAuth.getCurrentUser();
        if(user !=null){
            myUid=user.getUid();
        }else {
            startActivity(new Intent(ChatActivity.this, homeActivity.class));
            finish();
        }
    }

    //@Override
    //public boolean onCreateOptionsMenu(Menu menu) {
    //  getMenuInflater().inflate(R.menu.bottom_navigation,menu);
    //menu.findItem(R.id.searchNav);

    //   return super.onCreateOptionsMenu(menu);
    //}

    //@Override
    //public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    //  return super.onOptionsItemSelected(item);
    //}
}