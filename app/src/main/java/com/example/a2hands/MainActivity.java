package com.example.a2hands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.example.a2hands.dummy.DummyContent;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.List;

interface Callback{
      void callbackUser(User user);
      void callbackUserID(String uid);
}
public class MainActivity extends AppCompatActivity implements PostFragment.OnListFragmentInteractionListener {
    String email;
    String uid;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    public static ArrayList<Post> posts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        posts = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();



        //get posts from database
        autoSigningin(new Callback() {
            @Override
            public void callbackUser(User user) { }
            @Override
            public void callbackUserID(String uid) {
                getUser(new Callback() {
                    @Override
                    public void callbackUser(User user) {
                        List<String> visibility =new ArrayList<>();
                        visibility.add(user.country);
                        visibility.add(user.region);

                        getPosts(visibility,"general");
                    }
                    @Override
                    public void callbackUserID(String uid){ }
                },uid);

            }
        });

    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

    }

    public  void getUser(final Callback callback,String uid){
        db.collection("/users").document(uid)
            .get() .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                    User user = doc.toObject(User.class);
                    callback.callbackUser(user);
            }
            else {
                Log.w("", "Error getting documents.", task.getException());
            }
        }
    });
}

    public void getPosts(List<String> visibility, String category){
        // Read from the database
        db.collection("/posts")
                .whereIn("visibility", visibility)
                .whereEqualTo("category",category)
                .orderBy("date")
                .limitToLast(30)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                Post p = doc.toObject(Post.class);
                                Log.i("    post text",p.content_text);
                            }
                        } else {
                            Log.w("", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    public void autoSigningin(final Callback callback){
        mAuth.signInWithEmailAndPassword("ahmedKamal9@gmail.com", "556558554552")
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Log.i("login","auth success");
                        } else {
                            Log.i("login","auth failed");
                        }
                        callback.callbackUserID(mAuth.getCurrentUser().getUid());
                    }
                });
    }
}
