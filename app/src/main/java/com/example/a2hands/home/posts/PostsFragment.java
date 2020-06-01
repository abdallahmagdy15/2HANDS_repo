package com.example.a2hands.home.posts;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a2hands.Callback;
import com.example.a2hands.R;
import com.example.a2hands.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.todkars.shimmer.ShimmerRecyclerView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.lang.Double.MAX_VALUE;


public class PostsFragment extends Fragment {

    private FirebaseAuth mAuth;
    private String selectedCat;
    private List<String> location = new ArrayList<>();
    private String uid;
    private String profile_user_id;
    private View view;
    private final List<String> hiddenPostsIds = new ArrayList<>();
    private final List<String> mutedUsersId = new ArrayList<>();
    private final List<String> blockedUsersId = new ArrayList<>();
    private final List<Post> posts = new ArrayList<>();
    private MyPostRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    private int lastPostsCount = 0;
    private boolean loading = false;
    private boolean firstTimeLoadingPosts = true;
    private Date lastPostDate;
    private double lastPostPriority = MAX_VALUE;
    private String postsType;

    private ShimmerRecyclerView mShimmerRecyclerView;

    public PostsFragment() {
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
        final View view = inflater.inflate(R.layout.fragment_post_list, container, false);

        mShimmerRecyclerView = view.findViewById(R.id.postsRecyclerView_shimmer);
        recyclerView = view.findViewById(R.id.postsRecyclerView);


        Bundle bundle = this.getArguments();
        mAuth = FirebaseAuth.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.view = view;
        postsType = bundle.getString("FOR");

        // Set the adapter
        Context context = view.getContext();
        //using custom LinearLayout to catch (RecyclerView and java.lang.IndexOutOfBoundsException)
        recyclerView.setLayoutManager(new CustomLinearLayoutManager(context));
        recyclerView.setHasFixedSize(true);
        adapter = new MyPostRecyclerViewAdapter(posts);
        recyclerView.setAdapter(adapter);

        lastPostDate = new Date();
        if (postsType.equals("HOME_DATE")) {
            selectedCat = bundle.getString("CAT", String.valueOf(0));
            getUser(new Callback() {
                @Override
                public void callbackUser(User user) {
                    location.add(loadCountryUsingItsISO(user.country));
                    location.add(user.region);
                    getPostsForHomeByDate(location, selectedCat);
                }
            }, uid);
        }
        else if (postsType.equals("HOME_PRIORITY")) {
            selectedCat = bundle.getString("CAT", String.valueOf(0));
            getUser(new Callback() {
                @Override
                public void callbackUser(User user) {
                    location.add(loadCountryUsingItsISO(user.country));
                    location.add(user.region);
                    lastPostDate = new Date();
                    getPostsForHomeByPriority(location, selectedCat);
                }
            }, uid);
        }
        else if (postsType.equals("HOME_FOLLOWINGS_POSTS")) {
            selectedCat = bundle.getString("CAT", String.valueOf(0));
            getUser(new Callback() {
                @Override
                public void callbackUser(User user) {
                    location.add(loadCountryUsingItsISO(user.country));
                    location.add(user.region);
                    lastPostDate = new Date();
                    getFollowingsIds();
                }
            }, uid);
        }

        else if (postsType.equals("PROFILE")) {
            profile_user_id = bundle.getString("UID");
            if(profile_user_id.equals(uid))
                getPostsForProfile(profile_user_id);
            else
                getBlockedUsersId();
        } else if (postsType.equals("SAVED_POSTS")) {
            getSavedPostsId();
        }
        return view;
    }

    private void getFollowingsIds(){
        FirebaseDatabase.getInstance().getReference("followings").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<String> followingsIds = new ArrayList<>();
                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                            followingsIds.add(ds.getKey());
                        }
                        if(followingsIds.size()>0)
                            getPostsForHomeByFollowings(followingsIds, selectedCat);
                        else {
                            Toast.makeText(getContext(),getResources().getString(R.string.youHaveNoFollowings),Toast.LENGTH_LONG).show();
                            mShimmerRecyclerView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public static void getUser(final Callback callback, String uid) {
        FirebaseFirestore.getInstance().collection("/users")
                .document(uid).get()
                .addOnCompleteListener(
                        new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                User user = task.getResult().toObject(User.class);
                                callback.callbackUser(user);
                            }
                        }
                );
    }

    private void getPostsForHomeByPriority(final List<String> location, final String category) {
        // order by location , category , state , priority , date
        Query query = FirebaseFirestore.getInstance().collection("posts")
                .orderBy("priority", Query.Direction.DESCENDING);
        if(!firstTimeLoadingPosts)
            query = query.startAt(lastPostPriority);
        query = query.whereIn("location", location);
        if (!category.equals(String.valueOf(0))) {
            query = query.whereEqualTo("category", category);
        }
        query = query.whereEqualTo("state", true);
        query.limit(20)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Post> ps = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                final Post p = doc.toObject(Post.class);
                                ps.add(p);
                            }
                            if(ps.size() !=0){
                                posts.addAll(ps);
                                lastPostsCount=ps.size();
                            }
                            else{
                                lastPostsCount=0;
                                recyclerView.clearOnScrollListeners();
                            }
                            getHiddenPostsId();
                        } else {
                            Log.w("", "Error getting documents.", task.getException());

                        }
                    }
                });
    }

    private void getPostsForHomeByFollowings(final List<String> followingsIds, final String category) {
        // order by location , category , state , priority , date
        Query query = FirebaseFirestore.getInstance().collection("posts")
                .orderBy("date", Query.Direction.DESCENDING)
                .startAt(lastPostDate)
                .whereIn("user_id", followingsIds);
        if (!category.equals(String.valueOf(0))) {
            query = query.whereEqualTo("category", category);
        }
        query.whereEqualTo("state", true)
                .limit(20)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Post> ps = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                final Post p = doc.toObject(Post.class);
                                ps.add(p);
                            }
                            if(ps.size() !=0){
                                posts.addAll(ps);
                                lastPostsCount=ps.size();
                            }
                            else{
                                lastPostsCount=0;
                                recyclerView.clearOnScrollListeners();
                            }
                            getHiddenPostsId();
                        } else {
                            Log.w("", "Error getting documents.", task.getException());

                        }
                    }
                });
    }

    private void getPostsForHomeByDate(final List<String> location, final String category) {
        // order by location , category , state , priority , date
        Query query = FirebaseFirestore.getInstance().collection("posts")
                .orderBy("date", Query.Direction.DESCENDING)
                .startAt(lastPostDate)
                .whereIn("location", location);
        if (!category.equals(String.valueOf(0))) {
            query = query.whereEqualTo("category", category);
        }
        query.whereEqualTo("state", true)
                .limit(10)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Post> ps = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                final Post p = doc.toObject(Post.class);
                                ps.add(p);
                            }

                            if(ps.size() !=0){
                                posts.addAll(ps);
                                lastPostsCount=ps.size();
                            }
                            else{
                                lastPostsCount=0;
                                recyclerView.clearOnScrollListeners();
                            }
                            getHiddenPostsId();
                        } else {
                            Log.w("", "Error getting documents.", task.getException());

                        }
                    }
                });
    }

    //loading JSON file of countries and states from assets folder
    private String loadCountryStateJSONFromAsset() {
        String json = null;
        try {
            InputStream inputStreanm = getActivity().getAssets().open("countriesandstates.json");
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

    private String loadCountryUsingItsISO(String countryCode) {
        try {
            JSONObject obj = new JSONObject(loadCountryStateJSONFromAsset());
            JSONArray countries_arr = obj.getJSONArray("countries");

            Map<String, String> countries_code_name = new HashMap<>();

            for (int i = 0; i < countries_arr.length(); i++) {
                JSONObject jo_inside = countries_arr.getJSONObject(i);
                String iso2 = jo_inside.getString("iso2");
                String country_name = jo_inside.getString("name");

                countries_code_name.put(iso2, country_name);
            }
            return countries_code_name.get(countryCode);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void getPostsForProfile(final String uid) {
        Query query = FirebaseFirestore.getInstance().collection("/posts")
                .orderBy("date", Query.Direction.DESCENDING)
                .startAt(lastPostDate)
                .whereEqualTo("user_id", uid);
        if (!profile_user_id.equals(uid))
            query = query.whereEqualTo("visibility", true);
        query.limit(20)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task2) {
                        if (task2.isSuccessful()) {
                            List<Post> ps = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : task2.getResult()) {
                                final Post p = doc.toObject(Post.class);
                                ps.add(p);
                            }
                            if(ps.size() !=0){
                                posts.addAll(ps);
                                lastPostsCount=ps.size();
                            }
                            else{
                                lastPostsCount=0;
                                recyclerView.clearOnScrollListeners();
                            }
                            mShimmerRecyclerView.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            updateUiWithPosts();
                        } else {
                            Log.w("", "Error getting documents.", task2.getException());

                        }
                    }
                });
    }

    private void filterHomePosts(final List<Post> posts) {
        Iterator<Post> i = posts.iterator();
        while (i.hasNext()) {
            Post p = i.next();
            if (hiddenPostsIds.contains(p.post_id) || mutedUsersId.contains(p.user_id) || blockedUsersId.contains(p.user_id)) {
                i.remove();
                lastPostsCount--;
            }
        }
        mShimmerRecyclerView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        updateUiWithPosts();
    }

    private void getHiddenPostsId() {
        FirebaseDatabase.getInstance().getReference("hidden_posts").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() != 0) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        hiddenPostsIds.add(ds.getKey());
                    }
                }
                getMutedUsersId();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void getSavedPostsId() {
        FirebaseDatabase.getInstance().getReference("saved_posts").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        List<String> savedPostsId = new ArrayList<>();
                        if (dataSnapshot.getChildrenCount() != 0) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                savedPostsId.add(ds.getKey());
                            }
                            getSavedPosts(savedPostsId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    private void getSavedPosts(List<String> savedPostsId) {
        FirebaseFirestore.getInstance().collection("posts").whereIn("post_id", savedPostsId)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot ds : task.getResult()) {
                        posts.add(ds.toObject(Post.class));
                    }
                }
                mShimmerRecyclerView.setVisibility(View.GONE);
                updateUiWithPosts();
            }
        });
    }

    private void getMutedUsersId() {
        FirebaseDatabase.getInstance().getReference("muted_users").child(uid).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() != 0) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                mutedUsersId.add(ds.getKey());
                            }
                        }
                        getBlockedUsersId();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    private void getBlockedUsersId() {
        FirebaseDatabase.getInstance().getReference("blocked_users").child(uid).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() != 0) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                blockedUsersId.add(ds.getKey());
                            }
                        }
                        //check if loading the profile posts
                        if (profile_user_id != null) {
                            if (!blockedUsersId.contains(profile_user_id)) {
                                getPostsForProfile(profile_user_id);
                            }
                        } else {
                            filterHomePosts(posts);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    private void updateUiWithPosts() {
        //set next posts date or priority
        if(lastPostsCount != 0){
            lastPostPriority = posts.get(posts.size() - 1).priority - 0.1;
            lastPostDate = posts.get(posts.size() - 1).date;
            Calendar cal = Calendar.getInstance();
            cal.setTime(lastPostDate);
            cal.add(Calendar.MILLISECOND,-1);
            lastPostDate = cal.getTime();
        }
        //adapter.insertExtraPosts(posts.subList(posts.size()-lastPostsCount,posts.size()));
        if (firstTimeLoadingPosts) {
            adapter.notifyDataSetChanged();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NotNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (!recyclerView.canScrollVertically(1) && !loading  /*check if still loading*/) {
                        loading = true;
                        Toast.makeText(getActivity().getBaseContext(), "loading...", Toast.LENGTH_SHORT).show();
                        if (postsType.equals("HOME_DATE"))
                            getPostsForHomeByDate(location, selectedCat);
                        else if (postsType.equals("HOME_PRIORITY"))
                            getPostsForHomeByPriority(location,selectedCat);
                        else if (postsType.equals("HOME_FOLLOWINGS_POSTS"))
                            getPostsForHomeByFollowings(location,selectedCat);
                        else if (postsType.equals("PROFILE"))
                            getPostsForProfile(profile_user_id);

                    }
                }
            });
            firstTimeLoadingPosts = false;
        } else {
            if(lastPostsCount !=0) {
                adapter.notifyItemRangeInserted(0, lastPostsCount);
                loading = false;
            }
        }


    }

    //using custom LinearLayout to catch (RecyclerView and java.lang.IndexOutOfBoundsException)
    public static class CustomLinearLayoutManager extends LinearLayoutManager {
        public CustomLinearLayoutManager(Context context) {
            super(context);
        }

        //Generate constructors
        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
                Log.e("outOfBoundPosition", "Inconsistency detected");
            }
        }
    }///////////////////////////////////////////////////////////////////////////////////////////

}