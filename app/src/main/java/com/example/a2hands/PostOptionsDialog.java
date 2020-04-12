package com.example.a2hands;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

public class PostOptionsDialog extends BottomSheetDialogFragment implements View.OnClickListener {

    private LinearLayout saveBtn;
    private LinearLayout disableBtn;
    private LinearLayout deleteBtn;
    private LinearLayout hideBtn;
    private LinearLayout muteBtn;
    private LinearLayout blockBtn;
    private LinearLayout reportBtn;
    private String current_uid;
    private String post_id;
    private String post_user_id;
    private Context context;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        current_uid = FirebaseAuth.getInstance().getUid();
        post_id = getArguments().getString("post_id");
        post_user_id = getArguments().getString("post_user_id");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.post_options_bottom_sheet, container, false);

        saveBtn    = view.findViewById(R.id.postOptionSave);
        disableBtn = view.findViewById(R.id.postOptionDisable);
        deleteBtn  = view.findViewById(R.id.postOptionDelete);
        hideBtn    = view.findViewById(R.id.postOptionHide);
        muteBtn    = view.findViewById(R.id.postOptionMute);
        blockBtn   = view.findViewById(R.id.postOptionBlock);
        reportBtn  = view.findViewById(R.id.postOptionReport);


        //check user privileges
        if(current_uid.equals(post_user_id)){
            muteBtn.setVisibility(View.GONE);
            blockBtn.setVisibility(View.GONE);
            reportBtn.setVisibility(View.GONE);
        }
        else{
            disableBtn.setVisibility(View.GONE);
            deleteBtn.setVisibility(View.GONE);
        }

        saveBtn    .setOnClickListener(this);
        disableBtn .setOnClickListener(this);
        deleteBtn  .setOnClickListener(this);
        hideBtn    .setOnClickListener(this);
        muteBtn    .setOnClickListener(this);
        blockBtn   .setOnClickListener(this);
        reportBtn  .setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == saveBtn.getId()){
            savePost();
            dismiss();
        }
        else if(v.getId() == disableBtn.getId()){
            disableReact();
            dismiss();
        }

        else if(v.getId() == deleteBtn.getId()){
            deletePost();
            dismiss();
        }

        else if(v.getId() == hideBtn.getId()){
            hidePost();
            dismiss();
        }

        else if(v.getId() == muteBtn.getId()){
            muteUser();
            dismiss();
        }
        else if(v.getId() == blockBtn.getId()){
            blockUser();
            dismiss();
        }
        else if(v.getId() == reportBtn.getId()){
            reportPost();
            dismiss();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private void savePost(){
        FirebaseDatabase.getInstance().getReference("saved_posts").child(current_uid).child(post_id).setValue(true)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context,getResources().getString(R.string.postIsSavedSuccessfully),Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void disableReact(){
        FirebaseFirestore.getInstance().collection("posts").document(post_id).update("state",false)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context,getResources().getString(R.string.postIsDisabledSuccessfully),Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void deletePost(){
        FirebaseFirestore.getInstance().collection("posts").document(post_id).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context,getResources().getString(R.string.postIsDeletedSuccessfully),Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void hidePost(){
        FirebaseDatabase.getInstance().getReference("hidden_posts").child(current_uid).child(post_id).setValue(true)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context,getResources().getString(R.string.postIsHiddenSuccessfully),Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void muteUser(){
        FirebaseDatabase.getInstance().getReference("muted_users").child(current_uid).child(post_user_id).setValue(true)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context,getResources().getString(R.string.userIsMutedSuccessfully),Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void blockUser(){
        //add to blocked users
        FirebaseDatabase.getInstance().getReference("blocked_users").child(current_uid).child(post_user_id).setValue(true)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context,getResources().getString(R.string.userIsBlockedSuccessfully),Toast.LENGTH_LONG).show();
                    }
                });
        FollowingHelper fh = new FollowingHelper(current_uid,post_user_id,context);
        fh.unfollow();
    }

    private void reportPost(){

    }
}
