package com.darrenmleith.twitter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class BlogActivity extends AppCompatActivity {

    private DatabaseReference _mDatabase;
    private DatabaseReference _mDatabaseLike;
    private DatabaseReference _mDatabaseUsers;
    private RecyclerView _blogRecycler;
    private FirebaseAuth _mAuth;
    private boolean blogLike = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        _blogRecycler = findViewById(R.id.blogRecyclerView);
        _blogRecycler.setHasFixedSize(true);
        _blogRecycler.setLayoutManager(new LinearLayoutManager(this));

        _mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
        _mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        _mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        _mAuth = FirebaseAuth.getInstance();

        //TODO: this is new code. Need to investigate exactly what this does
        _mDatabase.keepSynced(true);
        _mDatabaseLike.keepSynced(true);
        _mDatabaseUsers.keepSynced(true);
    }

    //fire up the onStart method to set the FirebaseRecyclerAdapter that is tied to a custom object representing a "Blog" and a RecyclerView.ViewHolder object
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Blog, BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(
                Blog.class, //custom class, built entirely with getters/setters and a default constructor public Blog()
                R.layout.blog_row, //custom CardView giving us greater control over what we are presenting. This is the ONLY place we reference this CardView !
                BlogViewHolder.class, //custom RecyclerView.ViewHolder
                _mDatabase //this all works because the Blog.class follows the same format as what we have stored in this reference in the database
        ) {
            @Override
            protected void populateViewHolder(BlogViewHolder blogViewHolder, Blog blog, int i) {

                //using getRef(i) method of firebase to get the entire address of the Blog within firebase database. This is pretty cool and powerful
                final String postKey = getRef(i).getKey();

                blogViewHolder.setTitle(blog.getTitle());
                blogViewHolder.setDescription(blog.getDescription());
                blogViewHolder.setImage(getApplicationContext(), blog.getImageURL());
                blogViewHolder.setEmail(blog.getEmail());
                blogViewHolder.setLikeButton(postKey);

                //[START set on-click listener for the RecycleView so we can click on an item in the Recycle View
                blogViewHolder.blogView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(BlogActivity.this, postKey, Toast.LENGTH_SHORT).show();
                    }
                });

                //[START set on-click listener for the Like Button
                blogViewHolder.likeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(BlogActivity.this, "selected like button", Toast.LENGTH_SHORT).show();
                        blogLike = true;

                        _mDatabaseLike.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (blogLike) {
                                    //check to see if the like exists already. If already liked we want to delete it
                                    if (dataSnapshot.child(postKey).hasChild(_mAuth.getCurrentUser().getUid())) {
                                        _mDatabaseLike.child(postKey).child(_mAuth.getCurrentUser().getUid()).removeValue();
                                        blogLike = false;
                                    } else { //otherwise we want to like the post
                                        _mDatabaseLike.child(postKey).child(_mAuth.getCurrentUser().getUid()).setValue("RandomValue");
                                        blogLike = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                });
            }
        };
        _blogRecycler.setAdapter(firebaseRecyclerAdapter);
    }


    //need to extend RecyclerView.ViewHolder with a custom class that is tied to the FirebaseRecyclerAdapter
    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        //create a View object and assign it the value of itemView
        private View blogView;
        private TextView postTitle;
        private ImageButton likeButton;
        private DatabaseReference _mDatabaseLike;
        private FirebaseAuth _mAuth;


        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);
            blogView = itemView;
            likeButton = blogView.findViewById(R.id.likeButton);
            _mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
            _mAuth = FirebaseAuth.getInstance();
            _mDatabaseLike.keepSynced(true);

            postTitle = blogView.findViewById(R.id.postTitle);

            postTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("Title that was clicked", "Clicked a wee title");
                }
            });

        }

        //Set the like button image.
        //Duplication of code from above, but needed in order to change the image
        public void setLikeButton(final String postKey) {
            _mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(postKey).hasChild(_mAuth.getCurrentUser().getUid())) {
                        likeButton.setImageResource(R.drawable.like);
                    } else {
                        likeButton.setImageResource(R.drawable.dontlike );
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        //set the title text of postTitle within the CardView
        //2nd part to this is setting an onclick listener so can select the post
        public void setTitle(String title) {


            //TextView postTitle = blogView.findViewById(R.id.postTitle);
            postTitle.setText(title);
        }

        //set the title text of postTitle within the CardView
        public void setDescription(String description) {
            TextView postDescription = blogView.findViewById(R.id.postDescription);
            postDescription.setText(description);
        }

        //set the image that is within the CardView using Picasso
        public void setImage(Context context, String imageURL) {
            ImageView imageView = blogView.findViewById(R.id.postImage);
            Picasso.get().load(imageURL).into(imageView);
        }

        //set the username text
        public void setEmail(String userText) {
            TextView email = blogView.findViewById(R.id.usernameTextView);
            email.setText(userText);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add_post) {
            startActivity(new Intent(BlogActivity.this, PostActivity.class));
        } else if (item.getItemId() == R.id.logout) {
            signOut();
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        // Firebase sign out
        _mAuth.signOut();

        // Google sign out
        LoginActivity.mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(BlogActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                });
    }

    private void revokeAccess() {
        // Firebase sign out
        _mAuth.signOut();

        // Google revoke access
        LoginActivity.mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(BlogActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                });
    }
}
