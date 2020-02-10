package com.example.signuplogintask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


public class LoginActivity extends AppCompatActivity {

    private EditText etloginEmail;
    private EditText etloginPassword;
    private Button login;

    private FirebaseAuth auth;

    //////
    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton google_SignInButton;
    static final int GOOGLE_SIGN_IN = 123;

    //footer
    private TextView goToSignUp;
    private TextView goToForgotPass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Sign in with email and password//////////////////////////////////////
        etloginEmail = findViewById(R.id.loginEmail);
        etloginPassword = findViewById(R.id.loginPassword);
        login = findViewById(R.id.btnLogin);


        //footer///////////////////////////////////////////////////////////////
        goToSignUp =findViewById(R.id.signUpLink);
        goToForgotPass= findViewById(R.id.forgotPassLink);


        auth = FirebaseAuth.getInstance();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String loginEmail = etloginEmail.getText().toString();
                String loginPassword = etloginPassword.getText().toString();
                if (TextUtils.isEmpty(loginEmail)){
                    etloginEmail.setError("Enter your Email");
                    //Toast.makeText(RegisterActivity.this, "Empty Email or Password!", Toast.LENGTH_SHORT).show();
                } else if(! Patterns.EMAIL_ADDRESS.matcher(loginEmail).matches()){
                    etloginEmail.setError("Email is not valid");
                } else if(TextUtils.isEmpty(loginPassword)){
                    etloginPassword.setError("Enter your Password");
                }else{
                    loginUser(loginEmail , loginPassword);
                }
            }
        });



        //sign in by google account ///////////////////////////////////////////
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        google_SignInButton = findViewById(R.id.google_signin_button);
        google_SignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
            }
        });




        //////////////Footer
        goToSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this , RegisterActivity.class));
                finish();
            }
        });
        goToForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this , ForgotpassActivity.class));
                finish();
            }
        });

    }


    ///////////////////////////////if the user is already signed in
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null)
        if (auth.getCurrentUser() != null && auth.getCurrentUser().isEmailVerified()){
            startActivity(new Intent(LoginActivity.this , HomeActivity.class));
            finish();
        }

        //updateUI(currentUser);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().signOut();
    }

    //// google
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w("TAG", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        Log.d("TAG", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this , HomeActivity.class));
                            finish();

                            //FirebaseUser user = auth.getCurrentUser();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.

                            Toast.makeText(LoginActivity.this, "Error Signing In",
                                    Toast.LENGTH_SHORT).show();

                            //updateUI(null);
                        }

                    }
                });
    }




    // email and password
    private void loginUser( String txt_email,  String txt_password) {
        auth.signInWithEmailAndPassword(txt_email, txt_password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            if(auth.getCurrentUser().isEmailVerified()){
                                //Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this , HomeActivity.class));
                                finish();
                                // Sign in success, update UI with the signed-in user's information
                                //FirebaseUser user = auth.getCurrentUser();
                                //updateUI(user);
                            }else{
                                Toast.makeText(LoginActivity.this, "Check your email for verification",
                                        Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidUserException e) {
                                etloginEmail.setError("Invalid Email Address");
                                etloginEmail.requestFocus();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                etloginPassword.setError("Wrong Password");
                                etloginPassword.requestFocus();
                            } catch (FirebaseNetworkException e) {
                                Toast.makeText(LoginActivity.this, "No Network!", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(LoginActivity.this, "Error Signing In",
                                        Toast.LENGTH_SHORT).show();
                            }

                            //updateUI(null);
                        }

                    }
                });

    }

//        private void updateUI(FirebaseUser user) {
//            hideProgressBar();
//            if (user != null) {
//                mStatusTextView.setText(getString(R.string.emailpassword_status_fmt,
//                        user.getEmail(), user.isEmailVerified()));
//                mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));
//
//                findViewById(R.id.emailPasswordButtons).setVisibility(View.GONE);
//                findViewById(R.id.emailPasswordFields).setVisibility(View.GONE);
//                findViewById(R.id.signedInButtons).setVisibility(View.VISIBLE);
//
//                findViewById(R.id.verifyEmailButton).setEnabled(!user.isEmailVerified());
//            } else {
//                mStatusTextView.setText(R.string.signed_out);
//                mDetailTextView.setText(null);
//
//                findViewById(R.id.emailPasswordButtons).setVisibility(View.VISIBLE);
//                findViewById(R.id.emailPasswordFields).setVisibility(View.VISIBLE);
//                findViewById(R.id.signedInButtons).setVisibility(View.GONE);
//            }
//        }


}
