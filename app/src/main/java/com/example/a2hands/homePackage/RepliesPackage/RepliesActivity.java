package com.example.a2hands.homePackage.RepliesPackage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a2hands.Callback;
import com.example.a2hands.R;
import com.example.a2hands.User;
import com.example.a2hands.homePackage.CommentsPackage.Comment;
import com.example.a2hands.homePackage.CommentsPackage.CommentsActivity;
import com.example.a2hands.homePackage.CommentsPackage.CommentsFragment;
import com.example.a2hands.homePackage.PostsPackage.PostFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class RepliesActivity extends AppCompatActivity implements RepliesFragment.OnListFragmentInteractionListener {

    Toolbar toolbar;
    EditText add_reply;
    ImageView commentReplyBtn;
    String postid, commentid, curr_uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replies);
        add_reply = findViewById(R.id.add_reply);
        commentReplyBtn = findViewById(R.id.commentReplyBtn);
        toolbar = findViewById(R.id.toolbar);

        //getIntent Data
        postid = getIntent().getStringExtra("post_id");
        commentid = getIntent().getStringExtra("comment_id");
        curr_uid = getIntent().getStringExtra("publisher_id");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        commentReplyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (add_reply.getText().toString().equals("")) {
                    Toast.makeText(RepliesActivity.this, "You Can’t Send Empty Reply", Toast.LENGTH_SHORT).show();
                } else
                    addReply();
            }
        });

        //load Replies
        loadReplies(postid, commentid);
    }


    //load comments
    void loadReplies(String postId, String commentId) {

        Fragment frg = new RepliesFragment();
        Bundle bundle = new Bundle();
        bundle.putString("postId", postId);
        bundle.putString("commentId", commentId);
        frg.setArguments(bundle);
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.repliesContainer, frg);
        ft.commit();

    }

    private void addReply() {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Replies").child(postid).child(commentid);
        final Reply reply = new Reply();
        reply.reply_content = add_reply.getText().toString();
        reply.post_id = postid;
        reply.comment_id = commentid;
        reply.publisher_id = curr_uid;
        reply.date = new Date();
        PostFragment.getUser(new Callback() {
            @Override
            public void callbackUser(User user) {
                reply.publisher_pic = user.profile_pic;
                reply.name = user.first_name + " " + user.last_name;
                reply.reply_id = reference.push().getKey();
                reference.push().setValue(reply);

                add_reply.setText("");
            }
        }, curr_uid);
    }

    @Override
    public void onListFragmentInteraction(Reply item) {

    }
}
