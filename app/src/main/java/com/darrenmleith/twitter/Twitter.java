package com.darrenmleith.twitter;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class Twitter extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
