package com.example.a2hands.home;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a2hands.ChangeLocale;
import com.example.a2hands.CreatePostActivity;
import com.example.a2hands.LoginActivity;
import com.example.a2hands.UserStatus;
import com.example.a2hands.chat.Chat;
import com.example.a2hands.chat.chat_notifications.Token;
import com.example.a2hands.chat.chatlist.ChatList;
import com.example.a2hands.chat.chatlist.ChatListFragment;
import com.example.a2hands.notifications.Notification;
import com.example.a2hands.notifications.NotificationFragment;
import com.example.a2hands.notifications.NotificationsService;
import com.example.a2hands.profile.ProfileActivity;
import com.example.a2hands.R;
import com.example.a2hands.SavedPostsActivity;
import com.example.a2hands.search.SearchFragment;
import com.example.a2hands.User;
import com.example.a2hands.home.posts.PostsFragment;
import com.example.a2hands.settings.SettingsActivity;
import com.gauravk.bubblenavigation.BubbleNavigationConstraintView;
import com.gauravk.bubblenavigation.listener.BubbleNavigationChangeListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class HomeActivity extends AppCompatActivity {

    //navigation drawer
    AccountHeader headerResult;
    Drawer result;

    String userFullname;
    String userUsername;

    private FloatingActionButton addPost;

    FrameLayout postsFrag;
    FrameLayout searchFrag;
    FrameLayout notifiFrag;
    FrameLayout messagingFrag;

    //bottom navigation
    private BubbleNavigationConstraintView nav;

    MaterialSpinner catsSpinner;
    CircleImageView profile_image ;
    SearchView searchView;
    TextView notificationsTitle;

    String selectedPostsType="HOME_DATE";

    private FirebaseFirestore db;
    String myUid;
    Uri profileUri;

    List<ChatList> myUsersList = new ArrayList<>();
    int[] messagesCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeLocale.loadLocale(getBaseContext());
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        nav = findViewById(R.id.bottom_navigation);

        searchView = findViewById(R.id.searchView);
        notificationsTitle = findViewById(R.id.notificationsTitle);

        postsFrag=findViewById(R.id.home_postsFrag);
        searchFrag=findViewById(R.id.home_searchFrag);
        notifiFrag=findViewById(R.id.home_notifiFrag);
        messagingFrag=findViewById(R.id.home_messagingFrag);

        //category spinner declaration
        catsSpinner = findViewById(R.id.catsSpinner);
        profile_image = findViewById(R.id.home_profile_image);
        catsSpinner.setItems(getResources().getStringArray(R.array.categories));

        addPost = findViewById(R.id.home_addPost);

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                result.openDrawer();
            }
        });

        catsSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                loadPostsFragment(position,selectedPostsType);
            }
        });

        addPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateCreatePost();
            }
        });


        updateToken();
        loadUserPics();
        loadNavigationDrawerData();
        setListenerForBottomNavigation();
        getNewMessagesCountToBadge();
        checkForNotifications();
        navigateHome();

    }//////////////////////////////////end of onCreate method

    private void initDrawer() {
        PrimaryDrawerItem myProfileItem = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.myProfile).withIcon(R.drawable.ic_profile);
        PrimaryDrawerItem homeItem = new PrimaryDrawerItem().withIdentifier(2).withName(R.string.home).withIcon(R.drawable.ic_home_drawer);
        PrimaryDrawerItem trendingItem = new PrimaryDrawerItem().withIdentifier(3).withName(R.string.trending).withIcon(R.drawable.ic_trending);
        PrimaryDrawerItem followingsItem = new PrimaryDrawerItem().withIdentifier(4).withName(R.string.followings).withIcon(R.drawable.ic_followings);
        PrimaryDrawerItem savedItem = new PrimaryDrawerItem().withIdentifier(5).withName(R.string.saved).withIcon(R.drawable.ic_saved);
        PrimaryDrawerItem settingsItem = new PrimaryDrawerItem().withIdentifier(6).withName(R.string.settings).withIcon(R.drawable.ic_settings);
        PrimaryDrawerItem signOutItem = new PrimaryDrawerItem().withIdentifier(7).withName(R.string.signOut).withIcon(R.drawable.ic_sign_out);

        //initialize and create the image loader logic
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Picasso.with(HomeActivity.this).load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Picasso.with(HomeActivity.this).cancelRequest(imageView);
            }
        });

        // Create the AccountHeader
        headerResult = new AccountHeaderBuilder()
                .withActivity(HomeActivity.this)
                .withOnAccountHeaderProfileImageListener(new AccountHeader.OnAccountHeaderProfileImageListener() {
                    @Override
                    public boolean onProfileImageClick(View view, IProfile profile, boolean current) {
                        startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                        return false;
                    }

                    @Override
                    public boolean onProfileImageLongClick(View view, IProfile profile, boolean current) {
                        return false;
                    }
                })
                .withHeaderBackground(R.drawable.ic_large_triangles)
                .addProfiles(
                        new ProfileDrawerItem().withName(userFullname)
                                .withEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                                .withIcon(profileUri)
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();

        //create drawer menu
        result = new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(headerResult, true)
                .addDrawerItems(
                        myProfileItem,
                        new SectionDrawerItem().withName(R.string.posts),
                        homeItem,
                        trendingItem,
                        followingsItem,
                        savedItem,
                        new DividerDrawerItem(),
                        settingsItem,
                        signOutItem
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        getDrawerItemSelectedEvent(drawerItem);
                        result.setSelection(-1);
                        return false;
                    }
                })
                .withSelectedItem(-1)
                .build();
    }

    public void getDrawerItemSelectedEvent(IDrawerItem drawerItem){
        switch ((int) drawerItem.getIdentifier()) {
            case 1:
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                break;
            case 2:
                selectedPostsType = "HOME_DATE";
                loadPostsFragment(0,"HOME_DATE");
                break;
            case 3:
                selectedPostsType = "HOME_PRIORITY";
                loadPostsFragment(0, "HOME_PRIORITY");
                break;
            case 4:
                selectedPostsType = "HOME_FOLLOWINGS_POSTS";
                loadPostsFragment(0, "HOME_FOLLOWINGS_POSTS");
                break;
            case 5:
                startActivity(new Intent(HomeActivity.this, SavedPostsActivity.class));
                break;
            case 6:
                startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
                break;
            case 7:
                //stop notifications service
                Intent intent = new Intent(HomeActivity.this, NotificationsService.class);
                stopService(intent);

                UserStatus.updateOnlineStatus(false, myUid);

                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                finish();
                break;
        }
    }

    public void loadUserPics(){
        FirebaseFirestore.getInstance().collection("users/").document(myUid)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                User user = task.getResult().toObject(User.class);
                if(task.isSuccessful() && !user.profile_pic.equals("")){
                    profileUri = Uri.parse(user.profile_pic);
                    Picasso.with(HomeActivity.this).load(profileUri).into(profile_image);

                } else if (user.profile_pic.equals("")){
                    if (!user.gender) {
                        profileUri = getUriToDrawable(R.drawable.female_1);
                        profile_image.setImageResource(R.drawable.female_1);
                    } else {
                        profileUri = getUriToDrawable(R.drawable.male_1);
                        profile_image.setImageResource(R.drawable.male_1);
                    }
                }
            }
        });
    }

    public Uri getUriToDrawable(int drawableId) {
        Uri imageUri = (new Uri.Builder())
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(HomeActivity.this.getResources().getResourcePackageName(drawableId))
                .appendPath(HomeActivity.this.getResources().getResourceTypeName(drawableId))
                .appendPath(HomeActivity.this.getResources().getResourceEntryName(drawableId))
                .build();
        return imageUri;
    }

    public void loadNavigationDrawerData(){
        FirebaseFirestore.getInstance().collection("users/").document(myUid)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    User user = task.getResult().toObject(User.class);

                    userFullname = user.full_name;
                    userUsername = "@"+user.user_name;
                    initDrawer();
                }
            }
        });
    }


    public void getNewMessagesCountToBadge(){
        FirebaseDatabase.getInstance().getReference("chatList").child(myUid).child("myUsersList").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myUsersList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    ChatList chatList=ds.getValue(ChatList.class);
                    myUsersList.add(chatList);
                }
                messagesCount = new int[myUsersList.size()];
                for (int i=0; i<myUsersList.size();i++){
                    int finalI = i;
                    FirebaseDatabase.getInstance().getReference("chatList").child(myUsersList.get(i).getId()).child(myUid).child("messages").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            messagesCount[finalI] = 0;
                            for (DataSnapshot ds : dataSnapshot.getChildren()){
                                Chat chat = ds.getValue(Chat.class);
                                if (chat.getReceiver().equals(myUid) && chat.getSender().equals(myUsersList.get(finalI).getId()) && !chat.getIsSeen()){
                                    messagesCount[finalI]++;
                                }
                            }
                            int allMessageCount = 0;
                            for (int value : messagesCount) {
                                allMessageCount += value;
                            }
                            if (allMessageCount > 0){
                                nav.setBadgeValue(3,allMessageCount+"");
                            }
                            else
                                nav.setBadgeValue(3,null);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }


    public void setListenerForBottomNavigation() {
        //bottom navigation
        nav.setNavigationChangeListener(new BubbleNavigationChangeListener() {
            @Override
            public void onNavigationChanged(View view, int pos) {
                switch (pos) {
                    case 0: navigateHome();
                        break;
                    case 1: navigateSearch();
                        break;
                    case 2: navigateNotification();
                        break;
                    case 3: navigateChatList();
                        break;
                }
            }
        });
    }


    public void checkForNotifications(){
        //check notifications
        FirebaseDatabase.getInstance().getReference("notifications")
                .orderByChild("subscriber_id")
                .equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int count=0;
                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                            if(!ds.getValue(Notification.class).is_seen) count++;
                        }
                        if(count > 0){
                            nav.setBadgeValue(2,count+"");
                        }
                        else
                            nav.setBadgeValue(2,null);

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
    }

    public void updateToken(){
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(HomeActivity.this,  new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
                Token mToken = new Token(newToken);
                ref.child(myUid).setValue(mToken);

                SharedPreferences sp = getSharedPreferences("SP_USER",MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("Current_USERID",myUid);
                editor.apply();
            }
        });
    }


    @Override
    protected void onStart() {
        //start notifications service
        Intent intent = new Intent(this, NotificationsService.class);
        startService(intent);

        super.onStart();
    }

    @Override
    protected void onResume() {
        UserStatus.updateOnlineStatus(true, myUid);
        super.onResume();
    }


    ///////////////////////////////////////////////////////////////
    ///////////////////press back again to exit////////////////////
    ///////////////////////////////////////////////////////////////
    private boolean doubleBackToExitPressedOnce = false;
    private Handler mHandler = new Handler();

    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            doubleBackToExitPressedOnce = false;
        }
    };

    @Override
    protected void onStop()
    {
        if(UserStatus.isAppIsInBackground(getBaseContext())){
            UserStatus.updateOnlineStatus(false, myUid);
        }
        super.onStop();
        if (mHandler != null) { mHandler.removeCallbacks(mRunnable); }
    }

    /*@Override
    public void onBackPressed() {
        Fragment f = this.getSupportFragmentManager().findFragmentById(R.id.home_postsFrag);

        if(result.isDrawerOpen()){
            result.closeDrawer();
            return;
        } else if(f instanceof ChatListFragment ||
                f instanceof SearchFragment ||
                f instanceof NotificationFragment) {

            navigateHome();
            return;
        } else if (doubleBackToExitPressedOnce) {

            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getResources().getString(R.string.pressBackAgainToExit), Toast.LENGTH_SHORT).show();
        mHandler.postDelayed(mRunnable, 2000);
    }*/



    void loadPostsFragment(int pos,String type){
        Fragment frg = new PostsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("CAT", String.valueOf(pos));
        bundle.putString("FOR",type);
        frg.setArguments(bundle);
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.home_postsFrag,frg).addToBackStack("");
        ft.commit();
    }

    public void navigateHome(){
        catsSpinner.setVisibility(View.VISIBLE);
        catsSpinner.setTextSize(16);
        searchView.setVisibility(View.GONE);
        notificationsTitle.setVisibility(View.GONE);

        postsFrag.setVisibility(View.VISIBLE);
        searchFrag.setVisibility(View.GONE);
        notifiFrag.setVisibility(View.GONE);
        messagingFrag.setVisibility(View.GONE);

        if( getSupportFragmentManager().findFragmentById(R.id.home_postsFrag) == null )
            loadPostsFragment(0,"HOME_DATE");
    }

    public void navigateSearch(){
        searchView.setVisibility(View.VISIBLE);
        catsSpinner.setVisibility(View.GONE);
        notificationsTitle.setVisibility(View.GONE);

        postsFrag.setVisibility(View.GONE);
        searchFrag.setVisibility(View.VISIBLE);
        notifiFrag.setVisibility(View.GONE);
        messagingFrag.setVisibility(View.GONE);
        if( getSupportFragmentManager().findFragmentById(R.id.home_searchFrag) == null ){
            loadSearchFragment("");
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    loadSearchFragment(query);
                    return false;
                }
                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
        }
    }

    void loadSearchFragment(String query){
        Fragment frg = new SearchFragment();
        Bundle b= new Bundle();
        b.putString("search_query",query);
        frg.setArguments(b);
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.home_searchFrag,frg).addToBackStack(null);
        ft.commit();
    }

    public void navigateCreatePost(){
        Intent intent = new Intent(this, CreatePostActivity.class);
        intent.putExtra("uid", myUid);
        startActivity(intent);
    }

    void navigateNotification(){
        notificationsTitle.setVisibility(View.VISIBLE);
        notificationsTitle.setText(getResources().getString(R.string.notifications));
        catsSpinner.setVisibility(View.GONE);
        searchView.setVisibility(View.GONE);

        postsFrag.setVisibility(View.GONE);
        searchFrag.setVisibility(View.GONE);
        notifiFrag.setVisibility(View.VISIBLE);
        messagingFrag.setVisibility(View.GONE);

        if( getSupportFragmentManager().findFragmentById(R.id.home_notifiFrag) == null ) {

            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.home_notifiFrag, new NotificationFragment()).addToBackStack(null);
            ft.commit();
            nav.setBadgeValue(2, null);
        }
    }

    public void navigateChatList(){
        notificationsTitle.setVisibility(View.VISIBLE);
        notificationsTitle.setText(getResources().getString(R.string.inbox));
        catsSpinner.setVisibility(View.GONE);
        searchView.setVisibility(View.GONE);

        postsFrag.setVisibility(View.GONE);
        searchFrag.setVisibility(View.GONE);
        notifiFrag.setVisibility(View.GONE);
        messagingFrag.setVisibility(View.VISIBLE);

        if( getSupportFragmentManager().findFragmentById(R.id.home_messagingFrag) == null ){
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.home_messagingFrag,new ChatListFragment()).addToBackStack(null);
            ft.commit();
        }
    }

}