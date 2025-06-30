package com.example.bdapps.SearchPost;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.bdapps.R;

public class PostDetailActivity extends AppCompatActivity {
    private TextView contentTextView;
    private TextView usernameTextView;
    private TextView timeTextView;
    private TextView postIdTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        contentTextView = findViewById(R.id.detailContentTextView);
        usernameTextView = findViewById(R.id.detailUsernameTextView);
        timeTextView = findViewById(R.id.detailTimeTextView);
        postIdTextView = findViewById(R.id.detailPostIdTextView);
        loadPostData();
    }
    private void loadPostData() {
        // Get data from intent
        Integer postId = getIntent().getIntExtra("post_id", -1);
        String content = getIntent().getStringExtra("post_content");
        String username = getIntent().getStringExtra("post_username");
        String time = getIntent().getStringExtra("post_time");

        // Set data to views
        if (postId != -1) {
            postIdTextView.setText("Post ID: " + postId);
        } else {
            postIdTextView.setText("Post ID: N/A");
        }

        contentTextView.setText(content != null ? content : "No content");
        usernameTextView.setText("By: " + (username != null ? username : "Unknown"));
        timeTextView.setText(time != null ? time : "Unknown time");

        // Set title for the activity
        setTitle("Post Details");

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}