package com.darrenmleith.twitter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserListActivity extends AppCompatActivity {

    private FirebaseAuth _mAuth;
    private ArrayList<String> _emails;
    private ArrayList<String> _followingArray;
    private DatabaseReference _mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);


        _mDatabase = FirebaseDatabase.getInstance().getReference();
        _mAuth = FirebaseAuth.getInstance();
        _emails = new ArrayList<>();
        _followingArray = new ArrayList<>();
        setTitle(_mAuth.getCurrentUser().getEmail() + " " + _mAuth.getCurrentUser().getUid());

        final ListView _usersListView = findViewById(R.id.listView);
        _usersListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_checked, _emails);
        _usersListView.setAdapter(arrayAdapter);


        //TODO: Read who the logged in user is Following from the database
        _mDatabase.child("followers").child(_mAuth.getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.i("INITIAL READ or CHANGE", dataSnapshot.child("email").getValue().toString());
                _followingArray.add(dataSnapshot.child("email").getValue().toString());
                Log.i("Itemsin _followingArray", _followingArray.toString());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });


        //give the UI a chance to download
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Display a list of all users in listview
        _mDatabase.child("users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                _emails.add((String) dataSnapshot.child("email").getValue());
                arrayAdapter.notifyDataSetChanged();

                //determines which user should be checked as "following" based on database
                for (String user: _emails) {
                    if (_followingArray.contains(user)) {
                        _usersListView.setItemChecked(_emails.indexOf(user), true);
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });


        //TODO: Clicking on a user determines whether you are following them or not
        _usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView checkedTextView = (CheckedTextView) view;
                if (checkedTextView.isChecked()) {
                    Log.i("Info", "Checked");
                    if (!_followingArray.contains(_emails.get(position))) {
                        _mDatabase.child("followers").child(_mAuth.getCurrentUser().getUid()).push().child("email").setValue(_emails.get(position));
                    }
                } else {
                    Log.i("Info", "Not checked");
                    //TODO: write code to remove a follower
                    DatabaseReference test = _mDatabase.child("followers").child(_mAuth.getCurrentUser().getUid());
                    test.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String email = dataSnapshot.getValue(String.class);
                            System.out.println("!!!!Email is " + email);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    /*
                    if (_followingArray.contains(_emails.get(position))) {
                        _followingArray.remove(_emails.get(position));
                    }

                     */
                }
            }
        });
    }

    //TODO: replace all code below with code to send a message to server
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    /*
    //Create Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.share_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }



    //Menu item with options to 1. Tweet a message 2. Go to Twit Feed 3. Logout
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.tweet) {
            //TODO: bring up message dialog box

        } else if (item.getItemId() == R.id.logout) {
            _mAuth.signOut();
            finish();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.twitterFeed) {
            //TODO: shoot over to the Twit Feed Activity
            Intent intent = new Intent(getApplicationContext(), UserFeedActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

     */

    //pop up message dialog box
    public void getPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }
}
