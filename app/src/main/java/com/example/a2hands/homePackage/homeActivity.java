package com.example.a2hands.homePackage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.example.a2hands.CreatePost;
import com.example.a2hands.LoginActivity;
import com.example.a2hands.NotificationFragment;
import com.example.a2hands.Post;
import com.example.a2hands.ProfileActivity;
import com.example.a2hands.R;
import com.example.a2hands.SearchFragment;
import com.example.a2hands.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class homeActivity extends AppCompatActivity implements SearchFragment.OnFragmentInteractionListener, PostFragment.OnListFragmentInteractionListener,  BottomNavigationView.OnNavigationItemSelectedListener , NotificationFragment.OnListFragmentInteractionListener {

    private BottomNavigationView nav;
    private int navItemId;
    private BadgeDrawable badge;
    Spinner catsSpinner;
    String[] catsStrings;
    CircleImageView profile_image ;
    RelativeLayout homeTopMenu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        nav = findViewById(R.id.bottom_navigation);
        nav.setOnNavigationItemSelectedListener(this);

        badge = nav.getOrCreateBadge(nav.getMenu().getItem(3).getItemId());
        badge.setVisible(false);

        homeTopMenu = findViewById(R.id.homeTopMenu);

        //category spinner declaration
        catsStrings = getResources().getStringArray(R.array.categories);
        catsSpinner = findViewById(R.id.catsSpinner);
        profile_image = findViewById(R.id.profile_image);
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //load current user main pic in home top menu
        FirebaseFirestore.getInstance().collection("users/").document(uid)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                User user = task.getResult().toObject(User.class);
                FirebaseStorage.getInstance().getReference().child("Profile_Pics/"+uid+"/"+user.profile_pic).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri.toString()).into(profile_image);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });
            }
        });


        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(homeActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        loadPostsFrag();

        //check notifications
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
        homeTopMenu.setVisibility(View.VISIBLE);
        loadPostsFrag();

    }
    public void navigateSearch(){
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.homeFrag,new SearchFragment()).addToBackStack(null);
        ft.commit();
    }

    public void navigateCreatePost(){
        Intent intent = new Intent(this, CreatePost.class);
        intent.putExtra("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
        startActivity(intent);
    }
    void navigateNotification(){
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.homeFrag,new NotificationFragment()).addToBackStack(null);
        ft.commit();
        badge.clearNumber();
        badge.setVisible(false);

    }

    public void loadPostsFrag(){
        catsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Fragment frg = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putString("category", catsStrings[position]);
                bundle.putString("for","home");
                frg.setArguments(bundle);
                final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.homeFrag,frg).addToBackStack(null);
                ft.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        catsSpinner.setSelection(0);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        homeTopMenu.setVisibility(View.GONE);
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
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class));
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
    public void onListFragmentInteraction(int x) {

    }
}
