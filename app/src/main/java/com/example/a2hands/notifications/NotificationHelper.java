package com.example.a2hands.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a2hands.HelpRequest;
import com.example.a2hands.R;
import com.example.a2hands.User;
import com.example.a2hands.home.HomeActivity;
import com.example.a2hands.home.posts.Post;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class NotificationHelper {
    Context context;
    private Uri soundUri;
    private static String CHANNEL_ID = "CHANNEL_1";

    public NotificationHelper(Context context) {
    this.context = context;
    }

    public void sendNotificationForHelpReq(User user , Post curr_post, HelpRequest helpReq , String help_request_id){
        ///////send notification to the subscriber(who notifi is sent to)
        //fill notification obj with data from helpReq obj
        final Notification notifi = new Notification();

        String userName = user.full_name;
        notifi.publisher_name = userName;
        notifi.content = userName + " " + context.getResources().getString(R.string.sentYouAHelpRequest);
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
                                    ," " + context.getResources().getString(R.string.sharedYourPost) + " \""+curr_post.content_text+"\"");
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
        notifi.content = userName + " " + context.getResources().getString(R.string.followedYou);
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
                .setSmallIcon(R.drawable.ic_2hands_notifi2)
                .setContentTitle("2Hands")
                .setContentText(notification.content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        //Vibration
        builder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });

        //LED
        builder.setLights(Color.BLUE, 3000, 3000);

        //Ton
        builder.setSound(soundUri);

        // Creates the intent needed to show the notification
        Intent notificationIntent;
        if(notification.type.equals("FOLLOWING")){
            notificationIntent = new Intent(context, ProfileActivity.class);
            notificationIntent.putExtra("UID",notification.subscriber_id);
        }
        else {
            notificationIntent = new Intent(context, HomeActivity.class);
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

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();

            soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ context.getPackageName() + "/" + R.raw.notifi_sound_cheerful);

            channel.setSound(soundUri, audioAttributes);

            channel.setLightColor(Color.BLUE);
            channel.enableLights(true);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
