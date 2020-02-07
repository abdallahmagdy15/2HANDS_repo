package com.example.a2hands;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a2hands.homePackage.homeActivity;
import com.google.android.gms.internal.firebase_auth.zzcz;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {
    public FirebaseAuth mAuth;
    public FirebaseUser fu;
    public DatabaseReference dbRef;

    String email;
    String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        signin();

    }
    public void signin(){
        mAuth.signInWithEmailAndPassword("ahmedKamal9@gmail.com", "556558554552")
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        FirebaseUser fu = mAuth.getCurrentUser();
                        email = fu.getEmail();
                        uid = fu.getUid();
                        TextView txt = findViewById(R.id.mainTXT);
                        txt.setText(email + " " + uid);
                    }
                });
        Intent intent = new Intent(this,homeActivity.class);
        startActivity(intent);
    }
    @Override
    public void onStart() {
        super.onStart();

    }

}
