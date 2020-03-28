package com.example.a2hands.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a2hands.HelpRequest;
import com.example.a2hands.R;
import com.example.a2hands.User;
import com.example.a2hands.home.homeActivity;
import com.example.a2hands.home.posts.Post;
import com.example.a2hands.notifications.MyNotificationRecyclerViewAdapter;
import com.example.a2hands.notifications.Notification;
import com.example.a2hands.profile.ProfileActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotificationHelper {
    Context context;
    private static String CHANNEL_ID="CHANNEL_1";

    public NotificationHelper(Context context) {
    this.context = context;
    }

    public void sendNotificationForHelpReq(User user , Post curr_post, HelpRequest helpReq , String help_request_id){
        ///////send notification to the subscriber(who notifi is sent to)
        //fill notification obj with data from helpReq obj
        final Notification notifi = new Notification();

        String userName = user.full_name;
        notifi.publisher_name = userName;
        notifi.content = userName + " sent you a help request";
        notifi.subscriber_id = helpReq.subscriber_id;
        notifi.publisher_id = helpReq.publisher_id;
        notifi.publisher_pic = user.profile_pic;
        notifi.type = "HELP_REQUEST";
        notifi.help_request_id = help_request_id;
        notifi.post_id = curr_post.post_id;
        notifi.date = new Date();
        /////save notifi obj to realtime
        //push empty record and get its key
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("notifications");
        notifi.notification_id = ref.push().getKey();
        //push the notifi obj to this id
        ref.child(notifi.notification_id).setValue(notifi);
    }


    public void sendReactOnPostNotification(User publisheUser , String type , Post curr_post, String content){
        ///////send notification to the subscriber(who notifi is sent to)
        //fill notification obj with data from helpReq obj
        final Notification notifi = new Notification();

        String userName = publisheUser.full_name;
        notifi.publisher_name = userName;
        notifi.content = userName + content;
        notifi.subscriber_id = curr_post.user_id;
        notifi.publisher_id = publisheUser.user_id;
        notifi.publisher_pic = publisheUser.profile_pic;
        notifi.type = type;
        notifi.post_id = curr_post.post_id;
        notifi.date = new Date();
        /////save notifi obj to realtime
        //push empty record and get its key
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("notifications");
        notifi.notification_id = ref.push().getKey();
        //push the notifi obj to this id
        ref.child(notifi.notification_id).setValue(notifi);
    }

    public void sendSharingNotifi(final User user , String sharedPostId){
        //get Post data
        FirebaseFirestore.getInstance().collection("posts")
                .document(sharedPostId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.getResult() != null){
                            Post curr_post = task.getResult().toObject(Post.class);
                            sendReactOnPostNotification(user,"SHARE_POST",curr_post
                                    ," Shared your post \""+curr_post.content_text+"\"");
                        }
                    }
                });
    }

    public void sendFollowedNotification(User publisherUser ,String subscriberId){
        ///////send notification to the subscriber(who notifi is sent to)
        //fill notification obj with data from helpReq obj
        final Notification notifi = new Notification();
        String userName = publisherUser.full_name;
        notifi.publisher_name = userName;
        notifi.content = userName + " followed you";
        notifi.subscriber_id = subscriberId;
        notifi.publisher_id = publisherUser.user_id;
        notifi.publisher_pic = publisherUser.profile_pic;
        notifi.type = "FOLLOWING";
        notifi.date = new Date();
        /////save notifi obj to realtime
        //push empty record and get its key
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("notifications");
        notifi.notification_id = ref.push().getKey();
        //push the notifi obj to this id
        ref.child(notifi.notification_id).setValue(notifi);
    }

    //build notification
    public void buildNotification(Notification notification,int id){
        createNotificationChannel();

        // Builds your notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_2hands_notification)
                .setContentTitle("2Hands")
                .setContentText(notification.content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(uri);

        // Creates the intent needed to show the notification
        Intent notificationIntent;
        if(notification.type.equals("FOLLOWING")){
            notificationIntent = new Intent(context, ProfileActivity.class);
            notificationIntent.putExtra("UID",notification.subscriber_id);
        }
        else {
            notificationIntent = new Intent(context, homeActivity.class);
        }

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(id , builder.build());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel Name";
            String description = "Channel Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void getNotifications(final RecyclerView recyclerView){
        //get notifications of current user from realtime
        FirebaseDatabase.getInstance().getReference("notifications")
                .orderByChild("subscriber_id").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Notification> notifis  = new ArrayList<>();

                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                            notifis.add(snapshot.getValue(Notification.class));
                        }
                        recyclerView.setAdapter(new MyNotificationRecyclerViewAdapter(notifis));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
