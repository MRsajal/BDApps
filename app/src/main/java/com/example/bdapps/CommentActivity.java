package com.example.bdapps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentActivity extends AppCompatActivity {
    RecyclerView recyclerViewComments;
    EditText editTextComment;
    Button btnAddComment;
    CommentsAdapter commentsAdapter;
    List<Comment> commentsList;
    Integer postId;
    String currentUser="CurrentUser";
    String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        postId=getIntent().getIntExtra("POST_ID",-1);
        SharedPreferences pref=getSharedPreferences("MyPrefs", MODE_PRIVATE);
        currentUser=pref.getString("username",null);
        accessToken=pref.getString("access_token",null);
        if(accessToken==null){
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if(postId==-1){
            Toast.makeText(this, "Error: Invalid post ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        initViews();
        setupRecyclerView();
        loadComments();



        btnAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addComment();
            }
        });
    }



    private void setupRecyclerView() {
        commentsList=new ArrayList<>();
        commentsAdapter=new CommentsAdapter(commentsList);
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewComments.setAdapter(commentsAdapter);
    }

    private void initViews() {
        recyclerViewComments=findViewById(R.id.rvComments);
        editTextComment=findViewById(R.id.editTextComment);
        btnAddComment=findViewById(R.id.btnAddComment);
    }

    private void loadComments() {
        String url="https://dormitorybackend.duckdns.org/api/posts/" + postId + "/comments";

        JsonArrayRequest request=new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        commentsList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject commentObj = response.getJSONObject(i);
                            Comment comment = new Comment();
                            comment.setId(commentObj.getInt("id"));
                            comment.setContent(commentObj.getString("body")); // Changed from "content" to "body"
                            comment.setAuthor(commentObj.getString("author"));
                            comment.setCreatedAt(commentObj.getString("created_at")); // Changed from "createdAt" to "created_at"
                            comment.setPostId(postId);
                            commentsList.add(comment);
                        }
                        commentsAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing comments", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                        Toast.makeText(this, "Authentication failed. Please login again.", Toast.LENGTH_SHORT).show();
                        // Redirect to login
                        redirectToLogin();
                    } else {
                        Toast.makeText(this, "Error loading comments", Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private void addComment(){
            // Get the comment text and check for null
            String commentText = null;
            if (editTextComment != null && editTextComment.getText() != null) {
                commentText = editTextComment.getText().toString().trim();
            }

            // Check if comment text is empty or null
            if (commentText == null || commentText.isEmpty()) {
                Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if currentUser is not null
            if (currentUser == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = "https://dormitorybackend.duckdns.org/api/posts/" + postId + "/comments";

            // Create JSON object for new comment
            JSONObject commentData = new JSONObject();
            try {
                commentData.put("body", commentText);
                commentData.put("author", currentUser);
                commentData.put("postId", postId);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error creating comment data", Toast.LENGTH_SHORT).show();
                return;
            }

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    commentData,
                    response -> {
                        Toast.makeText(this, "Comment added successfully", Toast.LENGTH_SHORT).show();
                        if (editTextComment != null) {
                            editTextComment.setText("");
                        }
                        loadComments(); // Reload comments to show the new one
                    },
                    error -> {
                        if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                            Toast.makeText(this, "Authentication failed. Please login again.", Toast.LENGTH_SHORT).show();
                            redirectToLogin();
                        } else {
                            Toast.makeText(this, "Error adding comment", Toast.LENGTH_SHORT).show();
                            // Log the error for debugging
                            if (error.networkResponse != null) {
                                String errorMessage = new String(error.networkResponse.data);
                                Log.e("CommentActivity", "Error response: " + errorMessage);
                            }
                        }
                    }
            ){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + accessToken);
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            Volley.newRequestQueue(this).add(request);
    }

    private void redirectToLogin() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();

        // Redirect to login activity
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}