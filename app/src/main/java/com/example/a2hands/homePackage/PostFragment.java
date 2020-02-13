package com.example.a2hands.homePackage;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.List;

interface Callback{
    void callbackUser(User user);
    void callbackUserID(String uid);
}
public class PostFragment extends Fragment {

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String selectedCat;

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
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();


        if (bundle.getString("for").equals("home") ) {
            selectedCat = bundle.getString("category", "General");
            //get posts from database
            autoSigningin(new Callback() {
                @Override
                public void callbackUser(User user) { }
                @Override
                public void callbackUserID(final String uid) {
                    getUser(new Callback() {
                        @Override
                        public void callbackUser(User user) {
                            List<String> location =new ArrayList<>();
                            location.add(user.country);
                            location.add(user.region);

                            getPostsForHome(location,selectedCat, view );
                        }

                        @Override
                        public void callbackUserID(String uid){ }
                    },uid);

                }
            });
            //End get posts
        }
        else
        {
            String uid = bundle.getString("uid");
            //get posts for profile from database
                getPostsForProfile(uid ,view );
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

    public  void getUser(final Callback callback,String uid){
        db.collection("/users").document(uid)
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
    }

    public void getPostsForHome(final List<String> location,final String category,final View view ){
        final List<Post> posts = new ArrayList<>();

        // Read from the database
        db.collection("/users").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                final String userid = doc.getId();
                                final User user = doc.toObject(User.class);
                                Query query = db.collection("/users/"+userid+"/posts")
                                        .whereIn("location", location);
                                if(!category.equals("General")){
                                    query = query.whereEqualTo("category",category);
                                }
                                query.orderBy("date", Query.Direction.DESCENDING).limitToLast(30)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task2) {
                                                if (task2.isSuccessful()) {
                                                    for (QueryDocumentSnapshot doc2 : task2.getResult()) {
                                                        final Post p = doc2.toObject(Post.class);
                                                        p.postOwner = user.first_name + " " + user.last_name;
                                                        p.user_id= userid;
                                                        posts.add(p);
                                                    }
                                                    updateHomeWithPosts(posts,view);
                                                }
                                                else {
                                                    Log.w("", "Error getting documents.", task2.getException());

                                                }
                                            }
                                        });
                            }

                        } else {
                            Log.w("", "Error getting documents.", task.getException());
                        }
                    }
                });
    }
    public void getPostsForProfile(final String uid ,final View view ){
        // Read from the database
        db.collection("/users/").document(uid).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            if (doc.exists()) {
                                final User user = doc.toObject(User.class);
                                db.collection("/users/" + uid + "/posts")
                                        .whereEqualTo("visibility",true)
                                        .orderBy("date", Query.Direction.DESCENDING).limitToLast(30)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task2) {
                                                List<Post> posts = new ArrayList<>();
                                                if (task2.isSuccessful()) {
                                                    for (QueryDocumentSnapshot doc2 : task2.getResult()) {
                                                        final Post p = doc2.toObject(Post.class);
                                                        p.postOwner = user.first_name + " " + user.last_name;
                                                        p.user_id = uid;
                                                        posts.add(p);
                                                    }
                                                    updateHomeWithPosts(posts, view);
                                                } else {
                                                    Log.w("", "Error getting documents.", task2.getException());

                                                }
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    public void autoSigningin(final Callback callback){
        mAuth.signInWithEmailAndPassword("test@test.com", "123456789#A")
                .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        callback.callbackUserID(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    }
                });
    }

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
