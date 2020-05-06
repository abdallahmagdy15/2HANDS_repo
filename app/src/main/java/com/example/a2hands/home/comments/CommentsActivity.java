package com.example.a2hands.home.comments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;
import com.example.a2hands.Callback;
import com.example.a2hands.ChangeLocale;
import com.example.a2hands.UserStatus;
import com.example.a2hands.home.LikesActivity;
import com.example.a2hands.notifications.NotificationHelper;
import com.example.a2hands.R;
import com.example.a2hands.User;
import com.example.a2hands.home.posts.Post;
import com.example.a2hands.home.posts.PostsFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.Date;

public class CommentsActivity extends AppCompatActivity  {

    private SlidrInterface slidr;
    EditText add_comment;
    ImageView postCommentBtn;
    TextView like_count;
    int likesCount;
    String postId , curr_uid;
    Toolbar commentsToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeLocale.loadLocale(getBaseContext());
        setContentView(R.layout.activity_comments);

        commentsToolbar = findViewById(R.id.commentsToolbar);

        setCommentsSlider();

        //starting Coding to comment to firebase
        add_comment = findViewById(R.id.add_comment);
        postCommentBtn = findViewById(R.id.postCommentBtn);
        like_count = findViewById(R.id.like_count);

        Intent intent = getIntent();
        likesCount = intent.getIntExtra("likes_count",0);
        postId = intent.getStringExtra("post_id");
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

        SharedPreferences prefs = getSharedPreferences("settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Language", "");
        if(language.equals("ar")){
            postCommentBtn.setScaleX(-1f);
        }


        setLikesCounter();

        setLikesListener();

        //load comments
        loadComments(postId);
    }

    void setCommentsSlider(){
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
        overridePendingTransition(R.anim.slide_in_bottom,R.anim.slide_out_up);
    }

    void setLikesCounter(){
        if(likesCount!=0)
            like_count.setText(likesCount+" "+getResources().getString(R.string.likes));
    }

    void setLikesListener(){
        commentsToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CommentsActivity.this, LikesActivity.class);
                i.putExtra("POST_ID", postId);
                startActivity(i);
            }
        });
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
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("comments").child(postId);
        final Comment comment = new Comment();
        comment.comment_content = add_comment.getText().toString();
        comment.post_id= postId;
        comment.publisher_id=curr_uid;
        comment.date = new Date();
        PostsFragment.getUser(new Callback() {
            @Override
            public void callbackUser(final User user) {
                comment.publisher_pic=user.profile_pic;
                comment.name=user.full_name;
                //store Comment to use it in Like Comment
                comment.comment_id = reference.push().getKey();
                reference.push().setValue(comment)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                //send notification
                                final NotificationHelper nh = new NotificationHelper(CommentsActivity.this);
                                FirebaseFirestore.getInstance().collection("posts").document(postId)
                                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.getResult() != null){
                                            Post post = task.getResult().toObject(Post.class);
                                            nh.sendReactOnPostNotification(user,"COMMENT_POST",post,
                                                    " commented on your post \""+post.content_text+"\"");
                                        }
                                    }
                                });
                            }
                        });
                add_comment.setText("");
                FirebaseFirestore.getInstance().collection("posts")
                        .document(postId).update("priority", FieldValue.increment(0.1));
                //update counter for comments
                FirebaseDatabase.getInstance().getReference("counter").child(postId)
                        .child("comments_count").runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                        int curr_ = mutableData.getValue(Integer.class);
                        mutableData.setValue(curr_ +1);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

                    }
                });
                //end update counter for comments
            }
        },curr_uid);

    }
    //when Click Finish or back Starting...
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_bottom);
    }

    @Override
    protected void onResume() {
        UserStatus.updateOnlineStatus(true, curr_uid);
        super.onResume();
    }

    @Override
    protected void onStop()
    {
        if(UserStatus.isAppIsInBackground(getApplicationContext())){
            UserStatus.updateOnlineStatus(false, curr_uid);
        }
        super.onStop();
    }


}
