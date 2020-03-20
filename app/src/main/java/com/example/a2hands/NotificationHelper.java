package com.example.a2hands;

import androidx.annotation.NonNull;

import com.example.a2hands.home.PostsPackage.Post;
import com.example.a2hands.notifications.Notification;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class NotificationHelper {
    public NotificationHelper() {
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
        notifi.type = "FOLLOWED";
        notifi.date = new Date();
        /////save notifi obj to realtime
        //push empty record and get its key
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("notifications");
        notifi.notification_id = ref.push().getKey();
        //push the notifi obj to this id
        ref.child(notifi.notification_id).setValue(notifi);
    }
}
