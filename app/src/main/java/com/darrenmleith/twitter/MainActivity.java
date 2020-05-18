package com.darrenmleith.twitter;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private EditText _emailEditText;
    private EditText _passwordEditText;
    private FirebaseAuth _mAuth;
    private DatabaseReference _mDatabase;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _mDatabase = FirebaseDatabase.getInstance().getReference();
        setTitle("Twitter: Login");
        _emailEditText = findViewById(R.id.emailEditText);
        _passwordEditText = findViewById(R.id.passwordEditText);
        // Initialize Firebase Auth
        _mAuth = FirebaseAuth.getInstance();

        if (_mAuth.getCurrentUser() != null) {
            login();
        }
    }

    public void login() {
        //move us to the next activity
        startActivity(new Intent(this, TestActivity.class));
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
                                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    // add the user to Firebase real-time database to directory users > UID > email
                                                    _mDatabase.child("users").child(task.getResult().getUser().getUid()).child("email").setValue(email);
                                                    login();
                                                } else {
                                                    Toast.makeText(MainActivity.this, "Login failed. Try again", Toast.LENGTH_SHORT).show();
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
