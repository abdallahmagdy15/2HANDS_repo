package com.example.a2hands.signup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

//gitHub country code picker
import com.example.a2hands.LoginActivity;
import com.example.a2hands.R;
import com.example.a2hands.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.hbb20.CountryCodePicker;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class signupActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private EditText fullName;
    private EditText userName;
    //    private Spinner countrySelect;
    private Spinner stateSelect;
    private RadioGroup genderGroup;
    //    private EditText phone;
    private EditText email;
    private EditText confirmPassword;
    private EditText password;
    private TextView birthDate;
    private CountryCodePicker ccpCountry;
    private CountryCodePicker ccpCode;
    private EditText editTextCarrierNumber;

    private ViewFlipper viewFlipper;
    private Button btnBack;
    private Button btnNext;

    //default date in the date dialog
    private static final int DEFAULT_DAY = 1;
    private static final int DEFAULT_MONTH = 0;
    private static final int DEFAULT_YEAR = 1990;

    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
    private static final String USERNAME_PATTERN = "^(?=.{8,20}$)(?![_.])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])$";

    //firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();
        signingInAdmin();


        Button signUp;
        fullName = findViewById(R.id.signUpFullName);
        userName = findViewById(R.id.signUpUsername);
        genderGroup = findViewById(R.id.radioGroup);
        birthDate = findViewById(R.id.show_dialog);
        //country selection
        ccpCountry = findViewById(R.id.ccpCountry);
        ccpCountry.setAutoDetectedCountry(true);
        //code and phone
        ccpCode = findViewById(R.id.ccpCode);
        editTextCarrierNumber = findViewById(R.id.editText_carrierNumber);
        ccpCode.registerCarrierNumberEditText(editTextCarrierNumber);
//        countrySelect = findViewById(R.id.countrySpinner);
        stateSelect = findViewById(R.id.statesSpinner);
//        phone = findViewById(R.id.phone);
        email = findViewById(R.id.signUpEmail);
        password = findViewById(R.id.signUpPassword);
        confirmPassword = findViewById(R.id.signUpConfirmPassword);
        signUp = findViewById(R.id.btnSignUp);
        //flipper
        viewFlipper = findViewById(R.id.view_flipper);
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);

        //changes the phone code and update states spinner when country changes
        setUpStatesSpinner(ccpCountry.getSelectedCountryNameCode());
        ccpCountry.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                ccpCode.setCountryForNameCode(ccpCountry.getSelectedCountryNameCode());
                //load spinners items
                setUpStatesSpinner(ccpCountry.getSelectedCountryNameCode());
            }
        });

        birthDate.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

//        ordinary country spinner initiation
//        Locale[] locale = Locale.getAvailableLocales();
//        ArrayList<String> countries = new ArrayList<>();
//        String country;
//        for( Locale loc : locale ){
//            country = loc.getDisplayCountry();
//            if( country.length() > 0 && !countries.contains(country) ){
//                countries.add( country );
//            }
//        }
//        Collections.sort(countries, String.CASE_INSENSITIVE_ORDER);
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item, countries);
//        countrySelect.setAdapter(adapter);
//        //makes Egypt the default country
//        ArrayAdapter<String> spinnerAdapter = (ArrayAdapter<String>) countrySelect.getAdapter();
//        int spinnerPosition = spinnerAdapter.getPosition("Egypt");
//        countrySelect.setSelection(spinnerPosition);



        ////////////////////Sign up button
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt_email = email.getText().toString().trim();
                String txt_password = password.getText().toString();

                if (TextUtils.isEmpty(txt_email)){
                    email.setError("Enter your Email");
                    //Toast.makeText(RegisterActivity.this, "Empty Email or Password!", Toast.LENGTH_SHORT).show();
                } else if(! Patterns.EMAIL_ADDRESS.matcher(txt_email).matches()){
                    email.setError("Email is not valid");
                } else if(password.getText().toString().length() < 8 ){
                    if(TextUtils.isEmpty(txt_password)){
                        password.setError("Enter your Password");
                    }else{
                        Toast.makeText(signupActivity.this, "Too short password", Toast.LENGTH_SHORT).show();
                    }
                } else if(TextUtils.isEmpty(confirmPassword.getText().toString())){
                    confirmPassword.setError("Enter your Confirm Password");
                } else if(! confirmPassword.getText().toString().equals(password.getText().toString())){
                    confirmPassword.setError("Password doesn't match");
                } else if (! isValidPassword(confirmPassword.getText().toString())){
                    Toast.makeText(signupActivity.this, "Please add at least 1 Alphabet," + "\n" +" 1 Number and 1 Special Character", Toast.LENGTH_LONG).show();
                }else{
                    signUpUser(txt_email , txt_password);
                }
            }
        });//end of signUp.setOnClickListener



        //the ViewFlipper Next and Previous buttons
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnNext.setVisibility(View.VISIBLE);
                if (viewFlipper.getDisplayedChild()==1){
                    v.setVisibility(View.INVISIBLE);
                }
                if (viewFlipper.getDisplayedChild()==0) {
                    viewFlipper.stopFlipping();
                } else {
                    viewFlipper.setInAnimation(signupActivity.this, android.R.anim.slide_in_left);
                    viewFlipper.setOutAnimation(signupActivity.this, android.R.anim.slide_out_right);

                    viewFlipper.showPrevious();
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                viewFlipper.setInAnimation(signupActivity.this, R.anim.slide_in_right);
                viewFlipper.setOutAnimation(signupActivity.this, R.anim.slide_out_left);

                if(viewFlipper.getDisplayedChild()==0){
                    if(TextUtils.isEmpty(fullName.getText().toString().trim())){
                        fullName.setError("Enter your Full Name");
                    } else if(TextUtils.isEmpty(userName.getText().toString().trim())){
                        userName.setError("Enter your Username");
                    } else if (birthDate.getText().toString().equals("Select Your Date Of Birth")){
                        Toast.makeText(signupActivity.this, "Select your date of birth", Toast.LENGTH_SHORT).show();
                    } else if (genderGroup.getCheckedRadioButtonId() == -1){
                        Toast.makeText(signupActivity.this, "Select Your Gender", Toast.LENGTH_SHORT).show();
                    }else {
                        checkIfValidUsername(userName.getText().toString().trim());
                    }
                } else if (viewFlipper.getDisplayedChild()==1){
                    if(TextUtils.isEmpty(editTextCarrierNumber.getText().toString().trim())){
                        editTextCarrierNumber.setError("Enter your Phone");
                    }
                    else if(! ccpCode.isValidFullNumber()) {
                        editTextCarrierNumber.setError("Phone is not valid");
                    }else {
                        v.setVisibility(View.INVISIBLE);
                        viewFlipper.showNext();
                    }
                } else if (viewFlipper.getDisplayedChild() == 2) {
                    viewFlipper.stopFlipping();
                }
            }
        });




    }//end of onCreate



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

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item, countries_states.get(selectedCountry));
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


    //password validation
    public boolean isValidPassword(String password) {
        Pattern pattern;
        Matcher matcher;
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();
    }

    //username validation
    public void checkIfValidUsername(final String username){

        final Query usernameQuery = FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("user_name", username);

        Pattern usernamePattern;
        Matcher usernameMatcher;
        usernamePattern = Pattern.compile(USERNAME_PATTERN);
        usernameMatcher = usernamePattern.matcher(username);

        if(usernameMatcher.matches()){
            usernameQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()){
                        for (DocumentSnapshot ds: task.getResult()){
                            String userNames = ds.getString("user_name");
                            if (username.equals(userNames)) {
                                userName.setError("Username is already used");
                            }
                        }
                    }
                    if (task.getResult().size() == 0){
                        viewFlipper.showNext();
                        btnBack.setVisibility(View.VISIBLE);
                    }
                }
            });
        } else {
            userName.setError("Username is not valid");
        }

    }// end of checkIfValidUsername



    ////////Date of birth
    final Calendar c = Calendar.getInstance();
    private int dayNow = c.get(Calendar.DAY_OF_MONTH);
    private int monthNow = c.get(Calendar.MONTH);
    private int yearNow= c.get(Calendar.YEAR);

    public void showDatePickerDialog(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, this, DEFAULT_YEAR, DEFAULT_MONTH, DEFAULT_DAY);
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        if (year > yearNow || year >= yearNow &&  month > monthNow || year >= yearNow &&  month >= monthNow && dayOfMonth > dayNow) {
            Toast.makeText(signupActivity.this, "Date of Birth can't be from the future!", Toast.LENGTH_LONG).show();
        }
        month += 1;
        String date =  dayOfMonth + "/" + month + "/" + year;
        birthDate.setText(date);

    }




    ///////Signing up
    private void signUpUser(String email, String password) {
        signingOutAdmin();
        auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                saveUserData(auth.getCurrentUser().getUid());
                auth.getCurrentUser().sendEmailVerification()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isComplete()){
                                    Toast.makeText(signupActivity.this, "Registered Successfully!" + "\n"+ "Check your verification email", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(signupActivity.this , signupPickPictureActivity.class));
                                    finish();
                                }else {
                                    Toast.makeText(signupActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(signupActivity.this, "Email already in use!", Toast.LENGTH_LONG).show();
            }
        });
    }//end of signUpUser




    private void saveUserData(String userID) {
        int genderID = genderGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(genderID);

        User userData = new User(
                userID,
                fullName.getText().toString().trim(),
                userName.getText().toString().trim(),
                selectedRadioButton.isChecked(),
                new Date(),
                ccpCountry.getSelectedCountryNameCode(),
                stateSelect.getSelectedItem().toString(),
                ccpCode.getFullNumber(),
                0.0,
                "",
                "",
                "",
                "",
                0,
                "online",
                "noOne");

        Map<String,Object> registerDate = new HashMap<>();
        registerDate.put("register_date", FieldValue.serverTimestamp());


        db.collection("users").document(userID).set(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("saveUserData", "Done");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("saveUserData", e.toString());
                    }
                });

        db.collection("users").document(userID).update(registerDate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("saveRegisterDate", "Done");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("saveRegisterDate", e.toString());
                    }
                });
    }//end of saveUserData

    private void signingInAdmin(){
        auth.signInWithEmailAndPassword("admin@admin.com","123456");
    }
    private void signingOutAdmin(){
        auth.signOut();
    }

    @Override
    public void onBackPressed() {
        if(viewFlipper.getDisplayedChild()==0){
            startActivity(new Intent(signupActivity.this, LoginActivity.class));
        }else if(viewFlipper.getDisplayedChild()==1){
            btnBack.setVisibility(View.INVISIBLE);
            viewFlipper.showPrevious();
        }else if(viewFlipper.getDisplayedChild()==2){
            btnNext.setVisibility(View.VISIBLE);
            viewFlipper.showPrevious();
        }
    }


    //for changing app language
    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);

        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        //save the data to shared preferences
        SharedPreferences.Editor editor = getSharedPreferences("settings", MODE_PRIVATE).edit();
        editor.putString("My_Language", lang);
        editor.apply();
    }

    public void loadLocale (){
        SharedPreferences prefs = getSharedPreferences("settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Language", "");
        setLocale(language);
    }

}