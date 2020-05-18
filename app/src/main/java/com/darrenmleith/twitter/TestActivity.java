package com.darrenmleith.twitter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

public class TestActivity extends AppCompatActivity {

    private DatabaseReference _mDatabase;
    private RecyclerView _blogRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        _blogRecycler = findViewById(R.id.blogRecyclerView);
        _blogRecycler.setHasFixedSize(true);
        _blogRecycler.setLayoutManager(new LinearLayoutManager(this));
        _mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Blog, BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(
                Blog.class,
                R.layout.blog_row,
                BlogViewHolder.class,
                _mDatabase
        ) {
            @Override
            protected void populateViewHolder(BlogViewHolder blogViewHolder, Blog blog, int i) {
                blogViewHolder.setTitle(blog.getTitle());
                blogViewHolder.setDescription(blog.getDescription());
                blogViewHolder.setImage(getApplicationContext(), blog.getImageURL());
            }
        };
        _blogRecycler.setAdapter(firebaseRecyclerAdapter);

    }


    //need a ViewHolder when creating a RecyclerView
    public static class BlogViewHolder extends RecyclerView.ViewHolder {

        View blogView;

        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);
            blogView = itemView;
        }
        //set the title text of postTitle within the CardView

        public void setTitle(String title) {
            TextView postTitle = blogView.findViewById(R.id.postTitle);
            postTitle.setText(title);
        }
        //set the title text of postTitle within the CardView
        public void setDescription(String title) {
            TextView postDescription = blogView.findViewById(R.id.postDescription);
            postDescription.setText(title);
        }

        //set the image that is within the CardView using Picasso
        public void setImage(Context context, String imageURL) {
            ImageView imageView = blogView.findViewById(R.id.postImage);
            Picasso.get().load(imageURL).into(imageView);
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
        startActivity(new Intent(TestActivity.this, PostActivity.class));
        } else {

        }
        return super.onOptionsItemSelected(item);
    }
}
