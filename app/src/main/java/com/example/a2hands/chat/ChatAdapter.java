package com.example.a2hands.chat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Constraints;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a2hands.ImagePreview;
import com.example.a2hands.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyHolder>{

    private static final int MSG_TYPE_LEFT=0;
    private static final int MSG_TYPE_RIGHT=1;

    private Context context;
    private FloatingActionButton scrollDownBtn;
    private List<Chat> chatList;
    private String imageURI;
    private String hisUid;

    private LinearLayoutManager linearLayoutManager;

    FirebaseUser user;

    public ChatAdapter(Context context, List<Chat> chatList, String imageURI,
                       String hisUid, LinearLayoutManager linearLayoutManager,
                       FloatingActionButton scrollDownBtn)
    {
        this.context = context;
        this.chatList = chatList;
        this.imageURI = imageURI;
        this.hisUid = hisUid;
        this.linearLayoutManager = linearLayoutManager;
        this.scrollDownBtn = scrollDownBtn;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==MSG_TYPE_RIGHT){
            View view= LayoutInflater.from(context).inflate(R.layout.row_chat_right,parent,false);
            return new MyHolder(view);
        }else {
            View view= LayoutInflater.from(context).inflate(R.layout.row_chat_left,parent,false);
            return new MyHolder(view);
        }
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {

        final String message = chatList.get(position).getMessage();
        String timestamp = chatList.get(position).getTimestamp();
        final String messageImagee = chatList.get(position).getMessageImage();

        if(chatList.get(position).getIsDeleted()){
            holder.messageImage.setVisibility(View.GONE);
            holder.message.setVisibility(View.GONE);
            holder.deletedMessage.setVisibility(View.VISIBLE);
            holder.deletedMessage.setPaddingRelative(12, 8, 8, 0);
            holder.deletedMessage.getLayoutParams().width = Constraints.LayoutParams.WRAP_CONTENT;
        }
        else if(messageImagee.equals("")){
            holder.deletedMessage.setVisibility(View.GONE);
            holder.messageImage.setVisibility(View.GONE);
            holder.message.setVisibility(View.VISIBLE);
            holder.message.setPaddingRelative(12, 8, 8, 0);
            holder.message.setText(message);
            holder.message.getLayoutParams().width = Constraints.LayoutParams.WRAP_CONTENT;

        } else if(message.equals("")){
            holder.deletedMessage.setVisibility(View.GONE);
            holder.message.setVisibility(View.GONE);
            holder.messageImage.setVisibility(View.VISIBLE);
            Picasso.get().load(Uri.parse(messageImagee)).into(holder.messageImage);
        } else {
            holder.deletedMessage.setVisibility(View.GONE);
            holder.message.setVisibility(View.VISIBLE);
            holder.messageImage.setVisibility(View.VISIBLE);
            holder.message.getLayoutParams().width = Constraints.LayoutParams.MATCH_CONSTRAINT;
            holder.message.setPaddingRelative(8, 8, 8, 0);
            holder.message.setText(message);
            Picasso.get().load(Uri.parse(messageImagee)).into(holder.messageImage);
        }

        //set text of last message seen TextView of sender
        if(position==chatList.size()-1 && chatList.get(position).getSender().equals(user.getUid())){
            holder.isSeen.setVisibility(View.VISIBLE);

            if (chatList.get(position).getIsSeen()){
                holder.isSeen.setText(context.getResources().getString(R.string.seen));
            }else {
                holder.isSeen.setText(context.getResources().getString(R.string.delivered));
            }
        }else {
            holder.isSeen.setVisibility(View.GONE);
        }

        //for showing or hiding left message imageView
        if(position - 1 != -1 && holder.otherProfileImage != null &&
                chatList.get(position - 1).getReceiver().equals(chatList.get(position).getReceiver()))
        {
            holder.otherProfileImage.setVisibility(View.INVISIBLE);
        }else if(position - 1 != -1 && holder.otherProfileImage != null){
            holder.otherProfileImage.setVisibility(View.VISIBLE);
        }

        //for spacing between messages types (RIGHT or LEFT)
        if (position - 1 != -1 && getItemViewType(position) == MSG_TYPE_RIGHT &&
                getItemViewType(position-1) == MSG_TYPE_LEFT
                || position - 1 != -1 && getItemViewType(position) == MSG_TYPE_LEFT &&
                getItemViewType(position-1) == MSG_TYPE_RIGHT)
        {
            holder.messageLayout.setPaddingRelative(0,16,10,2);
        }else{
            holder.messageLayout.setPaddingRelative(0,0,10,2);
        }

        //change the visibility of scrollDown button
        if(chatList.size() - 2 != -1 && position >= chatList.size() - 2){
            scrollDownBtn.setVisibility(View.GONE);
        }else {
            scrollDownBtn.setVisibility(View.VISIBLE);
        }


        holder.time.setText(timestamp);
        try{
            Picasso.get().load(imageURI).into(holder.otherProfileImage);
        }
        catch (Exception e){
            //handle errors
        }

        //long click to show delete dialog
        holder.messageLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(chatList.get(position).getSender().equals(user.getUid())) {
                    AlertDialog.Builder builder= new AlertDialog.Builder(context);
                    builder.setTitle(context.getResources().getString(R.string.delete));
                    builder.setMessage(context.getResources().getString(R.string.areYouSureToDeleteThisMessage));
                    builder.setPositiveButton(context.getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteMessage(position);
                            holder.messageImage.setVisibility(View.GONE);
                            holder.message.setVisibility(View.GONE);
                        }
                    });
                    builder.setNegativeButton(context.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                }
                return false;
            }
        });

        holder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chatList.get(position).getSender().equals(user.getUid())) {
                    if(holder.isSeen.getVisibility() == View.GONE){
                        holder.isSeen.setVisibility(View.VISIBLE);

                        linearLayoutManager.scrollToPosition(position);
                        if (chatList.get(position).getIsSeen()){
                            holder.isSeen.setText(context.getResources().getString(R.string.seen));
                        }else {
                            holder.isSeen.setText(context.getResources().getString(R.string.delivered));
                        }
                    } else if(holder.isSeen.getVisibility() == View.VISIBLE
                            && position != chatList.size()-1)
                    {
                        holder.isSeen.setVisibility(View.GONE);
                        linearLayoutManager.scrollToPosition(position);
                    }
                }
            }
        });

        holder.messageImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String imagePath = chatList.get(position).getMessageImage();
                final Intent intent = new Intent(context, ImagePreview.class);
                intent.putExtra("IMAGE_PATH",imagePath);
                context.startActivity(intent);
            }
        });
    }

    private void deleteMessage(int position) {

        final String myUid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        String MSG_ID = chatList.get(position).getMSGID();

        DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("chatList")
                .child(myUid)
                .child(hisUid)
                .child("messages");

        DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("chatList")
                .child(hisUid)
                .child(myUid)
                .child("messages");

        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    //ds.getRef().removeValue();
                    Chat chat = ds.getValue(Chat.class);

                    if(chat.getMSGID().equals(MSG_ID)){
                        HashMap<String,Object> hashMap=new HashMap<>();
                        hashMap.put("isDeleted",true);
                        ds.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    //ds.getRef().removeValue();
                    Chat chat = ds.getValue(Chat.class);

                    if(chat.getMSGID().equals(MSG_ID)){
                        HashMap<String,Object> hashMap=new HashMap<>();
                        hashMap.put("isDeleted",true);
                        ds.getRef().updateChildren(hashMap);
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

        ImageView otherProfileImage, messageImage;
        TextView message, deletedMessage, time, isSeen;
        androidx.constraintlayout.widget.ConstraintLayout messageLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            otherProfileImage =itemView.findViewById(R.id.profileIv);
            messageImage =itemView.findViewById(R.id.messageImage);
            message =itemView.findViewById(R.id.messageTv);
            deletedMessage =itemView.findViewById(R.id.deletedMessageTv);
            time =itemView.findViewById(R.id.messageTime);
            isSeen =itemView.findViewById(R.id.isSeen);
            messageLayout=itemView.findViewById(R.id.messageLayout);
        }
    }
}