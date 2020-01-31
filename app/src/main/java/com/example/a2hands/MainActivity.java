package com.example.a2hands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

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


import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    String email;
    String uid;
    FirebaseUser fu;
    FirebaseAuth mAuth;
    public DatabaseReference dbRef;
    public ArrayList<Post> posts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        


        autoSigningin();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            email = user.getEmail();
            uid = user.getUid();
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("/posts");
        //get posts from database
        posts = getPosts();
        for (Post p : posts) {
            Log.i("post text",p.getContent_text());
        }

    }

    public ArrayList<Post> getPosts(){
        final ArrayList<Post> posts = new ArrayList<>();
        // Read from the database
        dbRef.child("posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> posts_ss = dataSnapshot.getChildren();
                for (DataSnapshot post_ss : posts_ss){
                    Post p = new Post();
                    p.setCategory(post_ss.getValue(Post.class).getCategory());
                    p.setCheck_in(post_ss.getValue(Post.class).getCheck_in());
                    p.setContent_text(post_ss.getValue(Post.class).getContent_text());
                    p.setDate(post_ss.getValue(Post.class).getDate());
                    p.setPrivacy(post_ss.getValue(Post.class).getPrivacy());
                    p.setImages(post_ss.getValue(Post.class).getImages());
                    p.setVideos(post_ss.getValue(Post.class).getVideos());
                    p.setLikes_count(post_ss.getValue(Post.class).getLikes_count());
                    p.setState(post_ss.getValue(Post.class).getState());
                    p.setVisibility(post_ss.getValue(Post.class).getVisibility());
                    p.setUser_id(post_ss.getValue(Post.class).getUser_id());
                    posts.add(p);
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException());
            }
        });
        return posts;
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
