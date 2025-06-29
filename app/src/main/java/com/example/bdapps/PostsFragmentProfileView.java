package com.example.bdapps;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import java.util.ArrayList;
import java.util.List;

public class PostsFragmentProfileView extends Fragment {
    private String etUsername;
    private RecyclerView rvPosts;
    private ProgressBar progressBar;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private RecyclerView recyclerView;
    private RequestQueue requestQueue;
    private static final String API_URL = "https://dormitorybackend.duckdns.org/api/posts";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_post_profile_view, container, false);
        recyclerView=view.findViewById(R.id.recyclerViewPosts);
        postList = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(requireContext());
        setupRecyclerView();
        loadUserPostsFromAPI();
        return view;
    }
    private void setupRecyclerView(){
        postList=new ArrayList<>();
        postAdapter=new PostAdapter(postList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(postAdapter);
        //addSamplePosts();
    }
    private void loadUserPostsFromAPI() {
        String url = "https://dormitorybackend.duckdns.org/api/posts";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray results = response.getJSONArray("results");
                            postList.clear();

                            // Get current user's username
                            String currentUsername = getCurrentUsername();

                            // If no current user found, don't show any posts
                            if (currentUsername == null || currentUsername.isEmpty()) {
                                postAdapter.notifyDataSetChanged();
                                return;
                            }

                            // Parse each post and filter by current user
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject postJson = results.getJSONObject(i);

                                // Extract post data
                                Integer id = postJson.getInt("id");
                                String author = postJson.getString("author");
                                String title = postJson.getString("title");
                                String content = postJson.getString("content");
                                String imageUrl = postJson.optString("image", "");
                                String createdAt = postJson.getString("created_at");

                                // Only add posts from the current user
                                if (author.equals(currentUsername)) {
                                    Post post = new Post(id, content, author, createdAt);
                                    postList.add(post);
                                }
                            }

                            // Notify adapter that data has changed
                            postAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            // Handle JSON parsing error
                            showError("Error parsing posts data");
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle network error
                        error.printStackTrace();
                        showError("Failed to load posts. Please check your internet connection.");
                    }
                });

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

    // Helper method to get current user's username
    private String getCurrentUsername() {
        // Return the current user's username from SharedPreferences, session, etc.
        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return prefs.getString("username", "");
    }
    private void showError(String message) {
        // You can implement this using Toast, Snackbar, or AlertDialog
         Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

    }
    private void addSamplePosts() {
        postList.add(new Post("Welcome to our social media app! Share your thoughts and connect with others.", "Admin"));
        postList.add(new Post("Beautiful sunset today! 🌅", "User1"));
        postList.add(new Post("Welcome to our social media app! Share your thoughts and connect with others.", "Admin"));
        postList.add(new Post("Beautiful sunset today! 🌅", "User1"));
        postList.add(new Post("Welcome to our social media app! Share your thoughts and connect with others.", "Admin"));
        postList.add(new Post("Beautiful sunset today! 🌅", "User1"));
        postAdapter.notifyDataSetChanged();
    }
}