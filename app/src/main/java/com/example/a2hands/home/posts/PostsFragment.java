package com.example.a2hands.home.posts;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a2hands.Callback;
import com.example.a2hands.R;
import com.example.a2hands.User;
import com.google.android.gms.tasks.OnCompleteListener;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class PostsFragment extends Fragment {

    FirebaseAuth mAuth;
    String selectedCat;
    String uid;
    String profile_user_id;
    View view;
    final List<String> hiddenPostsIds = new ArrayList<>();
    final List<String> mutedUsersId = new ArrayList<>();
    final List<String> blockedUsersId = new ArrayList<>();
    final List<Post> posts = new ArrayList<>();

    public PostsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_post_list, container, false);
        Bundle bundle = this.getArguments();
        mAuth = FirebaseAuth.getInstance();
        uid=FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.view = view;
        String activityName = bundle.getString("FOR");

        if (activityName.equals("HOME") ) {
            selectedCat = bundle.getString("CAT", String.valueOf(0));
            getUser(new Callback() {
                @Override
                public void callbackUser(User user) {
                    List<String> location =new ArrayList<>();
                    location.add(loadCountryUsingItsISO(user.country));
                    location.add(user.region);
                    getPostsForHome(location, selectedCat);
                }
            },uid);
        }
        else if (activityName.equals("PROFILE"))
        {
            profile_user_id = bundle.getString("UID");
            getBlockedUsersId();
        }
        else if (activityName.equals("SAVED_POSTS"))
        {
            getSavedPostsId();
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public static void getUser(final Callback callback,String uid) {
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


    //loading JSON file of countries and states from assets folder
    public String loadCountryStateJSONFromAsset() {
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

    private void getPostsForHome(final List<String> location,final String category ){
        // Read from the database
        Query query = FirebaseFirestore.getInstance().collection("/posts")
                .whereIn("location", location);
        if(!category.equals(String.valueOf(0))){
            query = query.whereEqualTo("category",category);
        }
        query.orderBy("date", Query.Direction.DESCENDING).limitToLast(30)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                final Post p = doc.toObject(Post.class);
                                posts.add(p);
                            }
                            getHiddenPostsId();
                        } else {
                            Log.w("", "Error getting documents.", task.getException());

                        }
                    }
                });
    }

    private void getPostsForProfile(final String uid ){
        FirebaseFirestore.getInstance().collection("/posts")
                .whereEqualTo("visibility",true)
                .whereEqualTo("user_id",uid)
                .orderBy("date", Query.Direction.DESCENDING).limitToLast(30)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task2) {
                        List<Post> posts = new ArrayList<>();
                        if (task2.isSuccessful()) {
                            for (QueryDocumentSnapshot doc2 : task2.getResult()) {
                                final Post p = doc2.toObject(Post.class);
                                posts.add(p);
                            }
                            updateUiWithPosts(posts);
                        } else {
                            Log.w("", "Error getting documents.", task2.getException());

                        }
                    }
                });
    }

    private void filterHomePosts(final List<Post> posts){
        Iterator<Post> i = posts.iterator();
        while (i.hasNext()){
            Post p = i.next();
            if(hiddenPostsIds.contains(p.post_id) || mutedUsersId.contains(p.user_id) || blockedUsersId.contains(p.user_id))
                i.remove();
        }
        updateUiWithPosts(posts);
    }

    private void getHiddenPostsId(){
        FirebaseDatabase.getInstance().getReference("hidden_posts").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() != 0){
                    for (DataSnapshot ds : dataSnapshot.getChildren()){
                        hiddenPostsIds.add(ds.getKey());
                    }
                }
                getMutedUsersId();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void getSavedPostsId(){
        FirebaseDatabase.getInstance().getReference("saved_posts").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<String> savedPostsId = new ArrayList<>();
                        if(dataSnapshot.getChildrenCount() != 0 ){
                            for(DataSnapshot ds : dataSnapshot.getChildren()){
                                savedPostsId.add(ds.getKey());
                            }
                            getSavedPosts(savedPostsId);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
    }

    private void getSavedPosts(List<String> savedPostsId){
        FirebaseFirestore.getInstance().collection("posts").whereIn("post_id",savedPostsId)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(!task.getResult().isEmpty()){
                    for( DocumentSnapshot ds : task.getResult()){
                        posts.add(ds.toObject(Post.class));
                    }
                }
                updateUiWithPosts(posts);
            }
        });
    }

    private void getMutedUsersId(){
        FirebaseDatabase.getInstance().getReference("muted_users").child(uid).addListenerForSingleValueEvent(
                new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() != 0){
                    for (DataSnapshot ds : dataSnapshot.getChildren()){
                        mutedUsersId.add(ds.getKey());
                    }
                }
                getBlockedUsersId();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void getBlockedUsersId(){
        FirebaseDatabase.getInstance().getReference("blocked_users").child(uid).addListenerForSingleValueEvent(
                new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() != 0){
                    for (DataSnapshot ds : dataSnapshot.getChildren()){
                        blockedUsersId.add(ds.getKey());
                    }
                }
                //check if loading the profile posts
                if(profile_user_id != null){
                    if(!blockedUsersId.contains(profile_user_id)){
                        getPostsForProfile(profile_user_id);
                    }
                }else {
                    filterHomePosts(posts);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void updateUiWithPosts(List<Post> posts){
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(new MyPostRecyclerViewAdapter(posts));

        }
    }


    ///////////////////////////////////////////////////////////
    // changing the language only to get english strings for //
    // categories to be able to load posts correctly ....... //
    ///////////////////////////////////////////////////////////
    @NonNull
    private String getEnglishString(int word) {
        Configuration configuration = getEnglishConfiguration();

        return getContext().createConfigurationContext(configuration).getResources().getString(word);
    }

    @NonNull
    private Configuration getEnglishConfiguration() {
        Configuration configuration = new Configuration(getContext().getResources().getConfiguration());
        configuration.setLocale(new Locale("en"));
        return configuration;
    }////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////


}