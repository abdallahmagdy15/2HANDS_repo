package com.example.a2hands.homePackage.RepliesPackage;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.a2hands.R;
import com.example.a2hands.homePackage.CommentsPackage.Comment;
import com.example.a2hands.homePackage.CommentsPackage.CommentsFragment;
import com.example.a2hands.homePackage.CommentsPackage.MyCommentRecyclerViewAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RepliesFragment extends Fragment implements CommentsFragment.OnListFragmentInteractionListener {

    private OnListFragmentInteractionListener mListener;

    public RepliesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_replies_list, container, false);

        String postId = getArguments().getString("postId");
        String commentId = getArguments().getString("commentId");
        FirebaseDatabase.getInstance().getReference("Replies").child(postId).child(commentId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Reply> replies = new ArrayList<>();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            replies.add(snapshot.getValue(Reply.class));
                        }
                        updateRepliesContainer(replies, view);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
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

    public void updateRepliesContainer(List<Reply> replies, View view) {
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new MyRepliesRecyclerViewAdapter(replies, mListener));

        }
    }

    @Override
    public void onListFragmentInteraction(Comment item) {

    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Reply item);
    }
}