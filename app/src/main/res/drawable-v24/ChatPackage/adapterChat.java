package com.example.a2hands.ChatPackage;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a2hands.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class adapterChat extends RecyclerView.Adapter<adapterChat.MyHolder>{

    private static final int MSG_TYPE_LEFT=0;
    private static final int MSG_TYPE_RIGHT=1;

    Context context;
    List<com.example.a2hands.ChatPackage.Chat> chatList;
    String imageURI;

    FirebaseUser user;


    public adapterChat(Context context, List<com.example.a2hands.ChatPackage.Chat> chatList, String imageURI) {
        this.context = context;
        this.chatList = chatList;
        this.imageURI = imageURI;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==MSG_TYPE_RIGHT){
            View view= LayoutInflater.from(context).inflate(R.layout.row_chat_right_ltr,parent,false);
            return new MyHolder(view);
        }else {
            View view= LayoutInflater.from(context).inflate(R.layout.row_chat_left_ltr,parent,false);
            return new MyHolder(view);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {
        String message = chatList.get(position).getMessage();
        String timestamp = chatList.get(position).getTimestamp();
        holder.message.setText(message);
        holder.Time.setText(timestamp);
        try{
            Picasso.get().load(imageURI).into(holder.otherProfileImage);
        }
        catch (Exception e){

        }
        // click to show delete dialog
        holder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder= new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this message?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMassage(position);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });
        if(position==chatList.size()-1){
            if (chatList.get(position).getIsSeen()){
                holder.isSeen.setText("seen");
            }else {
                holder.isSeen.setText("Delivered");
            }
        }else {
            holder.isSeen.setVisibility(View.GONE);
        }
    }

    private void deleteMassage(int position) {
        final String myUid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        String msgTimeStamp =chatList.get(position).getTimestamp();
        DatabaseReference dbref= FirebaseDatabase.getInstance().getReference("Chats");
        Query query =dbref.orderByChild("Timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    if (ds.child("Sender").getValue().equals(myUid)){
                        //ds.getRef().removeValue();
                        HashMap<String,Object> hashMap=new HashMap<>();
                        hashMap.put("Message","This message was Deleted...");
                        ds.getRef().updateChildren(hashMap);
                        Toast.makeText(context, "Massage deleted ...", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(context, "You can delete only your massages...", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        user= FirebaseAuth.getInstance().getCurrentUser();
        if(chatList.get(position).getSender().equals(user.getUid())){
            return MSG_TYPE_RIGHT;
        }else {
            return MSG_TYPE_LEFT;
        }
    }

    class MyHolder extends RecyclerView.ViewHolder{


        ImageView otherProfileImage;
        TextView message,Time,isSeen;
        androidx.constraintlayout.widget.ConstraintLayout messageLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            otherProfileImage =itemView.findViewById(R.id.profileIv);
            message =itemView.findViewById(R.id.messageTv);
            Time =itemView.findViewById(R.id.messageTime);
            isSeen =itemView.findViewById(R.id.isSeen);
            messageLayout=itemView.findViewById(R.id.messageLayout);
        }
    }
}

