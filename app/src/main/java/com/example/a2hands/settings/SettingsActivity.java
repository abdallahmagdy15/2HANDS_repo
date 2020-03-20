package com.example.a2hands.settings;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.a2hands.R;
import com.example.a2hands.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //account
        TextView editNamebtn = findViewById(R.id.btn_editName);
        TextView editEmailbtn = findViewById(R.id.btn_editEmail);
        TextView editPhonebtn = findViewById(R.id.btn_editPhone);
        TextView editCountrybtn = findViewById(R.id.btn_editCountry);
        TextView editPassbtn = findViewById(R.id.btn_editPass);
        final TextView editPhoneTxt = findViewById(R.id.txtView_editPhone);
        final TextView editNameTxt = findViewById(R.id.txtView_editName);
        final TextView editCountryTxt = findViewById(R.id.txtView_editCountry);
        final TextView editEmailTxt = findViewById(R.id.txtView_editEmail);
        TextView editPassTxt = findViewById(R.id.textView_editPass);
        TextView deleteAccTxt = findViewById(R.id.textView_deleteAcc);
        deleteAccTxt.setBackgroundColor(Color.TRANSPARENT);

        //general
        ListView generalListView = findViewById(R.id.listView_generalSettings);
        final String[] generalSettingsItems = {"Language","Notifications", "Blocked Accounts","Night Mode", "About 2Hands"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.simple_item_in_listview,generalSettingsItems);
        generalListView.setAdapter(adapter);

        generalListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String itemSelected = generalSettingsItems[position];

                switch (itemSelected){
                    case "Language":
                        startActivity(new Intent(SettingsActivity.this , LanguageActivity.class));
                        break;
                    case "Notifications":
                        startActivity(new Intent(SettingsActivity.this , NotificationActivity.class));
                        break;
                    case "Blocked Accounts":
                        startActivity(new Intent(SettingsActivity.this , BlockedUsersActivity.class));
                        break;
                    case "Night Mode":

                        break;
                    case "About 2Hands":
                        startActivity(new Intent(SettingsActivity.this , AboutAppActivity.class));
                        break;
                }
            }
        });


        editNamebtn.setOnClickListener(this);
        editEmailbtn.setOnClickListener(this);
        editPhonebtn.setOnClickListener(this);
        editPassbtn.setOnClickListener(this);
        editCountrybtn.setOnClickListener(this);
        editPassTxt.setOnClickListener(this);
        deleteAccTxt.setOnClickListener(this);


        //drawer header data
        FirebaseFirestore.getInstance().collection("users/").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                User user = task.getResult().toObject(User.class);

                editNameTxt.setText(user.full_name);
                editCountryTxt.setText(user.country);
                editPhoneTxt.setText(user.phone);
                editEmailTxt.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
            }
        });


    }// end of onCreate method


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_editName:
                startActivity(new Intent(SettingsActivity.this , EditNameActivity.class));
                break;
            case R.id.btn_editEmail:
                startActivity(new Intent(SettingsActivity.this , EditEmailActivity.class));
                break;
            case R.id.btn_editPhone:
                startActivity(new Intent(SettingsActivity.this , EditPhoneActivity.class));
                break;
            case R.id.btn_editPass:
                startActivity(new Intent(SettingsActivity.this , EditPassActivity.class));
                break;
            case R.id.textView_editPass:
                startActivity(new Intent(SettingsActivity.this , EditPassActivity.class));
                break;
            case R.id.btn_editCountry:
                startActivity(new Intent(SettingsActivity.this , EditCountryActivity.class));
                break;
            case R.id.textView_deleteAcc:
                startActivity(new Intent(SettingsActivity.this , DeleteAccActivity.class));
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
