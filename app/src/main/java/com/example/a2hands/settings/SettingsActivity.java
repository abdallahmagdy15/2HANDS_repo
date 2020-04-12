package com.example.a2hands.settings;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.a2hands.ChangeLocale;
import com.example.a2hands.R;
import com.example.a2hands.User;
import com.example.a2hands.home.HomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    String[] generalSettingsItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeLocale.loadLocale(getBaseContext());
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.settingsAppToolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.settings));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
            }
        });

        //account
        TextView editNamebtn = findViewById(R.id.btn_editName);
        TextView editUserNamebtn = findViewById(R.id.btn_editUserName);
        TextView editEmailbtn = findViewById(R.id.btn_editEmail);
        TextView editPhonebtn = findViewById(R.id.btn_editPhone);
        TextView editCountrybtn = findViewById(R.id.btn_editCountry);
        TextView editPassbtn = findViewById(R.id.btn_editPass);
        final TextView editPhoneTxt = findViewById(R.id.txtView_editPhone);
        final TextView editNameTxt = findViewById(R.id.txtView_editName);
        final TextView editUserNameTxt = findViewById(R.id.txtView_editUserName);
        final TextView editCountryTxt = findViewById(R.id.txtView_editCountry);
        final TextView editEmailTxt = findViewById(R.id.txtView_editEmail);
        TextView editPassTxt = findViewById(R.id.textView_editPass);
        TextView deleteAccTxt = findViewById(R.id.textView_deleteAcc);
        deleteAccTxt.setBackgroundColor(Color.TRANSPARENT);

        //general
        ListView generalListView = findViewById(R.id.listView_generalSettings);

        generalSettingsItems = getResources().getStringArray(R.array.generalSettings);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.simple_item_in_listview,generalSettingsItems);
        generalListView.setAdapter(adapter);

        setListViewHeightBasedOnChildren(generalListView);

        generalListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //String itemSelected = generalSettingsItems[position];

                switch (position){
                    case 0:
                        startActivity(new Intent(SettingsActivity.this , LanguageActivity.class));
                        break;
                    case 1:
                        startActivity(new Intent(SettingsActivity.this , NotificationActivity.class));
                        break;
                    case 2:
                        startActivity(new Intent(SettingsActivity.this , BlockedUsersActivity.class));
                        break;
                    case 3:

                        break;
                    case 4:
                        startActivity(new Intent(SettingsActivity.this , AboutAppActivity.class));
                        break;
                }
            }
        });

        editNamebtn.setOnClickListener(this);
        editUserNamebtn.setOnClickListener(this);
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
                editUserNameTxt.setText("@" + user.user_name);
                editCountryTxt.setText(loadCountryUsingItsISO(user.country));
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
            case R.id.btn_editUserName:
                startActivity(new Intent(SettingsActivity.this , EditUserNameActivity.class));
                break;
            case R.id.btn_editEmail:
                startActivity(new Intent(SettingsActivity.this , EditEmailActivity.class));
                break;
            case R.id.btn_editPhone:
                startActivity(new Intent(SettingsActivity.this , EditPhoneActivity.class));
                break;
            case R.id.btn_editPass:
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
        startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
    }


    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }


    // loading JSON file of countries and states from assets folder
    public String loadCountryStateJSONFromAsset() {
        String json = null;
        try {
            InputStream inputStreanm = this.getAssets().open("countriesandstates.json");
            int size = inputStreanm.available();
            byte[] buffer = new byte[size];
            inputStreanm.read(buffer);
            inputStreanm.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public String loadCountryUsingItsISO(String countryCode){
        try {
            JSONObject obj = new JSONObject(loadCountryStateJSONFromAsset());
            JSONArray countries_arr = obj.getJSONArray("countries");

            Map<String,String> countries_code_name = new HashMap<>();

            for (int i = 0; i < countries_arr.length(); i++) {
                JSONObject jo_inside = countries_arr.getJSONObject(i);
                String iso2 = jo_inside.getString("iso2");
                String country_name = jo_inside.getString("name");

                countries_code_name.put(iso2,country_name);
            }
            return countries_code_name.get(countryCode);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


}
