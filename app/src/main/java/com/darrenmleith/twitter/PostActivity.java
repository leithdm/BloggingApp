package com.darrenmleith.twitter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Random;

public class PostActivity extends AppCompatActivity {

    private static final int MAX_LENGTH = 8;
    private ImageButton _selectImageButton;
    private static final int GALLERY_REQUEST = 1;
    private EditText _postTitle;
    private EditText _postDescription;
    private Button _submitButton;
    private Uri _imageURI = null;
    private StorageReference _mFireStorage;
    private ProgressBar _progressBar;
    private DatabaseReference _mFirebaseDatabase;
    private FirebaseAuth _mAuth;
    private DatabaseReference _mDatabaseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        _postTitle = findViewById(R.id.titleEditText);
        _postDescription = findViewById(R.id.descriptionEditText);
        _submitButton = findViewById(R.id.submitPostButton);
        _mFireStorage = FirebaseStorage.getInstance().getReference();
        _mFirebaseDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
        _progressBar = findViewById(R.id.progressBar);
        _progressBar.setVisibility(View.INVISIBLE);
        _mAuth = FirebaseAuth.getInstance();
        _mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("users").child(_mAuth.getCurrentUser().getUid());

        _selectImageButton = findViewById(R.id.imageSelectButton);
        _selectImageButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                } else {
                    getPhoto();
                }
            }
        });

        _submitButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                startPosting();
            }
        });
    }

    public void getPhoto() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
    }

    private void startPosting() {
        final String postTitle = _postTitle.getText().toString().trim();
        final String postDescription = _postDescription.getText().toString().trim();

        //post to database and also get the download URL using a Task
        if (!TextUtils.isEmpty(postTitle) && (!TextUtils.isEmpty(postDescription) && _imageURI != null)) {
            _progressBar.setVisibility(View.VISIBLE);
            final StorageReference filePath = _mFireStorage.child("Blog_Images").child(_imageURI.getLastPathSegment());
            //store the image to firebase Storage using a "putFile"
            filePath.putFile(_imageURI).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    //We use a continueWithTask in order to get the download URL. We need the download URL
                    //when we "putFile" into firebase Storage so that we can also put a reference into the Database for downloading later
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    _progressBar.setVisibility(View.INVISIBLE);
                    if (task.isSuccessful()) {
                        final Uri downloadUri = task.getResult(); //get the URL of the image
                        final DatabaseReference newPost = _mFirebaseDatabase.push();

                        //we will use a valueEventListenet to get the email of the logged in user as per _mDatabaseUser reference location.
                        //Proper architecture would have this as the username, not the email
                        _mDatabaseUser.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                //post information to the database
                                newPost.child("title").setValue(postTitle);
                                newPost.child("description").setValue(postDescription);
                                newPost.child("imageURL").setValue(downloadUri.toString());
                                newPost.child("uid").setValue(_mAuth.getCurrentUser().getUid());
                                newPost.child("email").setValue(dataSnapshot.child("email").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        startActivity(new Intent(PostActivity.this, BlogActivity.class));
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    } else {
                        Toast.makeText(PostActivity.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


    }

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < randomLength; i++) {
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            _imageURI = data.getData();
            Log.i("Image URI", _imageURI.toString());
            _selectImageButton.setImageURI(_imageURI);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GALLERY_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getPhoto();
            }
        }
    }
}
