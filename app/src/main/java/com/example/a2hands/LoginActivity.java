package com.example.a2hands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a2hands.home.HomeActivity;
import com.example.a2hands.signup.SignUpActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;


public class LoginActivity extends AppCompatActivity {

    private EditText etloginEmail;
    private EditText etloginPassword;
    private Button login;

    private FirebaseAuth auth;

//    //////google
//    private GoogleSignInClient mGoogleSignInClient;
//    private SignInButton google_SignInButton;
//    static final int GOOGLE_SIGN_IN = 123;

    //footer
    private TextView goToSignUp;
    private TextView goToForgotPass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        ChangeLocale.loadLocale(getBaseContext());
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
                    etloginEmail.setError(getResources().getString(R.string.enterYourEmail));
                } else if(! Patterns.EMAIL_ADDRESS.matcher(loginEmail).matches()){
                    etloginEmail.setError(getResources().getString(R.string.emailIsNotValid));
                } else if(TextUtils.isEmpty(loginPassword)){
                    etloginPassword.setError(getResources().getString(R.string.enterYourPassword));
                }else{
                    loginUser(loginEmail , loginPassword);
                }
            }
        });



////////////sign in by google account ///////////////////////////////////////////
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();
//
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
//
//        google_SignInButton = findViewById(R.id.google_signin_button);
//        google_SignInButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//                startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
//            }
//        });



        //////////////Footer
        goToSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this , SignUpActivity.class));
                finish();
            }
        });
        goToForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this , ForgetPasswordActivity.class));
                finish();
            }
        });

    }//end of onCreate method


    ///////////////////////////////if the user is already signed in
    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null)
        if (auth.getCurrentUser() != null && auth.getCurrentUser().isEmailVerified()){
            startActivity(new Intent(LoginActivity.this , HomeActivity.class));
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

////////////////////////////////////////////////
////////////////////// google //////////////////
////////////////////////////////////////////////
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == GOOGLE_SIGN_IN) {
//            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            try {
//                GoogleSignInAccount account = task.getResult(ApiException.class);
//                if (account != null) firebaseAuthWithGoogle(account);
//            } catch (ApiException e) {
//                Log.w("TAG", "Google sign in failed", e);
//            }
//        }
//    }
//
//    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
//
//        Log.d("TAG", "firebaseAuthWithGoogle:" + acct.getId());
//
//        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
//        auth.signInWithCredential(credential)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
//                            startActivity(new Intent(LoginActivity.this , homeActivity.class));
//                            finish();
//
//                            //FirebaseUser user = auth.getCurrentUser();
//                            //updateUI(user);
//                        } else {
//                            // If sign in fails, display a message to the user.
//
//                            Toast.makeText(LoginActivity.this, "Error Signing In",
//                                    Toast.LENGTH_SHORT).show();
//
//                            //updateUI(null);
//                        }
//
//                    }
//                });
//    }




    // email and password
    private void loginUser( String txt_email,  String txt_password) {
        auth.signInWithEmailAndPassword(txt_email, txt_password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if(auth.getCurrentUser().isEmailVerified()){
                                startActivity(new Intent(LoginActivity.this , HomeActivity.class));
                                finish();

                            }else{
                                Toast.makeText(LoginActivity.this, getResources().getString(R.string.checkYourEmailForVerification),
                                        Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidUserException e) {
                                etloginEmail.setError(getResources().getString(R.string.emailIsNotValid));
                                etloginEmail.requestFocus();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                etloginPassword.setError(getResources().getString(R.string.wrongPassword));
                                etloginPassword.requestFocus();
                            } catch (FirebaseNetworkException e) {
                                Toast.makeText(LoginActivity.this, getResources().getString(R.string.noNetwork), Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(LoginActivity.this, getResources().getString(R.string.errorSigningIn),
                                        Toast.LENGTH_SHORT).show();
                            }

                        }

                    }
                });
    }


    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }


}