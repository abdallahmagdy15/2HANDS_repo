package com.example.a2hands.homePackage;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.a2hands.homePackage.PostFragment.OnListFragmentInteractionListener;
import com.example.a2hands.R;

import java.util.List;
import org.ocpsoft.prettytime.PrettyTime;

public class MyPostRecyclerViewAdapter extends RecyclerView.Adapter<MyPostRecyclerViewAdapter.ViewHolder> {

    private final List<Post> postsList;
    private final OnListFragmentInteractionListener mListener;

    public MyPostRecyclerViewAdapter(List<Post> posts, OnListFragmentInteractionListener listener) {
        postsList = posts;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_post, parent, false);
        return new ViewHolder(view);
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView postOwner;
        public final TextView postContent;
        public final TextView time;
        public final TextView visibility;
        public Post post;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            time=view.findViewById(R.id.postTime);
            visibility=view.findViewById(R.id.postVisibility);
            postOwner = view.findViewById(R.id.postOwner);
            postContent =  view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + postContent.getText() + "'";
        }
    }
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.post = postsList.get(position);
        holder.postOwner.setText(postsList.get(position).postOwner);
        holder.postContent.setText(postsList.get(position).content_text);
        PrettyTime p = new PrettyTime();
        holder.time.setText(p.format(postsList.get(position).date));
        holder.visibility.setText(postsList.get(position).visibility);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    //mListener.onListFragmentInteraction();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return postsList.size();
    }


}
