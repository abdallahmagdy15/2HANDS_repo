package com.example.a2hands.chat.chatlist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a2hands.chat.ChatActivity;
import com.example.a2hands.R;
import com.example.a2hands.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class AdapterChatList extends RecyclerView.Adapter<AdapterChatList.MyHolder> {

    Context context;
    List<User> usersList;
    private HashMap<String,String> lastMessageMap;
    private HashMap<String,String> SeenOfTheLastMessage;
    private FirebaseFirestore db=FirebaseFirestore.getInstance();

    public AdapterChatList(Context context, List<User> usersList) {
        this.context = context;
        this.usersList = usersList;
        lastMessageMap = new HashMap<>();
        SeenOfTheLastMessage =new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, int position) {
        //get data
        final String Uid =usersList.get(position).user_id;
        db.collection("users/").document(Uid)
            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            User user = task.getResult().toObject(User.class);
            loadPhotos(holder.profileIv,Uid);
            holder.nameTv.setText(user.full_name);
            }
        });

        String lastMessage = lastMessageMap.get(Uid);
        //set data
        if (Objects.equals(SeenOfTheLastMessage.get(Uid), "NewMessage")){
            SpannableString ss = new SpannableString(lastMessage);
            StyleSpan bold = new StyleSpan(Typeface.BOLD);
            ss.setSpan(bold,0,lastMessage.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.lastMessageTv.setText(ss);
            holder.newMessageStatusIV.setVisibility(View.VISIBLE);
        }
        else {
            holder.lastMessageTv.setText(lastMessage);
            holder.newMessageStatusIV.setVisibility(View.INVISIBLE);
        }

        //set online status of other users in Chatlist
        if (usersList.get(position).onlineStatus.equals("online")){
            holder.onlineStatusIV.setVisibility(View.VISIBLE);
        }else {
            holder.onlineStatusIV.setVisibility(View.INVISIBLE);
        }

        //handle click of user in chatlist
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start chat activity with that user
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid",Uid);
                context.startActivity(intent);
            }
        });

    }
    public void setLastMessageAndSeenMap(String userId,String lastMessage,String isSeen){
        lastMessageMap.put(userId,lastMessage);
        SeenOfTheLastMessage.put(userId,isSeen);
    }

    private void loadPhotos(final ImageView imgV, String path){
        FirebaseFirestore.getInstance().collection("users/").document(path)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    User user = task.getResult().toObject(User.class);
                    Picasso.with(context).load(Uri.parse(user.profile_pic)).into(imgV);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        ImageView profileIv, newMessageStatusIV, onlineStatusIV;
        TextView nameTv,lastMessageTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profileIv = itemView.findViewById(R.id.profileIv);
            newMessageStatusIV =itemView.findViewById(R.id.newMessageStatusIV);
            onlineStatusIV =itemView.findViewById(R.id.onlineStatusIV);
            nameTv = itemView.findViewById(R.id.nameTv);
            lastMessageTv = itemView.findViewById(R.id.lastMessageTv);
        }
    }
}
