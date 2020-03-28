package com.example.a2hands.notifications;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NotificationsService extends IntentService {

    public NotificationsService() {
        super("NotificationsService");
    }

    @Override
    public int onStartCommand(@Nullable final Intent intent, int flags, int startId) {

        //get notifications of current user from realtime
        FirebaseDatabase.getInstance().getReference("notifications")
                .orderByChild("subscriber_id").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        NotificationHelper nh = new NotificationHelper(NotificationsService.this);
                        int i=0;
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                            Notification notification =snapshot.getValue(Notification.class);
                            nh.buildNotification(notification,i);
                            i++;
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
        }
    }

}
