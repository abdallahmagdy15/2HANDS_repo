package com.example.a2hands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.a2hands.home.posts.Post;
import com.example.a2hands.home.posts.PostsFragment;
import com.example.a2hands.notifications.NotificationHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SharingOptions extends BottomSheetDialogFragment implements View.OnClickListener {
    private LinearLayout shareNowBtn;
    private LinearLayout quoteBtn;
    private LinearLayout shareMsgBtn;
    private LinearLayout shareExternalBtn;
    private LinearLayout copyPostLinkBtn;
    private String current_uid;
    private String shared_post_id;
    private String shared_post_location;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ChangeLocale.loadLocale(context);
        super.onCreate(savedInstanceState);
        current_uid = FirebaseAuth.getInstance().getUid();
        shared_post_id = getArguments().getString("POST_ID");
        shared_post_location = getArguments().getString("POST_LOCATION");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.sharing_bottom_sheet, container, false);

        shareNowBtn = view.findViewById(R.id.shareNowBtn);
        quoteBtn = view.findViewById(R.id.quoteBtn);
        shareMsgBtn = view.findViewById(R.id.shareMsgBtn);
        shareExternalBtn = view.findViewById(R.id.shareExternalBtn);
        copyPostLinkBtn = view.findViewById(R.id.copyPostLinkBtn);

        shareNowBtn.setOnClickListener(this);
        quoteBtn.setOnClickListener(this);
        shareMsgBtn.setOnClickListener(this);
        shareExternalBtn.setOnClickListener(this);
        copyPostLinkBtn.setOnClickListener(this);


        return view;
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == shareNowBtn.getId()){
            shareNow(shared_post_id,current_uid);
            dismiss();
        }
        else if(v.getId() == quoteBtn.getId()){
            shareWithComment(shared_post_id);
            dismiss();
        }

        else if(v.getId() == shareMsgBtn.getId()){
            dismiss();
        }

        else if(v.getId() == copyPostLinkBtn.getId()){
            dismiss();
        }

        else if(v.getId() == shareExternalBtn.getId()){
            dismiss();
        }

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    // sharing options methods
    private void shareNow(final String shared_post_id, final String current_uid){
        final CollectionReference ref = FirebaseFirestore.getInstance().collection("/posts");
        final String postid = ref.document().getId();
        final Post post= new Post();
        post.post_id = postid;
        post.user_id = current_uid;
        post.shared_id = shared_post_id;
        post.location = shared_post_location;
        PostsFragment.getUser(new Callback() {
            @Override
            public void callbackUser(final User user) {
                post.postOwner = user.full_name;
                ref.document(postid)
                        .set(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(context, getResources().getString(R.string.postSharedSuccessfully), Toast.LENGTH_LONG).show();
                        //increment Shares on the post counter
                        FirebaseDatabase.getInstance().getReference("counter").child(shared_post_id )
                                .child("shares_count").runTransaction(new Transaction.Handler() {
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
                        //end update counter for shares
                        NotificationHelper nh = new NotificationHelper(context);
                        nh.sendSharingNotifi(user,shared_post_id);
                    }
                });
            }
        },current_uid);
    }
    private void shareWithComment(final String shared_post_id){
        Intent i = new Intent(context , CreatePostActivity.class);
        i.putExtra("shared_post_id",shared_post_id);
        context.startActivity(i);
    }
    private void shareInMessage(){

    }
    private void copyPostLink(){

    }
    private void shareExternal(){

    }
}
