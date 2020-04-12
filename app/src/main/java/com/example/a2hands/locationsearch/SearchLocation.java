package com.example.a2hands.locationsearch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.a2hands.ChangeLocale;
import com.example.a2hands.R;
import com.example.a2hands.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
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

public class SearchLocation extends AppCompatActivity {

    SearchView search;
    ListView listOfSearchGovs;
    ArrayAdapter<String> adapter;
    ArrayList<String> statesArray;

    Button showInAllStatesbtn;

    String location;

    private CountryCodePicker ccpCountry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeLocale.loadLocale(getBaseContext());
        setContentView(R.layout.activity_search_location);

        ccpCountry = findViewById(R.id.ccpCountry_postLocation);
        showInAllStatesbtn = findViewById(R.id.showInAllStatesbtn);
        showInAllStatesbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("SELECTED_LOCATION", location);
                setResult(Activity.RESULT_OK,intent);
                finish();
            }
        });

        //search Location
        search = findViewById(R.id.search);
        listOfSearchGovs = findViewById(R.id.listOfSearchGovs);

        FirebaseFirestore.getInstance().collection("/users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get()
                .addOnCompleteListener(
                        new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                User user = task.getResult().toObject(User.class);
                                ccpCountry.setCountryForNameCode(user.country);
                                statesArray = loadStatesUsingCountryISO(ccpCountry.getSelectedCountryNameCode());
                                adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_expandable_list_item_1, statesArray);
                                listOfSearchGovs.setAdapter(adapter);
                            }
                        }
                );

        ccpCountry.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                location = loadCountryUsingItsISO(ccpCountry.getSelectedCountryNameCode());
                statesArray = loadStatesUsingCountryISO(ccpCountry.getSelectedCountryNameCode());
                adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_expandable_list_item_1, statesArray);
                listOfSearchGovs.setAdapter(adapter);
            }
        });

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                adapter.notifyDataSetChanged();
                return false;
            }
        });

        listOfSearchGovs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                location = adapterView.getItemAtPosition(i).toString();
                intent.putExtra("SELECTED_LOCATION", location);
                setResult(Activity.RESULT_OK,intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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

    public ArrayList<String> loadStatesUsingCountryISO(String countryCode){
        try {
            JSONObject obj = new JSONObject(loadCountryStateJSONFromAsset());
            JSONArray countries_arr = obj.getJSONArray("countries");

            Map<String, ArrayList<String>> countries_states = new HashMap<>();

            for (int i = 0; i < countries_arr.length(); i++) {
                JSONObject jo_inside = countries_arr.getJSONObject(i);
                JSONArray json_states = jo_inside.getJSONArray("states");
                String iso2 = jo_inside.getString("iso2");

                //load states
                ArrayList<String> states = new ArrayList<>();
                for(int j = 0; j < json_states.length(); j++)
                    states.add(json_states.getString(j));

                countries_states.put(iso2,states);
            }

            return countries_states.get(countryCode);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


}
