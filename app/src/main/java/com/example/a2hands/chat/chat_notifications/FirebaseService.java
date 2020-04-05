package com.example.a2hands.chat.chat_notifications;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;

@SuppressLint("Registered")
public class FirebaseService extends FirebaseMessagingService {


    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

        String newToken = FirebaseInstanceId.getInstance().getToken();

        if (user != null){
            updateToken(newToken);
        }
    }

    private void updateToken(String newToken) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(newToken);
        ref.child(user.getUid()).setValue(token);
    }
}
