package com.example.a2hands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.a2hands.homePackage.PostFragment;
import com.example.a2hands.homePackage.RatingFragment;
import com.example.a2hands.homePackage.RatingsActivity;
import com.example.a2hands.homePackage.dummy.DummyContent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import static java.lang.StrictMath.round;


public class ProfileActivity extends AppCompatActivity  implements PostFragment.OnListFragmentInteractionListener , RatingFragment.OnListFragmentInteractionListener {

    private static final int NUM_PAGES = 2;
    private ViewPager mPager;
    private PagerAdapter pagerAdapter;
    ImageView coverPhoto;
    ImageView profilePic;
    Button profileFollowBtn;
    Button profileEditBtn;
    ImageView profileMessaging;
    private StorageReference mStorageRef;
    private FirebaseFirestore db;
    public String uid;
    TextView profileName;
    TextView jobTitle;
    TextView country_region;
    TextView profileBio;
    TextView profileRate;
    RatingBar ratingBar;
    TextView ratings_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //declare
        Toolbar toolbar = findViewById(R.id.profileToolbar);
        Intent intent = getIntent();

        //initiate
        mStorageRef = FirebaseStorage.getInstance().getReference();
        uid = intent.getStringExtra("uid");
        mPager = findViewById(R.id.profilePostsContainer);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        coverPhoto = findViewById(R.id.coverPhoto);
        profilePic = findViewById(R.id.profilePic);
        db = FirebaseFirestore.getInstance();
        profileBio = findViewById(R.id.profileBio);
        profileName = findViewById(R.id.profileName);
        jobTitle = findViewById(R.id.jobTitle);
        country_region = findViewById(R.id.country_region);
        profileRate = findViewById(R.id.profileRate);
        profileFollowBtn = findViewById(R.id.profileFollowBtn);
        ratingBar = findViewById(R.id.ratingBarGet);
        ratings_count = findViewById(R.id.ratings_count);
        profileEditBtn = findViewById(R.id.profileEditBtn);
        profileMessaging = findViewById(R.id.profileMessaging);

        // setup
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mPager.setAdapter(pagerAdapter);

        if(uid != null){
            profileFollowBtn.setVisibility(View.VISIBLE);
            profileMessaging.setVisibility(View.VISIBLE);
        }
        else {
            uid= FirebaseAuth.getInstance().getCurrentUser().getUid();
            profileEditBtn.setVisibility(View.VISIBLE);
        }
        loadUserProfile();

    }
    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    void loadUserProfile(){
        db.collection("users/").document(uid)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                User user = task.getResult().toObject(User.class);
                loadPhotos(profilePic,"Profile_Pics/"+uid+"/"+user.profile_pic );
                loadPhotos(coverPhoto,"Profile_Covers/"+user.profile_cover);
                profileName.setText(user.first_name+" "+user.last_name);
                jobTitle.setText(user.job_title);
                country_region.setText(user.country+", "+user.region);
                profileBio.setText(user.bio);
                DecimalFormat df = new DecimalFormat("##.##");
                profileRate.setText(df.format(user.rate));
                ratingBar.setRating( (float)user.rate);
                NumberFormat nf = NumberFormat.getInstance();
                nf.setGroupingUsed(true);
                ratings_count.setText( nf.format(user.ratings_count) );

            }
        });
    }
    void loadPhotos(final ImageView imgV , String path){

        mStorageRef.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
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
    public void onListFragmentInteraction(Post item) {

    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0){
                if(uid != null){
                    Fragment frg = new PostFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("uid", uid);
                    bundle.putString("for", "profile");
                    frg.setArguments(bundle);
                    return frg;
                }else{
                    Fragment frg = new PostFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    bundle.putString("for", "profile");
                    frg.setArguments(bundle);
                    return frg;
                }
            }
            else
            {

                Fragment frg = new RatingFragment();
                Bundle b = new Bundle();
                b.putString("uid",uid);
                b.putString("for","profile");
                frg.setArguments(b);
                return frg;
            }

        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
        @Override
        public CharSequence getPageTitle(int position) {
            if(position == 0)
                return "Posts" ;
            else
                return "Reviews";
        }
    }

}
