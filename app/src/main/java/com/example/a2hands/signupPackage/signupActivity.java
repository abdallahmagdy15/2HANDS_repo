package com.example.a2hands.signupPackage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

//gitHub country code picker
import com.example.a2hands.R;
import com.example.a2hands.User;
import com.google.firebase.firestore.FieldValue;
import com.hbb20.CountryCodePicker;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class signupActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private EditText fName;
    private EditText lName;
    //    private Spinner countrySelect;
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
    //    private static final String VALID_PHONE = "^[+]?[0-9]{8,20}$";
    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";

    //firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Button signUp;

        fName = findViewById(R.id.signUpFirstName);
        lName = findViewById(R.id.signUpLastName);
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
//        phone = findViewById(R.id.phone);
        email = findViewById(R.id.signUpEmail);
        password = findViewById(R.id.signUpPassword);
        confirmPassword = findViewById(R.id.signUpConfirmPassword);
        signUp = findViewById(R.id.btnSignUp);
        //flipper
        viewFlipper = findViewById(R.id.view_flipper);
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);


        //changes the phone code when country changes
        ccpCountry.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                ccpCode.setCountryForNameCode(ccpCountry.getSelectedCountryNameCode());
            }
        });



        auth = FirebaseAuth.getInstance();


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
                    if(TextUtils.isEmpty(fName.getText().toString().trim())){
                        fName.setError("Enter your First Name");
                    } else if(TextUtils.isEmpty(lName.getText().toString().trim())){
                        lName.setError("Enter your Last Name");
                    } else if (birthDate.getText().toString().equals("Select Your Date Of Birth")){
                        Toast.makeText(signupActivity.this, "Select your date of birth", Toast.LENGTH_SHORT).show();
                    } else if (genderGroup.getCheckedRadioButtonId() == -1){
                        Toast.makeText(signupActivity.this, "Select Your Gender", Toast.LENGTH_SHORT).show();
                    }else {
                        btnBack.setVisibility(View.VISIBLE);
                        viewFlipper.showNext();
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


    //password validation
    public static boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();
    }



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

        User userData = new User(fName.getText().toString().trim(),
                lName.getText().toString().trim(),
                selectedRadioButton.isChecked(),
                new Date(),
                ccpCountry.getSelectedCountryName(),
                ccpCode.getFullNumber(),
                "",
                "",
                0,
                "",
                "",
                "",
                "",
                0
                );

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





}