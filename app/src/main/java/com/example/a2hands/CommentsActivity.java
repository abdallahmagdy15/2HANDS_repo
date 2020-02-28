package com.example.a2hands;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a2hands.homePackage.PostFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.Date;

public class CommentsActivity extends AppCompatActivity implements CommentsFragment.OnListFragmentInteractionListener {

    private SlidrInterface slidr;
    EditText add_comment;
    ImageView postCommentBtn;
    TextView like_count;
    int likesCount;
    String postid, publisherid , curr_uid;
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
                .velocityThreshold(1000)
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
        likesCount = intent.getIntExtra("likes_count",0);
        postid = intent.getStringExtra("post_id");
        curr_uid = intent.getStringExtra("curr_uid");
        postCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(add_comment.getText().toString().equals("")){
                    Toast.makeText(CommentsActivity.this, "You Can’t Send Empty Comment", Toast.LENGTH_SHORT).show();
                }
                else
                    addComment();
            }
        });
        //set likes counter
        if(likesCount!=0)
        like_count.setText(likesCount+" Likes");

        //load comments
        loadComments(postid);
    }

    //load comments
    void loadComments(String postId){

        Fragment frg = new CommentsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("postId", postId);
        frg.setArguments(bundle);
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.commentsContainer,frg);
        ft.commit();

    }
    //method to add comments to firebase in realtime mode
    private void addComment() {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("comments").child(postid);
        final Comment comment = new Comment();
        comment.comment_content = add_comment.getText().toString();
        comment.post_id=postid;
        comment.publisher_id=curr_uid;
        comment.date = new Date();
        PostFragment.getUser(new Callback() {
            @Override
            public void callbackUser(User user) {
                comment.publisher_pic=user.profile_pic;
                comment.name=user.first_name+" "+user.last_name;
                reference.push().setValue(comment);
                add_comment.setText("");
                //update comments count
                FirebaseFirestore.getInstance().collection("/posts").document(postid)
                        .update("comments_count", FieldValue.increment(1));
            }
        },curr_uid);

    }

    //when Click Finish or back Starting...
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_up);
    }

    @Override
    public void onListFragmentInteraction(Comment item) {

    }
}
