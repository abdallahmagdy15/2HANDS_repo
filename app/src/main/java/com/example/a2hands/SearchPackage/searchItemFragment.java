package com.example.a2hands.SearchPackage;

import android.content.Context;
import android.icu.lang.UScript;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

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
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class searchItemFragment extends Fragment {
    private OnListFragmentInteractionListener mListener;
    final List<User> users = new ArrayList<>();


    public searchItemFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_searchitem_list, container, false);

        //get Search results
        String query = getArguments().getString("search_query");
        //check if query null to load the recent users
        if(query.equals("")){
            FirebaseDatabase.getInstance().getReference("recent_users")
                    .child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //get recent users ids
                    List<String> recentUsers = new ArrayList<>();
                    for(DataSnapshot dataSnap:dataSnapshot.getChildren()){
                        recentUsers.add(dataSnap.getKey());
                    }
                    if(!recentUsers.isEmpty())
                    //get recent users data
                    FirebaseFirestore.getInstance().collection("users")
                            .whereIn("user_id",recentUsers).get().addOnCompleteListener(
                            new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    loadUsersData(task , view);
                                }
                            });
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }

        else {
            try {
                FirebaseFirestore.getInstance().collection("users")
                        .orderBy("first_name")
                        .startAt(query)
                        .endAt(query + "\uf8ff")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                loadUsersData(task , view);
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return view;
    }

    void loadUsersData(@NonNull Task<QuerySnapshot> task , View view ){
        for (DocumentSnapshot doc : task.getResult()) {
            users.add(doc.toObject(User.class));
        }
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(new MysearchItemRecyclerViewAdapter(users, mListener));
        }
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

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(User item);
    }
}
