package com.example.a2hands.RatingPackage;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class RatingFragment extends Fragment {

    private OnListFragmentInteractionListener mListener;
    private String activity;

    public RatingFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_rating_list, container, false);

        activity = getArguments().getString("for");
        //check fragment is called for home post or profile reviews tab
        if(activity.equals("home")){
            String post_id = getArguments().getString("postId");

            FirebaseDatabase.getInstance().getReference("ratings")
                    .orderByChild("post_id").equalTo(post_id)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            List<Rating> ratings = new ArrayList<>();
                            for( DataSnapshot snapshot : dataSnapshot.getChildren()){
                                ratings.add(snapshot.getValue(Rating.class));
                            }
                            String uid= FirebaseAuth.getInstance().getCurrentUser().getUid();
                            updatePostRatingsUI(ratings,view,uid);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
        else{
            final String uid = getArguments().getString("uid");

            FirebaseDatabase.getInstance().getReference("ratings")
                    .orderByChild("subscriber_id").equalTo(uid)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            List<Rating> ratings = new ArrayList<>();
                            for( DataSnapshot snapshot : dataSnapshot.getChildren()){
                                ratings.add(snapshot.getValue(Rating.class));
                            }
                            updatePostRatingsUI(ratings,view ,uid);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

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

    void updatePostRatingsUI(List<Rating> ratings, View view ,String uid){
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new MyRatingRecyclerViewAdapter(ratings, mListener , uid , activity));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Rating item);
    }
}
