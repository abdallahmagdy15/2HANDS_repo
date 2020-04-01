package com.example.a2hands.users;

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

public class UsersFragment extends Fragment  {

    View view;
    String id;
    final List<User> users = new ArrayList<>();

    public UsersFragment() {
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
        id = b.getString("ID");
        final String activityName = b.getString("FOR");
        getUsersId(activityName.toLowerCase());
        return view;
    }
    void getUsersId(String type ){
        FirebaseDatabase.getInstance().getReference(type).child(id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final List<String> usersId = new ArrayList<>();
                        if(dataSnapshot.getChildrenCount() !=0){
                            for(DataSnapshot ds : dataSnapshot.getChildren()){
                                usersId.add(ds.getKey());
                            }
                            getUsers(usersId);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void getUsers(List<String> usersId){
        FirebaseFirestore.getInstance().collection("users")
                .whereIn("user_id",usersId).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for( DocumentSnapshot sn : task.getResult()){
                                users.add(sn.toObject(User.class));
                            }
                            updateUiWithUsers();
                        }
                    }
                });
    }
    void updateUiWithUsers(){
        Context context = view.getContext();
        final RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new MyuserRecyclerViewAdapter(users));

    }
}
