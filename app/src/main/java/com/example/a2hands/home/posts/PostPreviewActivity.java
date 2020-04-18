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

public class PostPreviewActivity extends AppCompatActivity {

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
        getPost(postid,myPostRecyclerViewAdapter,vh);
    }

    private void getPost(
            final String post_id ,
            final MyPostRecyclerViewAdapter myPostRecyclerViewAdapter,
            final MyPostRecyclerViewAdapter.ViewHolder vh)
    {
        FirebaseFirestore.getInstance().collection("posts").document(post_id)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Post post = task.getResult().toObject(Post.class);
                if(post != null) {
                    myPostRecyclerViewAdapter.setupPostData(vh,post,true);
                }
            }
        });
    }
}
