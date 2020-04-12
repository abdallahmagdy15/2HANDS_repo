package com.example.a2hands.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.a2hands.ChangeLocale;
import com.example.a2hands.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class MessageImageActivity extends AppCompatActivity {

    private ImageView MSGImage;
    private TextView Save;

    String messageImageID;
    String imageUri;
    String myUid;
    String hisUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeLocale.loadLocale(getBaseContext());
        setContentView(R.layout.activity_message_image);

        MSGImage = findViewById(R.id.MSGImage);
        Save = findViewById(R.id.Save);

        final Intent intent = getIntent();
        messageImageID = intent.getStringExtra("MSGDI");
        myUid = intent.getStringExtra("myUid");
        hisUid = intent.getStringExtra("hisUid");
        final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("chatList")
                .child(myUid)
                .child(hisUid)
                .child("messages");
        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds :dataSnapshot.getChildren()) {
                    Chat chat = ds.getValue(Chat.class);
                    if (chat.getMSGID().equals(messageImageID)) {
                        loadPhotos(MSGImage, "Chat_Pics/" + chat.getMessageImage());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    void loadPhotos(final ImageView imgV , String path){
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
        mStorageRef.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                imageUri=uri.toString();
                Picasso.get().load(uri.toString()).into(imgV);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }
}