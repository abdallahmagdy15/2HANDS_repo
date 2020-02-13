package com.example.a2hands.homePackage;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.a2hands.Callback;
import com.example.a2hands.Post;
import com.example.a2hands.R;
import com.example.a2hands.User;
import com.google.android.gms.tasks.OnCompleteListener;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.List;


public class PostFragment extends Fragment {

    FirebaseAuth mAuth;
    String selectedCat;
    String uid;

    private OnListFragmentInteractionListener mListener;

    public PostFragment() {
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

        if (bundle.getString("for").equals("home") ) {
            selectedCat = bundle.getString("category", "General");
            getUser(new Callback() {
                @Override
                public void callbackUser(User user) {
                    List<String> location =new ArrayList<>();
                    location.add(user.country);
                    location.add(user.region);

                    getPostsForHome(location,selectedCat, view );
                }
            },uid);
        }
        else
        {
            String postuid = bundle.getString("uid");
            //get posts for profile from database
            getPostsForProfile(postuid ,view );
            //End get posts
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

        /*db.collection("/users").document(uid)
                .get() .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    User user = doc.toObject(User.class);
                    callback.callbackUser(user);
                }
                else {
                    Log.w("", "Error getting documents.", task.getException());
                }
            }
        });
    */

    public void getPostsForHome(final List<String> location,final String category,final View view ){
        final List<Post> posts = new ArrayList<>();
        /*Query q = db.child("posts").orderByChild("location").startAt(location.get(0)).endAt(location.get(1));
        if(!category.equals("General")){
            q = q.orderByChild("category").equalTo(category);
        }
        q.orderByChild("date").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    final Post p = postSnapshot.getValue(Post.class);
                    posts.add(p);
                }
                updateHomeWithPosts(posts,view);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/

        // Read from the database
        Query query = FirebaseFirestore.getInstance().collection("/posts")
                .whereIn("location", location);

        if(!category.equals("General")){
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
                            updateHomeWithPosts(posts, view);
                        } else {
                            Log.w("", "Error getting documents.", task.getException());

                        }
                    }
                });
    }
    public void getPostsForProfile(final String uid ,final View view ){
        final List<Post> posts = new ArrayList<>();

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
                            updateHomeWithPosts(posts, view);
                        } else {
                            Log.w("", "Error getting documents.", task2.getException());

                        }
                    }
                });
    }
    /*db.child("posts").orderByChild("user_id").equalTo(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                            final Post p = postSnapshot.getValue(Post.class);
                            posts.add(p);
                        }
                        updateHomeWithPosts(posts,view);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });*/

/*
    public void autoSigningin(final Callback callback){
        mAuth.signInWithEmailAndPassword("test@test.com", "123456789#A")
                .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        callback.callbackUserID(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    }
                });
    }
*/

    public void updateHomeWithPosts(List<Post> posts , View view ){
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;

            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new MyPostRecyclerViewAdapter(posts, mListener));
        }
    }

public interface OnListFragmentInteractionListener {
    // TODO: Update argument type and name
    void onListFragmentInteraction(Post item);
}
}
