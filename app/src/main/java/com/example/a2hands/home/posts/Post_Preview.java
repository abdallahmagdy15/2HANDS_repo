package com.example.a2hands.home.posts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;

import com.example.a2hands.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Post_Preview extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post__preview);

        Toolbar toolbar = findViewById(R.id.postPreview_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        String postid =  getIntent().getStringExtra("POST_ID");
        View postContainer= findViewById(R.id.postPreview_container);

        MyPostRecyclerViewAdapter myPostRecyclerViewAdapter = new MyPostRecyclerViewAdapter(null);
        MyPostRecyclerViewAdapter.ViewHolder vh = myPostRecyclerViewAdapter.new ViewHolder(postContainer);
        getSharedPost(postid,myPostRecyclerViewAdapter,vh);
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
}
