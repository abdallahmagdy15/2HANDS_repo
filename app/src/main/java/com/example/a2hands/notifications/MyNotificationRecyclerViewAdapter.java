package com.example.a2hands.notifications;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;


import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a2hands.R;
import com.example.a2hands.rating.Rating;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;


public class MyNotificationRecyclerViewAdapter extends RecyclerView.Adapter<MyNotificationRecyclerViewAdapter.ViewHolder> {

    private final List<Notification> notifisList;

    public MyNotificationRecyclerViewAdapter(List<Notification> items) {
        notifisList = items;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public TextView notifiDesc;
        public TextView  notifiTime;
        public CircleImageView notifiPic;
        public ImageView notifiTypePic;
        public Button acceptReqBtn;
        public Button refuseReqBtn;
        public LinearLayout notifiContainer;



        public ViewHolder(View view) {
            super(view);
            mView = view;
            notifiContainer = (LinearLayout) view;
        }

    }
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int pos) {
    final ViewHolder vh ;
        //check type of notifi
        if(notifisList.get(pos).type.equals("HELP_REQUEST")) {
            vh = setLayoutViews(holder,notifisList.get(pos).type);

            //set listener for acc btn
            vh.acceptReqBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteNotifiAndhelpReq(pos,vh);
                    addRating(vh,pos);

                }
            });
            holder.refuseReqBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteNotifiAndhelpReq(pos,vh);
                    Toast.makeText(vh.mView.getContext(),"Help Request Refused",Toast.LENGTH_SHORT).show();
                    //delete help request
                    FirebaseFirestore.getInstance().collection("help_requests")
                            .document(notifisList.get(pos).help_request_id).delete();
                }
            });
        }

        else{
            vh = setLayoutViews(holder,notifisList.get(pos).type);
            setLayoutViews(holder,notifisList.get(pos).type);
        }

        /////set notification info

        String notifiDesc = getotifiDescWithBoldedName(notifisList.get(pos).content);
        vh.notifiDesc.setText(Html.fromHtml(notifiDesc));

        PrettyTime p = new PrettyTime();
        holder.notifiTime.setText(p.format(notifisList.get(pos).date));
        //load pic of notifi publisher and put into the image view
        FirebaseStorage.getInstance().getReference()
                .child("Profile_Pics/" +notifisList.get(pos).publisher_id+ "/"
                        + notifisList.get(pos).publisher_pic)
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri.toString()).into(vh.notifiPic);
            }
        });


    }
    private String getotifiDescWithBoldedName(String content){
        //get Name to be bold
        String [] words = content.split(" ");
        String publisherName="";
        String notifiDesc= content;
        int i=0;
        for (String word:words) {
            if(i==0)
                publisherName += word + " ";
            else if(i==1)
                publisherName += word;
            i++;
        }
        return notifiDesc = notifiDesc.replace(publisherName,"<b>"+publisherName+"</b>");
    }

    void addRating(final ViewHolder vh , int pos){
        /////add user to ratings
        Rating rating = new Rating();
        //rating subscriber is who will be rated by publisher
        rating.subscriber_id = notifisList.get(pos).publisher_id;
        rating.publisher_id = notifisList.get(pos).subscriber_id;
        rating.post_id = notifisList.get(pos).post_id;
        rating.subscriber_pic = notifisList.get(pos).publisher_pic;
        rating.subscriber_name = notifisList.get(pos).publisher_name;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ratings");
        String rating_id = ref.push().getKey();
        rating.rating_id = rating_id;
        rating.review_text="";
        rating.date = new Date();
        ref.child(rating_id).setValue(rating);
        Toast.makeText(vh.mView.getContext(),"Help request accepted", Toast.LENGTH_SHORT).show();
        //update counter for ratings
        FirebaseDatabase.getInstance().getReference("counter").child(rating.post_id )
                .child("ratings_count").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                int curr_ = mutableData.getValue(Integer.class);
                mutableData.setValue(curr_ +1);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

            }
        });
        //end update counter for ratings

        //delete help request
        FirebaseFirestore.getInstance().collection("help_requests")
                .document(notifisList.get(pos).help_request_id).delete();
    }

    void deleteNotifiAndhelpReq(final int pos,final ViewHolder holder){
        //delete help request record from firestore db
        FirebaseFirestore.getInstance().collection("help_requests")
                .document(notifisList.get(pos).help_request_id)
                .delete();
        //delete this notification
        FirebaseDatabase.getInstance().getReference("notifications")
                .child(notifisList.get(pos).notification_id).removeValue();
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_notification, parent, false);
        return new ViewHolder(view);
    }

    ViewHolder setLayoutViews(ViewHolder holder , String type){
        LayoutInflater inflater = (LayoutInflater)holder.mView.getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout;
        //check notifi type
        if(type.equals("HELP_REQUEST")){
            //setting the inner layout
            layout = inflater.inflate(R.layout.notification_request, null);
            holder.notifiContainer.removeAllViews();
            holder.notifiContainer.addView(layout);

            holder.notifiTypePic = holder.notifiContainer.findViewById(R.id.notifiTypePic);
            holder.acceptReqBtn = holder.notifiContainer.findViewById(R.id.acceptReqBtn);
            holder.refuseReqBtn = holder.notifiContainer.findViewById(R.id.refuseReqBtn);
        }
        else {
            //setting the inner layout
            layout = inflater.inflate(R.layout.notification_react, null);
            holder.notifiContainer.removeAllViews();
            holder.notifiContainer.addView(layout);
            holder.notifiTypePic = holder.notifiContainer.findViewById(R.id.notifiTypePic);
            if(type.equals("LIKE_POST")){
                holder.notifiTypePic.setImageResource(R.drawable.like_filled);
            }
            else if(type.equals("COMMENT_POST")){
                holder.notifiTypePic.setImageResource(R.drawable.comment_filled);
            }
            else if(type.equals("SHARE_POST")){
                holder.notifiTypePic.setImageResource(R.drawable.share_filled);
            }
            else if(type.equals("FOLLOWED")){
                holder.notifiTypePic.setImageResource(R.drawable.followed_user);
            }
        }

        //set the views
        holder.notifiDesc = holder.notifiContainer.findViewById(R.id.notifiDesc);
        holder.notifiTime = holder.notifiContainer.findViewById(R.id.notifiTime);
        holder.notifiPic = holder.notifiContainer.findViewById(R.id.notifiPic);
        return holder;
    }
    @Override
    public int getItemCount() {
        return notifisList.size();
    }
}
