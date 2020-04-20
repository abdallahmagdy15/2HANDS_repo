package com.example.a2hands.home;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
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
import com.google.firebase.storage.FirebaseStorage;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class HomeActivity extends AppCompatActivity {

    //drawer
    private DrawerLayout drawer;
    public NavigationView navigationView;

    //bottom navigation
    private BottomNavigationView nav;
    private int navItemId;

    private BadgeDrawable badge;
    private BadgeDrawable badgeForMessages;

    MaterialSpinner catsSpinner;
    String[] catsStrings;
    CircleImageView profile_image ;
    SearchView searchView;
    TextView notificationsTitle;

    //drawer header data
    View headerView;
    CircleImageView header_profilePic;
    TextView header_fAndLName;
    TextView header_email;

    private FirebaseFirestore db;
    String myUid;

    List<ChatList> myUsersList = new ArrayList<>();
    int[] messagesCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeLocale.loadLocale(getBaseContext());
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //drawer
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        nav = findViewById(R.id.bottom_navigation);
        badge = nav.getOrCreateBadge(nav.getMenu().getItem(3).getItemId());
        badge.setVisible(false);

        badgeForMessages = nav.getOrCreateBadge(nav.getMenu().getItem(4).getItemId());
        badgeForMessages.setVisible(false);

        searchView = findViewById(R.id.searchView);
        notificationsTitle = findViewById(R.id.notificationsTitle);

        //category spinner declaration
        //catsStrings = getEnglishStringArray(R.array.categories);
        catsSpinner = findViewById(R.id.catsSpinner);
        profile_image = findViewById(R.id.home_profile_image);
        catsSpinner.setItems(getResources().getStringArray(R.array.categories));


        //drawer header data
        headerView = navigationView.getHeaderView(0);
        header_profilePic = headerView.findViewById(R.id.drawer_profileImage);
        header_fAndLName = headerView.findViewById(R.id.drawer_fAndLName);
        header_email = headerView.findViewById(R.id.drawer_userEmail);


        header_profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                drawer.closeDrawer(GravityCompat.START);
            }
        });

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(GravityCompat.START);
            }
        });

        catsSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                loadPosts(position);
            }
        });

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(HomeActivity.this,  new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();
                updateToken(newToken);
            }
        });

        loadUserPicInTopMenu();
        loadNavigationDrawerData();
        setListenerForBottomNavigation();
        setListenerForNavigationView();
        getNewMessagesCountToBadge();
        checkForNotifications();
        navigateHome();

    }//////////////////////////////////end of onCreate method



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
                                badgeForMessages.setVisible(true);
                                badgeForMessages.setNumber(allMessageCount);
                            }else
                                badgeForMessages.setVisible(false);
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

    public void loadUserPicInTopMenu(){
        //load current user main pic in home top menu
        FirebaseFirestore.getInstance().collection("users/").document(myUid)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                User user = task.getResult().toObject(User.class);
                FirebaseStorage.getInstance().getReference().child("Profile_Pics/"+myUid+"/"+user.profile_pic).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
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
    }

    public void loadNavigationDrawerData(){
        FirebaseFirestore.getInstance().collection("users/").document(myUid)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                User user = task.getResult().toObject(User.class);
                FirebaseStorage.getInstance().getReference().child("Profile_Pics/"+myUid+"/"+user.profile_pic).getDownloadUrl()
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.get().load(uri.toString()).into(header_profilePic);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });

                header_fAndLName.setText(user.full_name);
                header_email.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
            }
        });
    }

    public void setListenerForBottomNavigation(){
        //bottom navigation
        nav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
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
                    case 1: navigateSearch();
                        break;
                    case 2: navigateCreatePost();
                        break;
                    case 3: navigateNotification();
                        break;
                    case 4: navigateChatList();
                        break;
                }
                return true;
            }
        });
    }

    public void setListenerForNavigationView(){
        //navigation drawer menu items
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_profile:
                        startActivity(new Intent(HomeActivity.this , ProfileActivity.class));
                        break;
                    case R.id.nav_saved:
                        startActivity(new Intent(HomeActivity.this , SavedPostsActivity.class));
                        break;
                    case R.id.nav_settings:
                        startActivity(new Intent(HomeActivity.this , SettingsActivity.class));
                        break;
                    case R.id.nav_signOut:
                        //stop notifications service
                        Intent intent = new Intent(HomeActivity.this, NotificationsService.class);
                        stopService(intent);

                        UserStatus.updateOnlineStatus(false, myUid);

                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(HomeActivity.this , LoginActivity.class));
                        break;
                    case R.id.nav_share:
                        Toast.makeText(HomeActivity.this, getResources().getString(R.string.share), Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_send:
                        Toast.makeText(HomeActivity.this, getResources().getString(R.string.send), Toast.LENGTH_SHORT).show();
                        break;
                }

                drawer.closeDrawer(GravityCompat.START);
                return true;
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
                            badge.setVisible(true);
                            badge.setNumber(count);
                        }
                        else {
                            badge.setVisible(false);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
    }

    public void updateToken(String token){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(myUid).setValue(mToken);

        SharedPreferences sp = getSharedPreferences("SP_USER",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("Current_USERID",myUid);
        editor.apply();
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
        if(UserStatus.isAppIsInBackground(getApplicationContext())){
            UserStatus.updateOnlineStatus(false, myUid);
        }
        super.onStop();
        if (mHandler != null) { mHandler.removeCallbacks(mRunnable); }
    }

    @Override
    public void onBackPressed() {
        Fragment f = this.getSupportFragmentManager().findFragmentById(R.id.homeFrag);

        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
            return;
        } else if(f instanceof ChatListFragment ||
                f instanceof SearchFragment ||
                f instanceof NotificationFragment) {

            nav.setSelectedItemId(nav.getMenu().getItem(0).getItemId());
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
    }



    void loadPosts(int pos){
        Fragment frg = new PostsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("CAT", String.valueOf(pos));
        bundle.putString("FOR","HOME");
        frg.setArguments(bundle);
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.homeFrag,frg).addToBackStack("");
        ft.commit();
    }
    public void navigateHome(){
        catsSpinner.setVisibility(View.VISIBLE);
        catsSpinner.setTextSize(15);
        searchView.setVisibility(View.INVISIBLE);
        notificationsTitle.setVisibility(View.INVISIBLE);
        loadPosts(0);
    }
    public void navigateSearch(){
        searchView.setVisibility(View.VISIBLE);
        catsSpinner.setVisibility(View.INVISIBLE);
        notificationsTitle.setVisibility(View.INVISIBLE);

        startFragmentSearch("");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startFragmentSearch(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }
    void startFragmentSearch(String query){
        Fragment frg = new SearchFragment();
        Bundle b= new Bundle();
        b.putString("search_query",query);
        frg.setArguments(b);
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.homeFrag,frg).addToBackStack(null);
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
        catsSpinner.setVisibility(View.INVISIBLE);
        searchView.setVisibility(View.INVISIBLE);

        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.homeFrag,new NotificationFragment()).addToBackStack(null);
        ft.commit();
        badge.clearNumber();
        badge.setVisible(false);
    }

    public void navigateChatList(){
        notificationsTitle.setVisibility(View.VISIBLE);
        notificationsTitle.setText(getResources().getString(R.string.inbox));
        catsSpinner.setVisibility(View.GONE);
        searchView.setVisibility(View.GONE);

        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.homeFrag,new com.example.a2hands.chat.chatlist.ChatListFragment()).addToBackStack(null);
        ft.commit();
    }

}