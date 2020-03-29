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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Constraints;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a2hands.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyHolder>{

    private static final int MSG_TYPE_LEFT=0;
    private static final int MSG_TYPE_RIGHT=1;

    Context context;
    List<Chat> chatList;
    String imageURI;
    String hisUid;
    String hisImage;

    FirebaseUser user;

    public ChatAdapter(Context context, List<Chat> chatList, String imageURI, String hisUid) {
        this.context = context;
        this.chatList = chatList;
        this.imageURI = imageURI;
        this.hisUid = hisUid;
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

        if(messageImagee.equals("") || message.equals("This message was Deleted...")){
            holder.messageImage.setVisibility(View.GONE);
            holder.message.setVisibility(View.VISIBLE);
            holder.message.setPaddingRelative(12, 8, 8, 0);
            holder.message.setText(message);
            holder.message.getLayoutParams().width = Constraints.LayoutParams.WRAP_CONTENT;

        } else if(message.equals("")){
            holder.message.setVisibility(View.GONE);
            holder.messageImage.setVisibility(View.VISIBLE);
            loadPhotos(holder.messageImage,"Chat_Pics/"+messageImagee);
        } else {
            holder.message.setVisibility(View.VISIBLE);
            holder.messageImage.setVisibility(View.VISIBLE);
            holder.message.getLayoutParams().width = Constraints.LayoutParams.MATCH_CONSTRAINT;
            holder.message.setPaddingRelative(8, 8, 8, 0);
            holder.message.setText(message);
            loadPhotos(holder.messageImage,"Chat_Pics/"+messageImagee);

        }
        holder.Time.setText(timestamp);
        try{
            Picasso.get().load(imageURI).into(holder.otherProfileImage);
        }
        catch (Exception e){

        }

        // click to show delete dialog
        holder.messageLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(chatList.get(position).getSender().equals(user.getUid())) {
                    AlertDialog.Builder builder= new AlertDialog.Builder(context);
                    builder.setTitle("Delete");
                    builder.setMessage("Are you sure to delete this message?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteMassage(position);
                            holder.messageImage.setVisibility(View.GONE);
                            holder.message.setVisibility(View.VISIBLE);
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
                return false;
            }
        });

        holder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chatList.get(position).getSender().equals(user.getUid())) {
                    if(holder.isSeen.getVisibility() == View.GONE){
                        holder.isSeen.setVisibility(View.VISIBLE);
                        if (chatList.get(position).getIsSeen()){
                            holder.isSeen.setText("seen");
                        }else {
                            holder.isSeen.setText("sent");
                        }
                    } else if(holder.isSeen.getVisibility() == View.VISIBLE
                                && position != chatList.size()-1)
                    {
                        holder.isSeen.setVisibility(View.GONE);
                    }
                }
            }
        });

//
        if(position==chatList.size()-1 && chatList.get(position).getSender().equals(user.getUid())){
            holder.isSeen.setVisibility(View.VISIBLE);
            if (chatList.get(position).getIsSeen()){
                holder.isSeen.setText("seen");
            }else {
                holder.isSeen.setText("sent");
            }
        }else {
            holder.isSeen.setVisibility(View.GONE);
        }

        holder.messageImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MessageImageActivity.class);
                intent.putExtra("MSGDI",chatList.get(position).getMSGID());
                intent.putExtra("myUid",chatList.get(position).getReceiver());
                intent.putExtra("hisUid",chatList.get(position).getSender());
                context.startActivity(intent);
            }
        });
    }

    private void deleteMassage(int position) {

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

        Query query1 = chatRef1.orderByChild("MSGID").equalTo(MSG_ID);
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    //ds.getRef().removeValue();
                    HashMap<String,Object> hashMap=new HashMap<>();
                    hashMap.put("Message","This message was Deleted...");
                    ds.getRef().updateChildren(hashMap);
                    Toast.makeText(context, "Massage deleted ...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Query query2 = chatRef2.orderByChild("MSGID").equalTo(MSG_ID);
        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    //ds.getRef().removeValue();
                    HashMap<String,Object> hashMap=new HashMap<>();
                    hashMap.put("Message","This message was Deleted...");
                    ds.getRef().updateChildren(hashMap);
                    Toast.makeText(context, "Massage deleted ...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void loadPhotos(final ImageView imgV , String path){
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
        mStorageRef.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                hisImage=uri.toString();
                Picasso.get().load(uri.toString()).into(imgV);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
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

        ImageView otherProfileImage,messageImage;
        TextView message,Time,isSeen;
        androidx.constraintlayout.widget.ConstraintLayout messageLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            otherProfileImage =itemView.findViewById(R.id.profileIv);
            messageImage =itemView.findViewById(R.id.messageImage);
            message =itemView.findViewById(R.id.messageTv);
            Time =itemView.findViewById(R.id.messageTime);
            isSeen =itemView.findViewById(R.id.isSeen);
            messageLayout=itemView.findViewById(R.id.messageLayout);
        }
    }
}