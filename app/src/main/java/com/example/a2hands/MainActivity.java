package com.example.a2hands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;

  interface Callback{
      void firebaseResponseCallback(ArrayList<Post> posts);
}
public class MainActivity extends AppCompatActivity {
    String email;
    String uid;
    FirebaseUser fu;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    public static ArrayList<Post> posts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        posts = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        autoSigningin();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            email = user.getEmail();
            uid = user.getUid();
        }

        db = FirebaseFirestore.getInstance();
        //get posts from database
        getPosts(new Callback() {
            @Override
            public void firebaseResponseCallback(ArrayList<Post> posts) {
                for (Post p : posts) {
                    Log.i("post text",p.getContent_text());
                }
            }
        });



    }

    public void getPosts(final Callback callback){
        // Read from the database
        db.collection("/posts")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<Post> posts=new ArrayList<>();
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                Post p = doc.toObject(Post.class);
                                posts.add(p);
                                callback.firebaseResponseCallback(posts);
                            }
                        } else {
                            Log.w("", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    public void autoSigningin(){
        mAuth.signInWithEmailAndPassword("ahmedKamal9@gmail.com", "556558554552")
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            fu =  mAuth.getCurrentUser();
                        } else {
                            Log.i("TAG","auth failed");
                        }
                    }
                });
    }
}
