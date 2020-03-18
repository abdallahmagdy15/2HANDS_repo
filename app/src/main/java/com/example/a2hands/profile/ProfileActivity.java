package com.example.a2hands.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;


import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.a2hands.FollowingHelper;
import com.example.a2hands.R;
import com.example.a2hands.User;
import com.example.a2hands.home.PostsPackage.PostsFragment;
import com.example.a2hands.rating.RatingFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.NumberFormat;


public class ProfileActivity extends AppCompatActivity
{

    private static final int NUM_PAGES = 2;
    private ViewPager mPager;
    private PagerAdapter pagerAdapter;
    ImageView coverPhoto;
    ImageView profilePic;
    CardView profileFollowBtn;
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
    String curr_uid;
    TextView profileFollowingsCount;
    TextView profileFollowersCount;
    TextView profileFollowBtnTxt;
    String UserName;

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
        curr_uid = FirebaseAuth.getInstance().getUid();
        profileFollowingsCount = findViewById(R.id.profileFollowingsCount);
        profileFollowersCount = findViewById(R.id.profileFollowersCount);
        profileFollowBtnTxt = findViewById(R.id.profileFollowBtnTxt);

        // setup
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mPager.setAdapter(pagerAdapter);

        if(uid != null){//if redirected to this profile by uid
            if(uid.equals(curr_uid)){
                profileEditBtn.setVisibility(View.VISIBLE);
            }
            else {
                setFollowListener();
            }
        }
        else {//if not by uid then its by curr uid
            uid = curr_uid;
            profileEditBtn.setVisibility(View.VISIBLE);
        }
        loadUserProfile();
        setLoadingFollowersListener();
        setLoadingFollowingsListener();
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
    void setLoadingFollowersListener(){
        profileFollowersCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ProfileActivity.this,FollowersActivity.class);
                i.putExtra("uid",uid);
                startActivity(i);
            }
        });
    }
    void setLoadingFollowingsListener(){
        profileFollowingsCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ProfileActivity.this,FollowingsActivity.class);
                i.putExtra("uid",uid);
                startActivity(i);
            }
        });
    }
    void setFollowListener(){
        profileFollowBtn.setVisibility(View.VISIBLE);
        profileMessaging.setVisibility(View.VISIBLE);
        //check if follow or un follow
        FirebaseDatabase.getInstance().getReference("followings").child(curr_uid).child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //if curr_uid following uid
                        if (dataSnapshot.exists()) {
                            //change style of follow btn
                            profileFollowBtnTxt.setTextColor(getResources().getColor(R.color.colorPureWhite));
                            profileFollowBtn.setCardBackgroundColor(getResources().getColor(R.color.colorAccent));
                            profileFollowBtnTxt.setText("Following");

                            profileFollowBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //confirm unfollow
                                    new AlertDialog.Builder(ProfileActivity.this)
                                            .setTitle("Are you sure you want to unfollow ?")
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    FollowingHelper fh = new FollowingHelper(curr_uid,uid);
                                                    fh.unfollow();                                                }
                                            })
                                            .setNegativeButton(android.R.string.no, null).show();
                                }
                            });
                        } else {
                            //change style of follow btn
                            profileFollowBtnTxt.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                            profileFollowBtn.setCardBackgroundColor(getResources().getColor(R.color.colorWhite));
                            profileFollowBtnTxt.setText("Follow");

                            profileFollowBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    FollowingHelper fh = new FollowingHelper(curr_uid,uid);
                                    fh.follow();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }
    void loadUserProfile(){
        db.collection("/users").document(uid)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                User user = task.getResult().toObject(User.class);
                try {
                    loadPhotos(profilePic,"Profile_Pics/"+uid+"/"+user.profile_pic );
                } catch (Exception e) {
                    e.printStackTrace();
                }
                loadPhotos(coverPhoto,"Profile_Covers/"+user.profile_cover);
                UserName = user.first_name+" "+user.last_name;
                profileName.setText(UserName );
                jobTitle.setText(user.job_title);
                String location = user.country+((user.region.equals(""))?"":", "+user.region);
                country_region.setText(location);
                profileBio.setText(user.bio);
                DecimalFormat df = new DecimalFormat("##.##");
                profileRate.setText(df.format(user.rate));
                ratingBar.setRating( (float)user.rate);
                NumberFormat nf = NumberFormat.getInstance();
                nf.setGroupingUsed(true);
                ratings_count.setText( nf.format(user.ratings_count) );

            }
        });
        //get followings count
        FirebaseDatabase.getInstance().getReference("followings").child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String count = Long.toString(dataSnapshot.getChildrenCount());
                        count = "<b>"+count+"</b> Followings";
                        profileFollowingsCount.setText(Html.fromHtml(count));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
        //get followers count
        FirebaseDatabase.getInstance().getReference("followers")
                .child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String count = Long.toString(dataSnapshot.getChildrenCount());
                        count = "<b>"+count+"</b> Followers";
                        profileFollowersCount.setText(Html.fromHtml(count));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
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

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0){
                if(uid != null){
                    Fragment frg = new PostsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("UID", uid);
                    bundle.putString("FOR", "PROFILE");
                    frg.setArguments(bundle);
                    return frg;
                }else{
                    Fragment frg = new PostsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("UID", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    bundle.putString("FOR", "PROFILE");
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
