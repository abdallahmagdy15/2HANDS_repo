package com.example.a2hands.profile;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.a2hands.R;
import com.example.a2hands.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class userFragment extends Fragment  {


    private OnListFragmentInteractionListener mListener;
    View view;
    String uid;

    public userFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user_list, container, false);
        Bundle b = this.getArguments();
        uid = b.getString("uid");
        final String for_ = b.getString("for");
        if(for_.equals("followers")){
            getUsers("followers" );
        }
        else {
            getUsers("followings" );
        }
        return view;
    }
void getUsers(String type ){
    Context context = view.getContext();
    final RecyclerView recyclerView = (RecyclerView) view;
    recyclerView.setLayoutManager(new LinearLayoutManager(context));
    FirebaseDatabase.getInstance().getReference(type).child(uid)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    final List<String> usersIds = new ArrayList<>();
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        usersIds.add(ds.getKey());
                    }
                    FirebaseFirestore.getInstance().collection("users")
                            .whereIn("user_id",usersIds).get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    final List<User> users = new ArrayList<>();
                                    if(task.isSuccessful())
                                        for( DocumentSnapshot sn : task.getResult()){
                                            users.add(sn.toObject(User.class));
                                        }
                                    recyclerView.setAdapter(new MyuserRecyclerViewAdapter(users));
                                }
                            });
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
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
        void onListFragmentInteraction(int i);
    }
}
