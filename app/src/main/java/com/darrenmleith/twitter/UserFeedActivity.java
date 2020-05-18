package com.darrenmleith.twitter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class UserFeedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_feed);
        setTitle("Your Feed");


    }
}
