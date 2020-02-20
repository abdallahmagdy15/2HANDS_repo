package com.example.a2hands.homePackage;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.a2hands.ProfileActivity;
import com.example.a2hands.R;
import com.example.a2hands.Rating;
import com.example.a2hands.User;
import com.example.a2hands.homePackage.RatingFragment.OnListFragmentInteractionListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MyRatingRecyclerViewAdapter extends RecyclerView.Adapter<MyRatingRecyclerViewAdapter.ViewHolder> {

    private final List<Rating> ratingsList;
    private final OnListFragmentInteractionListener mListener;
    private final String uid;
    public MyRatingRecyclerViewAdapter(List<Rating> ratings, OnListFragmentInteractionListener listener , String uid) {
        ratingsList = ratings;
        mListener = listener;
        this.uid = uid;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_rating, parent, false);

        return new ViewHolder(view);
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView ratingSubscriber;
        public final RatingBar ratingBarGet;
        public final RatingBar ratingBarSet;
        public final TextView ratingDate;
        public final EditText ratingWriteReview;
        public final TextView reviewText;
        public final CircleImageView ratingsPic;
        public final LinearLayout postReviewContainer;
        public final ImageButton postReview;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            ratingSubscriber = view.findViewById(R.id.ratingSubscriber);
            ratingBarGet = view.findViewById(R.id.ratingBarGet);
            ratingBarSet = view.findViewById(R.id.ratingBarSet);
            ratingDate = view.findViewById(R.id.ratingDate);
            ratingWriteReview = view.findViewById(R.id.ratingWriteReview);
            ratingsPic = view.findViewById(R.id.ratingsPic);
            postReviewContainer = view.findViewById(R.id.postReviewContainer);
            postReview = view.findViewById(R.id.postReview);
            reviewText = view.findViewById(R.id.reviewText);
        }
    }
    @Override
    public void onBindViewHolder(final ViewHolder holder,final int pos) {
        holder.ratingSubscriber.setText(ratingsList.get(pos).subscriber_name);
        FirebaseStorage.getInstance().getReference().child("Profile_Pics/"+ratingsList.get(pos).subscriber_id
                + "/"+ratingsList.get(pos).subscriber_pic).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(holder.ratingsPic);
                    }
                });

        /////check if the current user is the owner of ratings post
        if(uid.equals(ratingsList.get(pos).publisher_id)){
            //check if a review was submitted
            if(ratingsList.get(pos).review_text.equals("")){//not
                holder.reviewText.setVisibility(View.GONE);
                holder.ratingDate.setVisibility(View.GONE);
                holder.ratingBarGet.setVisibility(View.GONE);
                holder.postReviewContainer.setVisibility(View.VISIBLE);
                holder.ratingBarSet.setVisibility(View.VISIBLE);
                holder.ratingBarSet.setRating(ratingsList.get(pos).rate);
                holder.ratingBarSet.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar ratingBar, final float rating, boolean fromUser) {
                        //save rating to ratings && calc the total rate for the subscriber
                        FirebaseDatabase.getInstance().getReference("ratings")
                                .child(ratingsList.get(pos).rating_id)
                                .child("rate").setValue((int)rating);
                        FirebaseFirestore.getInstance().collection("users")
                                .document(ratingsList.get(pos).subscriber_id).get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot doc) {
                                        User user = doc.toObject(User.class);
                                        //basic added rate value ranging between [0.1 , -0.1]
                                        double basicRating =  (rating/50)- 0.05;

                                        double bonusRating;
                                        if(basicRating>0) {
                                            //using quadratic eq (more previous rating , more bonus rating)
                                            bonusRating = Math.pow(rating, 1.8) * basicRating;
                                        }
                                        else {
                                            bonusRating = 0;
                                        }

                                        user.rate += bonusRating+basicRating;
                                        user.ratings_count++;
                                        FirebaseFirestore.getInstance().collection("users")
                                                .document(ratingsList.get(pos).subscriber_id)
                                                .set(user);
                                    }
                                });
                    }
                });
                holder.postReview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //save review to the ratings db
                        FirebaseDatabase.getInstance().getReference().child("ratings")
                                .child(ratingsList.get(pos).rating_id)
                                .child("review_text").setValue(holder.ratingWriteReview.getText().toString());

                    }
                });

            }
            else{//yes
                holder.ratingBarGet.setRating(ratingsList.get(pos).rate);
                PrettyTime p = new PrettyTime();
                holder.ratingDate.setText(p.format(ratingsList.get(pos).date));
                holder.reviewText.setText(ratingsList.get(pos).review_text);
            }
        }
        else{
            //check if a review was submitted
            if(ratingsList.get(pos).review_text.equals("")){//not
                holder.ratingBarGet.setRating(ratingsList.get(pos).rate);
                PrettyTime p = new PrettyTime();
                holder.ratingDate.setText(p.format(ratingsList.get(pos).date));
            }
            else {//yes
                holder.ratingBarGet.setRating(ratingsList.get(pos).rate);
                PrettyTime p = new PrettyTime();
                holder.ratingDate.setText(p.format(ratingsList.get(pos).date));
                holder.reviewText.setText(ratingsList.get(pos).review_text);
            }

        }
        holder.ratingsPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(holder.mView.getContext(), ProfileActivity.class);
                i.putExtra("uid",ratingsList.get(pos).subscriber_id);
                holder.mView.getContext().startActivity(i);
            }
        });

    }

    @Override
    public int getItemCount() {
        return ratingsList.size();
    }


}
