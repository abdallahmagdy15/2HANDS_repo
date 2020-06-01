package com.example.a2hands.notifications;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.a2hands.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.todkars.shimmer.ShimmerRecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class NotificationFragment extends Fragment {

    private List<Notification> notifis  = new ArrayList<>();

    private MyNotificationRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    private ShimmerRecyclerView mShimmerRecyclerView;

    public NotificationFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        mShimmerRecyclerView.showShimmer();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification_list, container, false);

        mShimmerRecyclerView = view.findViewById(R.id.notificationRecyclerView_shimmer);
        recyclerView = view.findViewById(R.id.notificationRecyclerView);

        Context context = view.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setHasFixedSize(true);
        adapter = new MyNotificationRecyclerViewAdapter(notifis);
        recyclerView.setAdapter(adapter);

        getNotifications();

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        setNotificationsSeen();

    }

    private void getNotifications(){
        //get notifications of current user from realtime
        FirebaseDatabase.getInstance().getReference("notifications")
                .orderByChild("subscriber_id").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .limitToLast(30)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                            notifis.add(snapshot.getValue(Notification.class));
                        }
                        Collections.reverse(notifis);
                        adapter.notifyDataSetChanged();
                        mShimmerRecyclerView.setVisibility(View.GONE);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void setNotificationsSeen(){
        FirebaseDatabase.getInstance().getReference("notifications").orderByChild("is_seen")
                .equalTo(false).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    FirebaseDatabase.getInstance().getReference("notifications")
                            .child(ds.getKey()).child("is_seen").setValue(true);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
