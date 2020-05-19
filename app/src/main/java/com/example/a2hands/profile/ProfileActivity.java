package com.example.a2hands.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a2hands.ChangeLocale;
import com.example.a2hands.CreatePostActivity;
import com.example.a2hands.FollowingHelper;
import com.example.a2hands.ImagePreview;
import com.example.a2hands.R;
import com.example.a2hands.User;
import com.example.a2hands.home.posts.PostsFragment;
import com.example.a2hands.rating.RatingFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Size;


public class ProfileActivity extends AppCompatActivity {

    private static final int NUM_PAGES = 2;
    private static final int PICTURES_REQUEST = 10;
    private ViewPager mPager;
    private PagerAdapter pagerAdapter;
    ImageView coverPhoto;
    ImageView profilePic;
    CardView profileFollowBtn;
    CardView profileEditBtn;
    ImageView profileMessaging;
    private FirebaseFirestore db;
    public String uid;
    TextView profileName;
    TextView jobTitle;
    TextView country_region;
    TextView profileBio;
    TextView profileRate;
    TextView ratings_count;
    String curr_uid;
    TextView profileFollowingsCount;
    TextView profileFollowersCount;
    TextView profileFollowBtnTxt;
    String UserName;
    User user;
    NestedScrollView profile_nestedScrollView;
    ConstraintLayout profile_info_container;
    FloatingActionButton profile_addPost;
    ConstraintLayout profile_blockedStatus_container;
    TextView profile_blockedStatus_name;
    TextView profile_blockedStatus;

    KonfettiView konfettiView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeLocale.loadLocale(getBaseContext());
        setContentView(R.layout.activity_profile);

        //declare
        Toolbar toolbar = findViewById(R.id.profileToolbar);
        Intent intent = getIntent();

        //initiate
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
        ratings_count = findViewById(R.id.ratings_count);
        profileEditBtn = findViewById(R.id.profileEditBtn);
        profileMessaging = findViewById(R.id.profileMessaging);
        curr_uid = FirebaseAuth.getInstance().getUid();
        profileFollowingsCount = findViewById(R.id.profileFollowingsCount);
        profileFollowersCount = findViewById(R.id.profileFollowersCount);
        profileFollowBtnTxt = findViewById(R.id.profileFollowBtnTxt);
        profile_nestedScrollView = findViewById(R.id.profile_nestedScrollView);
        profile_info_container = findViewById(R.id.profile_info_container);
        profile_addPost = findViewById(R.id.profile_addPost);
        profile_blockedStatus_container = findViewById(R.id.profile_blockedStatus_container);
        profile_blockedStatus_name = findViewById(R.id.profile_blockedStatus_name);
        profile_blockedStatus = findViewById(R.id.profile_blockedStatus);

        konfettiView = findViewById(R.id.viewKonfetti);

        // setup
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        profile_addPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, CreatePostActivity.class));
            }
        });

        //Chat Activity
        profileMessaging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, com.example.a2hands.chat.ChatActivity.class);
                intent.putExtra("hisUid", uid);
                startActivity(intent);
            }
        });

        mPager.setAdapter(pagerAdapter);

        checkProfileStatus();

    }// end of onCreate method

    void checkProfileStatus(){
        if(uid != null){//if redirected to this profile by uid
            if(uid.equals(curr_uid)){ // if its your profile
                profileEditBtn.setVisibility(View.VISIBLE);
                profile_addPost.setVisibility(View.VISIBLE);
                loadUserProfile();
                setLoadingFollowersListener();
                setLoadingFollowingsListener();
            }
            else {// someone's profile
                checkBlockedStatus();
            }
        }
        else { // then its your profile
            uid = curr_uid;
            profileEditBtn.setVisibility(View.VISIBLE);
            profile_addPost.setVisibility(View.VISIBLE);
            loadUserProfile();
            setLoadingFollowersListener();
            setLoadingFollowingsListener();
        }

    }

    void checkBlockedStatus(){
        FirebaseDatabase.getInstance().getReference("blocked_users").child(uid).child(curr_uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //check if this user is blocking you you
                        if (dataSnapshot.exists()) {
                            profile_info_container.setVisibility(View.GONE);
                            profile_addPost.setVisibility(View.GONE);
                            profile_nestedScrollView.setVisibility(View.GONE);
                            profile_blockedStatus_container.setVisibility(View.VISIBLE);
                            loadBlockStatusData(true);
                        }
                        else {
                            checkBlockingStatus();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    void checkBlockingStatus(){
        FirebaseDatabase.getInstance().getReference("blocked_users").child(curr_uid).child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //check if this user is blocking you you
                        if (dataSnapshot.exists()) {
                            profile_info_container.setVisibility(View.GONE);
                            profile_addPost.setVisibility(View.GONE);
                            profile_nestedScrollView.setVisibility(View.GONE);
                            profile_blockedStatus_container.setVisibility(View.VISIBLE);
                            loadBlockStatusData(false);
                        }
                        else {
                            loadUserProfile();
                            setLoadingFollowersListener();
                            setLoadingFollowingsListener();
                            setFollowListener();                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    void loadBlockStatusData(boolean blocked){
        db.collection("/users").document(uid)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                user = task.getResult().toObject(User.class);
                profile_blockedStatus_name.setText(user.full_name);
                if(blocked)
                profile_blockedStatus.setText(getResources().getString(R.string.youAreBlockedFromfollowing)+" @"+user.user_name
                        + " " + getResources().getString(R.string.andViewingHisPostsAndReviews));
                else
                    profile_blockedStatus.setText("@"+user.user_name+" "+getResources().getString(R.string.isBlocked));
                profile_blockedStatus.setTextSize(24);
            }});
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem block = menu.findItem(R.id.blockUserItem);
        MenuItem mute = menu.findItem(R.id.muteUserItem);

        if(!uid.equals(curr_uid)) {
            FirebaseDatabase.getInstance().getReference("blocked_users").child(curr_uid).child(uid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                block.setTitle(R.string.unblock);
                                block.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        unblockUser();
                                        return false;
                                    }
                                });
                            } else {
                                block.setTitle(R.string.blockUser);
                                block.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        blockUser();
                                        return false;
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
            FirebaseDatabase.getInstance().getReference("muted_users").child(curr_uid).child(uid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                mute.setTitle(R.string.unmute);
                                mute.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        unmuteUser();
                                        return false;
                                    }
                                });
                            } else {
                                mute.setTitle(R.string.muteUser);
                                mute.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        muteUser();
                                        return false;
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });

        }
        return super.onPrepareOptionsMenu(menu);
    }

    public void blockUser(){
        FirebaseDatabase.getInstance().getReference("blocked_users").child(curr_uid).child(uid).setValue(true)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(ProfileActivity.this,getResources().getString(R.string.userIsBlockedSuccessfully),Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void muteUser(){
        FirebaseDatabase.getInstance().getReference("muted_users").child(curr_uid).child(uid).setValue(true)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(ProfileActivity.this,getResources().getString(R.string.userIsMutedSuccessfully),Toast.LENGTH_LONG).show();
                    }
                });
    }

    void unblockUser(){
        FirebaseDatabase.getInstance().getReference("blocked_users").child(curr_uid).child(uid).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(ProfileActivity.this,getResources().getString(R.string.unblock)
                                + " " + getResources().getString(R.string.done),Toast.LENGTH_LONG).show();
                    }
                });
    }

    void unmuteUser(){
        FirebaseDatabase.getInstance().getReference("muted_users").child(curr_uid).child(uid).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(ProfileActivity.this,getResources().getString(R.string.unmute)
                                + " " + getResources().getString(R.string.done),Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_options_menu, menu);
        return true;
    }

    void setEditBtnListener(){
        profileEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ProfileActivity.this, EditProfileActivity.class);
                i.putExtra("UID",curr_uid);
                i.putExtra("NAME",user.full_name);
                i.putExtra("JOB",user.job_title);
                i.putExtra("BIO",user.bio);
                i.putExtra("COVER_PATH",user.profile_cover);
                i.putExtra("PIC_PATH",user.profile_pic);
                startActivityForResult(i,PICTURES_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURES_REQUEST && resultCode == RESULT_OK){
            loadUserProfile();
        }
    }

    //loading JSON file of countries and states from assets folder
    public String loadCountryStateJSONFromAsset() {
        String json = null;
        try {
            InputStream inputStreanm = this.getAssets().open("countriesandstates.json");
            int size = inputStreanm.available();
            byte[] buffer = new byte[size];
            inputStreanm.read(buffer);
            inputStreanm.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public String loadCountryUsingItsISO(String countryCode){
        try {
            JSONObject obj = new JSONObject(loadCountryStateJSONFromAsset());
            JSONArray countries_arr = obj.getJSONArray("countries");

            Map<String,String> countries_code_name = new HashMap<>();

            for (int i = 0; i < countries_arr.length(); i++) {
                JSONObject jo_inside = countries_arr.getJSONObject(i);
                String iso2 = jo_inside.getString("iso2");
                String country_name = jo_inside.getString("name");

                countries_code_name.put(iso2,country_name);
            }
            return countries_code_name.get(countryCode);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
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
                i.putExtra("UID",uid);
                startActivity(i);
            }
        });
    }

    void setLoadingFollowingsListener(){
        profileFollowingsCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ProfileActivity.this,FollowingsActivity.class);
                i.putExtra("UID",uid);
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
                            profileFollowBtnTxt.setText(getResources().getString(R.string.following));
                            profileFollowBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //confirm unfollow
                                    new AlertDialog.Builder(ProfileActivity.this)
                                            .setTitle(getResources().getString(R.string.areYouSureYouWantToUnFollow))
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    FollowingHelper fh = new FollowingHelper(curr_uid,uid,ProfileActivity.this);
                                                    fh.unfollow();                                                }
                                            })
                                            .setNegativeButton(android.R.string.no, null).show();
                                }
                            });
                        } else {
                            //change style of follow btn
                            profileFollowBtnTxt.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                            profileFollowBtn.setCardBackgroundColor(getResources().getColor(R.color.colorWhite));
                            profileFollowBtnTxt.setText(getResources().getString(R.string.follow));

                            profileFollowBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    FollowingHelper fh = new FollowingHelper(curr_uid,uid,ProfileActivity.this);
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
                user = task.getResult().toObject(User.class);
                try {
                    loadPhotos(profilePic,uid, "profile_pic");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                loadPhotos(coverPhoto,uid, "cover");
                UserName = user.full_name;
                profileName.setText(UserName);
                jobTitle.setText(user.job_title);
                String location = loadCountryUsingItsISO(user.country)+((user.region.equals(""))?"":", "+user.region);
                country_region.setText(location);
                profileBio.setText(user.bio);
                DecimalFormat df = new DecimalFormat("##.##");
                profileRate.setText(df.format(user.rate));
                NumberFormat nf = NumberFormat.getInstance();
                nf.setGroupingUsed(true);
                ratings_count.setText( nf.format(user.ratings_count) + " " +getResources().getString(R.string.reviews));

                java.util.Date birthDate = user.birth_date.toDate();
                java.util.Date newDate = new java.util.Date();
                Calendar calBirth = Calendar.getInstance();
                Calendar calNew = Calendar.getInstance();
                calBirth.setTime(birthDate);
                calNew.setTime(newDate);

                //start konfetti animation if today is the user's birthday
                if(calBirth.get(Calendar.MONTH) == calNew.get(Calendar.MONTH)
                        && calBirth.get(Calendar.DAY_OF_MONTH) == calNew.get(Calendar.DAY_OF_MONTH)){

                    startKonfetti();
                }

                if(uid.equals(curr_uid))
                    setEditBtnListener();

            }
        });
        //get followings count
        FirebaseDatabase.getInstance().getReference("followings").child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String count = Long.toString(dataSnapshot.getChildrenCount());
                        count = "<b>"+count+"</b>"+ "  " + getResources().getString(R.string.followings);
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
                        count = "<b>"+count+"</b>"+ "  " + getResources().getString(R.string.followers);
                        profileFollowersCount.setText(Html.fromHtml(count));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
    }

    void loadPhotos(final ImageView imgV , String path, String type){
        FirebaseFirestore.getInstance().collection("users/").document(path)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    User user = task.getResult().toObject(User.class);
                    if (type.equals("cover")){
                        Picasso.with(ProfileActivity.this).load(Uri.parse(user.profile_cover)).into(imgV);
                    } else if (type.equals("profile_pic")){
                        Picasso.with(ProfileActivity.this).load(Uri.parse(user.profile_pic)).into(imgV);
                        profilePic.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final Intent i = new Intent(ProfileActivity.this, ImagePreview.class);
                                i.putExtra("IMAGE_PATH",user.profile_pic);
                                startActivity(i);
                            }
                        });
                    }
                }
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
            else {
                Fragment frg = new RatingFragment();
                Bundle b = new Bundle();
                b.putString("UID",uid);
                b.putString("FOR","PROFILE");
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
                return getResources().getString(R.string.posts);
            else
                return getResources().getString(R.string.reviews);
        }

    }

    public void startKonfetti(){
        konfettiView.build()
                .addColors(Color.parseColor("#ffeb3b"), Color.parseColor("#ffd54f"))
                .setDirection(0.0, 359.0)
                .setSpeed(1f, 5f)
                .setFadeOutEnabled(true)
                .setTimeToLive(3000L)
                .addSizes(new Size(12, 5))
                .setPosition(-50f, konfettiView.getWidth() + 50f, -50f, -50f)
                .streamFor(50, 5000L);
        konfettiView.postDelayed(new Runnable() {
            @Override
            public void run() {
                konfettiView.build()
                        .addColors(Color.parseColor("#ff5722"))
                        .setDirection(0.0, 359.0)
                        .setSpeed(3f, .2f)
                        .setFadeOutEnabled(true)
                        .setTimeToLive(1000L)
                        .addSizes(new Size(3, 4))
                        .setPosition(konfettiView.getX() + konfettiView.getWidth() / 3.0f, konfettiView.getY() + konfettiView.getHeight() / 3.0f)
                        .streamFor(3000, 1000L);
            }
        }, 2000);
        konfettiView.postDelayed(new Runnable() {
            @Override
            public void run() {
                konfettiView.build()
                        .addColors(Color.parseColor("#64b5f6"))
                        .setDirection(0.0, 359.0)
                        .setSpeed(3f, .2f)
                        .setFadeOutEnabled(true)
                        .setTimeToLive(1000L)
                        .addSizes(new Size(3, 4))
                        .setPosition(konfettiView.getX() + konfettiView.getWidth() - konfettiView.getWidth() / 4.0f, konfettiView.getY() + konfettiView.getHeight() - konfettiView.getHeight() / 4.0f)
                        .streamFor(3000, 1000L);
            }
        },3000);
        konfettiView.postDelayed(new Runnable() {
            @Override
            public void run() {
                konfettiView.build()
                        .addColors(Color.RED)
                        .setDirection(0.0, 359.0)
                        .setSpeed(3.3f, .4f)
                        .setFadeOutEnabled(true)
                        .setTimeToLive(1300L)
                        .addSizes(new Size(3, 4))
                        .setPosition(konfettiView.getX() + konfettiView.getWidth() / 2.0f, konfettiView.getY() + konfettiView.getHeight() / 3.0f)
                        .streamFor(5000, 1700L);
            }
        },4500);
    }


}
