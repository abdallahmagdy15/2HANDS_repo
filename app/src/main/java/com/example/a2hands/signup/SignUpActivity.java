package com.example.a2hands.signup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.example.a2hands.ChangeLocale;
import com.example.a2hands.LoginActivity;
import com.example.a2hands.R;
import com.example.a2hands.User;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
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
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TextWatcher {

    private EditText fullName;
    private EditText userName;
    //private Spinner countrySelect;
    private Spinner stateSelect;
    private RadioGroup genderGroup;
    //private EditText phone;
    private EditText email;
    private EditText confirmPassword;
    private EditText password;
    private TextView birthDateText;
    private CountryCodePicker ccpCountry;
    private CountryCodePicker ccpCode;
    private EditText editTextCarrierNumber;

    private TextInputLayout textInputLayout;

    private ViewFlipper viewFlipper;
    private Button btnBack;
    private Button btnNext;
    private Button signUp;

    //default date in the date dialog
    private static final int DEFAULT_DAY = 1;
    private static final int DEFAULT_MONTH = 0;
    private static final int DEFAULT_YEAR = 1990;
    Calendar combinedCal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));

    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
    private static final String USERNAME_PATTERN = "^(?=.{8,20}$)(?![_.])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])$";

    //firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeLocale.loadLocale(getBaseContext());
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();
        signingInAdmin();

        textInputLayout = findViewById(R.id.signUpFullName);
        fullName = findViewById(R.id.signUpFullNameText);
        userName = findViewById(R.id.signUpUsernameText);
        genderGroup = findViewById(R.id.radioGroup);
        birthDateText = findViewById(R.id.signUpBirthDayEditText);

        //country selection
        ccpCountry = findViewById(R.id.ccpCountry);
        ccpCountry.setAutoDetectedCountry(true);

        //code and phone
        ccpCode = findViewById(R.id.ccpCode);
        editTextCarrierNumber = findViewById(R.id.editText_carrierNumber);
        ccpCode.registerCarrierNumberEditText(editTextCarrierNumber);
        stateSelect = findViewById(R.id.statesSpinner);
        email = findViewById(R.id.signUpEmail);
        password = findViewById(R.id.signUpPassword);
        confirmPassword = findViewById(R.id.signUpConfirmPassword);
        signUp = findViewById(R.id.btnSignUp);


        //set onTextChangeListener to all EditTexts
        fullName.addTextChangedListener(this);
        userName.addTextChangedListener(this);
        email.addTextChangedListener(this);
        password.addTextChangedListener(this);
        confirmPassword.addTextChangedListener(this);

        //flipper
        viewFlipper = findViewById(R.id.view_flipper);
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);

        String html = getResources().getString(R.string.alreadyHaveAnAccount) + " " +
                "<b>"+ getResources().getString(R.string.login) +"</b>";
        Spanned result = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY);
        TextView alreadyHaveAccTV = findViewById(R.id.signUpAlreadyHaveAccount);
        alreadyHaveAccTV.setText(result);
        alreadyHaveAccTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            }
        });

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

        birthDateText.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        //setUp form onClickListeners with form validations
        setUpFormValidation();

    }/////////////////////end of onCreate



    private void setUpFormValidation() {
        ////////////////////Sign up button
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt_email = email.getText().toString().trim();
                String txt_password = password.getText().toString();

                if (TextUtils.isEmpty(txt_email)){
                    textInputLayout = findViewById(R.id.signUpEmailLayout);
                    textInputLayout.setError(getResources().getString(R.string.enterYourEmail));
                } else if(! Patterns.EMAIL_ADDRESS.matcher(txt_email).matches()){
                    textInputLayout = findViewById(R.id.signUpEmailLayout);
                    textInputLayout.setError(getResources().getString(R.string.emailIsNotValid));
                } else if(password.getText().toString().length() < 8 ){
                    textInputLayout = findViewById(R.id.signUpPasswordLayout);
                    if(TextUtils.isEmpty(txt_password))
                        textInputLayout.setError((getResources().getString(R.string.enterYourPassword)));
                    else
                        textInputLayout.setError(getResources().getString(R.string.tooShortPassword));
                } else if(TextUtils.isEmpty(confirmPassword.getText().toString())){
                    textInputLayout = findViewById(R.id.signUpConfirmPasswordLayout);
                    textInputLayout.setError(getResources().getString(R.string.enterYourConfirmPassword));
                } else if(! confirmPassword.getText().toString().equals(password.getText().toString())){
                    textInputLayout = findViewById(R.id.signUpConfirmPasswordLayout);
                    textInputLayout.setError(getResources().getString(R.string.passwordDoesnotMatch));
                } else if (! isValidPassword(password.getText().toString())){
                    textInputLayout = findViewById(R.id.signUpPasswordLayout);
                    textInputLayout.setError(getResources().getString(R.string.atLeast1Capital1Number1SpecialChar));
                }else{
                    signUpUser(txt_email , txt_password);
                }
            }
        });

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
                    viewFlipper.setInAnimation(SignUpActivity.this, android.R.anim.slide_in_left);
                    viewFlipper.setOutAnimation(SignUpActivity.this, android.R.anim.slide_out_right);

                    viewFlipper.showPrevious();
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFlipper.setInAnimation(SignUpActivity.this, R.anim.slide_in_right);
                viewFlipper.setOutAnimation(SignUpActivity.this, R.anim.slide_out_left);

                if(viewFlipper.getDisplayedChild()==0){
                    if(TextUtils.isEmpty(fullName.getText().toString().trim())){
                        textInputLayout = findViewById(R.id.signUpFullName);
                        textInputLayout.setError(getResources().getString(R.string.enterYourFullName));
                    } else if(TextUtils.isEmpty(userName.getText().toString().trim())){
                        textInputLayout = findViewById(R.id.signUpUsername);
                        textInputLayout.setError(getResources().getString(R.string.enterYourUserName));
                    } else if (birthDateText.getText().toString().equals(getResources().getString(R.string.selectYourDateOfBirth))){
                        textInputLayout = findViewById(R.id.show_dialog);
                        textInputLayout.setError(getResources().getString(R.string.selectYourDateOfBirth));
                    }else if (genderGroup.getCheckedRadioButtonId() == -1){
                        Toast.makeText(SignUpActivity.this, getResources().getString(R.string.selectYourGender), Toast.LENGTH_SHORT).show();
                    }  else {
                        checkIfValidUsername(userName.getText().toString().trim());
                    }
                } else if (viewFlipper.getDisplayedChild()==1){
                    if(TextUtils.isEmpty(editTextCarrierNumber.getText().toString().trim())){
                        editTextCarrierNumber.setError(getResources().getString(R.string.enterYourPhone));
                    }
                    else if(! ccpCode.isValidFullNumber()) {
                        editTextCarrierNumber.setError(getResources().getString(R.string.phoneIsNotValid));
                    }else {
                        v.setVisibility(View.INVISIBLE);
                        viewFlipper.showNext();
                    }
                } else if (viewFlipper.getDisplayedChild() == 2) {
                    viewFlipper.stopFlipping();
                }
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
                                textInputLayout = findViewById(R.id.signUpUsername);
                                textInputLayout.setError(getResources().getString(R.string.userNameIsAlreadyUsed));
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
            textInputLayout = findViewById(R.id.signUpUsername);
            textInputLayout.setError(getResources().getString(R.string.userNameIsNotValid));
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
            Toast.makeText(SignUpActivity.this, getResources().getString(R.string.dateOfBirthCanNotBeFromTheFuture), Toast.LENGTH_LONG).show();
        } else {
            combinedCal.set(year, month, dayOfMonth);

            month += 1;
            String date =  dayOfMonth + " / " + getMonth(month) + " / " + year;
            birthDateText.setText(date);
            textInputLayout.setError(null);
        }
    }
    public String getMonth(int month) {
        return new DateFormatSymbols().getMonths()[month-1];
    }


    ///////Signing up
    private void signUpUser(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                signingOutAdmin();
                saveUserData(auth.getCurrentUser().getUid());
                auth.getCurrentUser().sendEmailVerification()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isComplete()){
                                    startActivity(new Intent(SignUpActivity.this , SignUpPickPictureActivity.class));
                                    finish();
                                }else {
                                    Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                textInputLayout = findViewById(R.id.signUpEmailLayout);
                textInputLayout.setError(getResources().getString(R.string.emailAlreadyInUse));
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
                new Timestamp(new Date(combinedCal.getTimeInMillis())),
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

        db.collection("users").document(userID).set(userData);

        db.collection("users").document(userID).update(registerDate);
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
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
        }else if(viewFlipper.getDisplayedChild()==1){
            btnBack.setVisibility(View.INVISIBLE);
            viewFlipper.showPrevious();
        }else if(viewFlipper.getDisplayedChild()==2){
            btnNext.setVisibility(View.VISIBLE);
            viewFlipper.showPrevious();
        }else{
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
        }
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(count>0)
            textInputLayout.setError(null);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

}