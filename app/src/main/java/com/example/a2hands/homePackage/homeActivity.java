package com.example.a2hands.homePackage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;

import com.example.a2hands.CreatePost;
import com.example.a2hands.Notification;
import com.example.a2hands.NotificationFragment;
import com.example.a2hands.Post;
import com.example.a2hands.R;
import com.example.a2hands.SearchFragment;
import com.example.a2hands.dummy.DummyContent;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class homeActivity extends AppCompatActivity implements SearchFragment.OnFragmentInteractionListener, PostFragment.OnListFragmentInteractionListener,  HomeFragment.OnFragmentInteractionListener,  BottomNavigationView.OnNavigationItemSelectedListener , NotificationFragment.OnListFragmentInteractionListener {

    private BottomNavigationView nav;
    private int navItemId;
    private BadgeDrawable badge;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        nav = findViewById(R.id.bottom_navigation);
        nav.setOnNavigationItemSelectedListener(this);

        badge = nav.getOrCreateBadge(nav.getMenu().getItem(3).getItemId());
        badge.setVisible(false);


        FirebaseDatabase.getInstance().getReference("notifications")
                .orderByChild("subscriber_id")
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getChildrenCount() > 0){
                            badge.setVisible(true);
                            badge.setNumber(( (int)dataSnapshot.getChildrenCount()));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
        navigateHome();
    }

    public void navigateHome(){
                final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.homeFragment,new HomeFragment()).addToBackStack("home");
                ft.commit();

    }
    public void navigateSearch(){
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.homeFragment,new SearchFragment()).addToBackStack(null);
        ft.commit();
    }

    public void navigateCreatePost(){
        Intent intent = new Intent(this, CreatePost.class);
        intent.putExtra("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
        startActivity(intent);
    }
    void navigateNotification(){
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.homeFragment,new NotificationFragment()).addToBackStack(null);
        ft.commit();
        badge.clearNumber();
        badge.setVisible(false);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int pos =0;
        navItemId = item.getItemId();
        for (int i = 0; i < nav.getMenu().size(); i++) {
            if(navItemId == nav.getMenu().getItem(i).getItemId()){
                pos = i;
                break;
            }
        }

        switch (pos) {
            case 0: navigateHome();
                break;
            case 1:navigateSearch() ;
                break;
            case 2:navigateCreatePost() ;
                break;
            case 3: navigateNotification();
                break;
            case 4:
                break;
        }
        return true;
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onListFragmentInteraction(Post item) {

    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

    }
}
