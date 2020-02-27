package com.example.a2hands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.HashMap;

public class Comments extends AppCompatActivity {

    private SlidrInterface slidr;
    EditText add_comment;
    ImageView postCommentBtn;
    TextView like_count;
    String postid, publisherid, like_nums;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comments_layout);
        //Custom Animation To Activity
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.TOP)
                .sensitivity(1f)
                .scrimColor(Color.BLACK)
                .scrimStartAlpha(0.8f)
                .scrimEndAlpha(0.0f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .edge(true|false)
                .edgeSize(0.18f) // The % of the screen that counts as the edge, default 18%
                .build();
        slidr = Slidr.attach(this, config);
        //starting when Intent begins
        overridePendingTransition(R.anim.slide_in_up,R.anim.slide_out_bottom);


        //starting Coding to comment to firebase
        add_comment = findViewById(R.id.add_comment);
        postCommentBtn = findViewById(R.id.postCommentBtn);
        like_count = findViewById(R.id.like_count);
        Intent intent = getIntent();
        postid = intent.getStringExtra("postid");
        publisherid = intent.getStringExtra("publisherid");
        like_nums = intent.getStringExtra("like_nums");
        postCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(add_comment.getText().toString().equals("")){
                    Toast.makeText(Comments.this, "You Can’t Send Empty Comment", Toast.LENGTH_SHORT).show();
                }
                else
                    addComment();
            }
        });
        mlikes(postid);
    }

    //method to add comments to firebase in realtime mode
    private void addComment() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("comments").child(postid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("comment", add_comment.getText().toString());
        hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.push().setValue(hashMap);
        add_comment.setText("");
        //update comments count
        FirebaseFirestore.getInstance().collection("/posts").document(postid)
                .update("comments_count", FieldValue.increment(1));
    }

    //get Likes Count
    private void mlikes(final String postid){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("likes")
                .child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //likes.setText(dataSnapshot.getChildrenCount() + " Likes");
                //updatepost with count likes
                DocumentReference ref = FirebaseFirestore.getInstance().collection("posts").document(postid);
                like_count.setText( (int) dataSnapshot.getChildrenCount() +" Likes");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    //when Click Finish or back Starting...
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_up);
    }
}
