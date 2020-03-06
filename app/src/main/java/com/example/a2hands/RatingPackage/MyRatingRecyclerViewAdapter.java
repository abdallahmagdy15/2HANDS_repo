package com.example.a2hands.RatingPackage;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.a2hands.Callback;
import com.example.a2hands.ProfileActivity;
import com.example.a2hands.R;
import com.example.a2hands.User;
import com.example.a2hands.RatingPackage.RatingFragment.OnListFragmentInteractionListener;
import com.example.a2hands.homePackage.PostsPackage.PostFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MyRatingRecyclerViewAdapter extends RecyclerView.Adapter<MyRatingRecyclerViewAdapter.ViewHolder> {

    private final List<Rating> ratingsList;
    private final OnListFragmentInteractionListener mListener;
    private final String uid;
    private final String activity;
    public MyRatingRecyclerViewAdapter(List<Rating> ratings, OnListFragmentInteractionListener listener , String uid , String activity) {
        ratingsList = ratings;
        mListener = listener;
        this.uid = uid;
        this.activity =activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_rating, parent, false);

        return new ViewHolder(view);
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView ratingUserName;
        public final RatingBar ratingBarGet;
        public final RatingBar ratingBarSet;
        public final TextView ratingDate;
        public final EditText ratingWriteReview;
        public final TextView reviewText;
        public final CircleImageView ratingsPic;
        public final LinearLayout postReviewContainer;
        public final ImageView postReview;
        public final TextView ratingToText;
        public final TextView ratingWriteReviewBtn;
        public final TextView ratingFinishSubmitBtn;
        public final LinearLayout ratingConfirmSubmitContainer;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            ratingUserName = view.findViewById(R.id.ratingUserName);
            ratingBarGet = view.findViewById(R.id.ratingBarGet);
            ratingBarSet = view.findViewById(R.id.ratingBarSet);
            ratingDate = view.findViewById(R.id.ratingDate);
            ratingWriteReview = view.findViewById(R.id.ratingWriteReview);
            ratingsPic = view.findViewById(R.id.ratingsPic);
            postReviewContainer = view.findViewById(R.id.postReviewContainer);
            postReview = view.findViewById(R.id.postReview);
            reviewText = view.findViewById(R.id.reviewText);
            ratingToText = view.findViewById(R.id.ratingToText);
            ratingWriteReviewBtn = view.findViewById(R.id.ratingWriteReviewBtn);
            ratingFinishSubmitBtn = view.findViewById(R.id.ratingFinishSubmitBtn);
            ratingConfirmSubmitContainer = view.findViewById(R.id.ratingConfirmSubmitContainer);

        }
    }
    @Override
    public void onBindViewHolder(final ViewHolder holder,final int pos) {

        ////check the activity to change the layout
        if(activity.equals("profile")){
            PostFragment.getUser(new Callback() {
                @Override
                public void callbackUser(final User user) {
                    //load publisher pic
                    FirebaseStorage.getInstance().getReference().child("Profile_Pics/"+user.user_id
                            + "/"+user.profile_pic).getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Picasso.get().load(uri).into(holder.ratingsPic);
                                }
                            });
                    String fullname = user.first_name + " "+ user.last_name;
                    holder.ratingUserName.setText(fullname);
                    //publisher pic on click
                    holder.ratingsPic.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(holder.mView.getContext(), ProfileActivity.class);
                            i.putExtra("uid",user.user_id);
                            holder.mView.getContext().startActivity(i);
                        }
                    });

                }
            },ratingsList.get(pos).publisher_id);
            holder.ratingToText.setVisibility(View.GONE);


        } else {
            //load subscriber pic
            FirebaseStorage.getInstance().getReference().child("Profile_Pics/"+ratingsList.get(pos).subscriber_id
                    + "/"+ratingsList.get(pos).subscriber_pic).getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri).into(holder.ratingsPic);
                        }
                    });
            holder.ratingUserName.setText(ratingsList.get(pos).subscriber_name);
            //subscriber pic on click
            holder.ratingsPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(holder.mView.getContext(), ProfileActivity.class);
                    i.putExtra("uid",ratingsList.get(pos).subscriber_id);
                    holder.mView.getContext().startActivity(i);
                }
            });
        }

        /////check if the current user is the owner of ratings post
        if(uid.equals(ratingsList.get(pos).publisher_id)){
            //check if a rating was submitted
            if(ratingsList.get(pos).rate == 0){//not
                holder.reviewText.setVisibility(View.GONE);
                holder.ratingDate.setVisibility(View.GONE);
                holder.ratingBarGet.setVisibility(View.GONE);
                holder.ratingBarSet.setVisibility(View.VISIBLE);
                holder.ratingBarSet.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar ratingBar, final float rating, boolean fromUser) {
                        //confirm writing a review or finish
                        holder.ratingConfirmSubmitContainer.setVisibility(View.VISIBLE);
                        //on clicking write review
                        holder.ratingWriteReviewBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                holder.ratingConfirmSubmitContainer.setVisibility(View.GONE);
                                holder.postReviewContainer.setVisibility(View.VISIBLE);
                                holder.postReview.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //save review to the ratings db
                                        FirebaseDatabase.getInstance().getReference().child("ratings")
                                                .child(ratingsList.get(pos).rating_id)
                                                .child("review_text").setValue(holder.ratingWriteReview.getText().toString());
                                        //save time of posting the review
                                        FirebaseDatabase.getInstance().getReference().child("ratings")
                                                .child(ratingsList.get(pos).rating_id)
                                                .child("date").setValue(new Date());
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
                                                        user.rate = ( user.rate + 2 * rating)/2;
                                                        user.ratings_count++;
                                                        FirebaseFirestore.getInstance().collection("users")
                                                                .document(ratingsList.get(pos).subscriber_id)
                                                                .set(user);
                                                    }
                                                });
                                    }
                                });
                            }
                        });
                        //on clicking finished rating
                        holder.ratingFinishSubmitBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //save rating to ratings
                                FirebaseDatabase.getInstance().getReference("ratings")
                                        .child(ratingsList.get(pos).rating_id)
                                        .child("rate").setValue((int)rating);
                                //save time of posting the review
                                FirebaseDatabase.getInstance().getReference().child("ratings")
                                        .child(ratingsList.get(pos).rating_id)
                                        .child("date").setValue(new Date());
                                //calc the total rate for the subscriber
                                FirebaseFirestore.getInstance().collection("users")
                                        .document(ratingsList.get(pos).subscriber_id).get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot doc) {
                                                User user = doc.toObject(User.class);
                                                user.rate = ( user.rate + 2 * rating)/2;
                                                user.ratings_count++;
                                                FirebaseFirestore.getInstance().collection("users")
                                                        .document(ratingsList.get(pos).subscriber_id)
                                                        .set(user);
                                            }
                                        });

                            }
                        });

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
                holder.ratingBarGet.setRating(ratingsList.get(pos).rate);
                PrettyTime p = new PrettyTime();
                holder.ratingDate.setText(p.format(ratingsList.get(pos).date));
                String review_txt= ratingsList.get(pos).review_text;
                if(review_txt.equals(""))
                    holder.reviewText.setVisibility(View.GONE);
                else
                    holder.reviewText.setText(review_txt);
        }


    }

    @Override
    public int getItemCount() {
        return ratingsList.size();
    }


}
