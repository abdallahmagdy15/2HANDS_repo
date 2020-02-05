package com.example.a2hands;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.gms.tasks.OnCompleteListener;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.example.a2hands.dummy.DummyContent.DummyItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

interface Callback{
    void callbackUser(User user);
    void callbackUserID(String uid);
}
public class PostFragment extends Fragment {

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    private OnListFragmentInteractionListener mListener;

    public PostFragment() {
    }

    public static PostFragment newInstance() {
        PostFragment fragment = new PostFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_post_list, container, false);


        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        //get posts from database
        autoSigningin(new Callback() {
            @Override
            public void callbackUser(User user) { }
            @Override
            public void callbackUserID(String uid) {
                getUser(new Callback() {
                    @Override
                    public void callbackUser(User user) {
                        List<String> visibility =new ArrayList<>();
                        visibility.add(user.country);
                        visibility.add(user.region);

                        getPosts(visibility,"general", view);
                    }

                    @Override
                    public void callbackUserID(String uid){ }
                },uid);

            }
        });
        //End get posts

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

    public void getPosts(List<String> visibility, String category,final View view ){
        // Read from the database
        db.collection("/posts")
                .whereIn("visibility", visibility)
                .whereEqualTo("category",category)
                .orderBy("date")
                .limitToLast(30)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Post> posts = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                Post p = doc.toObject(Post.class);
                                posts.add(p);
                            }
                            updateHomeWithPosts(posts,view);
                        } else {
                            Log.w("", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    public void autoSigningin(final Callback callback){
        mAuth.signInWithEmailAndPassword("ahmedKamal9@gmail.com", "556558554552")
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
        void onListFragmentInteraction(DummyItem item);
    }
}
