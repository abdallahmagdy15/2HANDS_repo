package com.example.a2hands.settings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.a2hands.ChangeLocale;
import com.example.a2hands.R;
import com.example.a2hands.UserStatus;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditCountryActivity extends AppCompatActivity {

    private CountryCodePicker ccpCountry;
    private Spinner stateSelect;
    private Button saveCountry;

    //firebase
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeLocale.loadLocale(getBaseContext());
        setContentView(R.layout.activity_edit_country);

        Toolbar toolbar = findViewById(R.id.editCountryToolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.editCountry));

        saveCountry = findViewById(R.id.saveNewCountry_btn);
        ccpCountry = findViewById(R.id.ccpCountry_editCountry);
        ccpCountry.setAutoDetectedCountry(true);
        stateSelect = findViewById(R.id.settingsEditStates);


        //changes the phone code and update states spinner when country changes
        setUpStatesSpinner(ccpCountry.getSelectedCountryNameCode());
        ccpCountry.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                //load spinners items
                setUpStatesSpinner(ccpCountry.getSelectedCountryNameCode());
            }
        });

        saveCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCountryAndRegion();
            }
        });

    }

    private void updateCountryAndRegion() {
        Map<String, Object> newCountry = new HashMap<>();
        newCountry.put("country", ccpCountry.getSelectedCountryNameCode());

        db.collection("users").document(user.getUid()).update(newCountry)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("saveNewCountry", "Done");
                        startActivity(new Intent(EditCountryActivity.this , SettingsActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("saveNewCountry", e.toString());
                        startActivity(new Intent(EditCountryActivity.this , SettingsActivity.class));
                        finish();
                    }
                });

        Map<String, Object> newRegion = new HashMap<>();
        newRegion.put("region", stateSelect.getSelectedItem().toString());

        db.collection("users").document(user.getUid()).update(newRegion)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("saveNewCountry", "Done");
                        startActivity(new Intent(EditCountryActivity.this , SettingsActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("saveNewCountry", e.toString());
                        startActivity(new Intent(EditCountryActivity.this , SettingsActivity.class));
                        finish();
                    }
                });
    }


    public void setUpStatesSpinner(String selectedCountry){
        try {
            JSONObject obj = new JSONObject(loadCountryStateJSONFromAsset());
            JSONArray countries_arr = obj.getJSONArray("countries");
            ArrayList<String> countries = new ArrayList<>();
            Map<String,ArrayList<String>> countries_states = new HashMap<>();

            for (int i = 0; i < countries_arr.length(); i++) {
                JSONObject jo_inside = countries_arr.getJSONObject(i);
                String iso2 = jo_inside.getString("iso2");
                JSONArray json_states = jo_inside.getJSONArray("states");

                ArrayList<String> states = new ArrayList<>();
                for(int j = 0; j < json_states.length(); j++)
                    states.add(json_states.getString(j));

                countries_states.put(iso2, states);
                countries.add(iso2);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.simple_item_in_spinner, countries_states.get(selectedCountry));
            stateSelect.setAdapter(adapter);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


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



    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        UserStatus.updateOnlineStatus(true, user.getUid());
        super.onResume();
    }

    @Override
    protected void onStop()
    {
        if(UserStatus.isAppIsInBackground(getApplicationContext())){
            UserStatus.updateOnlineStatus(false, user.getUid());
        }
        super.onStop();
    }


}
