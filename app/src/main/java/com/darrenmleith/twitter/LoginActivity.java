package com.darrenmleith.twitter;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private EditText _emailEditText;
    private EditText _passwordEditText;

    // [START declare_auth]
    private FirebaseAuth _mAuth;
    // [END declare_auth]

    private DatabaseReference _mDatabase;
    private SignInButton _googleSignInButton;
    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;
    public static GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onStart() {
        super.onStart();

        if (_mAuth.getCurrentUser() != null) {
            login();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Blogging App: Login");

        // [START config_signin]
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]


        // [START initialize_auth]
        _mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        _googleSignInButton = findViewById(R.id.sign_in_button);

        //[START initialize database and keep-synced for off-line
        _mDatabase = FirebaseDatabase.getInstance().getReference();
        _mDatabase.keepSynced(true);
        //[END initialize database and keep-synced for off-line

        _emailEditText = findViewById(R.id.emailEditText);
        _passwordEditText = findViewById(R.id.passwordEditText);


        _googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    // [START signin]
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signin]

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // Google Sign In was successful, authenticate with Firebase
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                //updateUI(null);
            }
        }
    }

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        _mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = _mAuth.getCurrentUser();
                            login();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            //Snackbar.make(mBinding.mainLayout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                        //hideProgressBar();
                    }
                });
    }
    // [END auth_with_google]


    public void login() {
        //move us to the next activity
        startActivity(new Intent(this, BlogActivity.class));
    }


    public void goClicked(View view) {
        //check if we can log in the user

        final String email = _emailEditText.getText().toString();
        final String password = _passwordEditText.getText().toString();

        if (!(TextUtils.isEmpty(email) || TextUtils.isEmpty(password))) {
            _mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                login();
                            } else {
                                //sign-up the user to Firebase Authentication
                                _mAuth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    // add the user to Firebase real-time database to directory users > UID > email
                                                    _mDatabase.child("users").child(task.getResult().getUser().getUid()).child("email").setValue(email);
                                                    login();
                                                } else {
                                                    Toast.makeText(LoginActivity.this, "Login failed. Try again", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "Email and password must not be empty", Toast.LENGTH_SHORT).show();
        }
    }
}
