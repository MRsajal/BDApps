package com.example.bdapps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private TextView userNameTextView;
    private TextView userEmailTextView;
    private CircleImageView profileImageView;
    DrawerLayout drawerLayout;
    MaterialButton btnMenu;
    MaterialButton btnChat, btnSearch;
    FloatingActionButton btnAddPost;
    LinearLayout menuProfile, menuPost, menuLogout, menuGroups;

    RecyclerView recyclerView;
    PostAdapter postAdapter;
    List<Post> postList;
    String currentUsername;
    String accessToken;
    RequestQueue requestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentUsername= getIntent().getStringExtra("current_username");
        accessToken = getIntent().getStringExtra("access_token");
        initView();
        setupClickListeners();
        setupRecyclerView();
        requestQueue=Volley.newRequestQueue(this);
        //fetchPosts();
        loadPostsFromAPI();
        fetchUserProfile();
    }
    private void loadPostsFromAPI() {
        // Create a request queue for Volley (you'll need to add Volley dependency)
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://dormitorybackend.duckdns.org/api/posts";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Get the results array from the response
                            JSONArray results = response.getJSONArray("results");

                            // Clear existing posts
                            postList.clear();

                            // Parse each post from the API response
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject postJson = results.getJSONObject(i);

                                // Extract post data
                                Integer id=postJson.getInt("id");
                                String author = postJson.getString("author");
                                String title = postJson.getString("title");
                                String content = postJson.getString("content");
                                String imageUrl = postJson.optString("image", ""); // Use optString for optional fields
                                String createdAt = postJson.getString("created_at");

                                // Create Post object (you may need to modify your Post constructor)
                                Post post = new Post(id,content, author,createdAt);

                                // Add to post list
                                postList.add(post);
                            }

                            // Notify adapter that data has changed
                            postAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            // Handle JSON parsing error
                            //addSamplePosts();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle network error
                        error.printStackTrace();
                        //addSamplePosts();
                    }
                });

        // Add the request to the RequestQueue
        queue.add(jsonObjectRequest);
    }
    private void initView() {
        drawerLayout=findViewById(R.id.drawer_layout);
        btnMenu=findViewById(R.id.btn_menu);
        btnChat=findViewById(R.id.btn_chat);
        btnAddPost=findViewById(R.id.btn_add_post);
        recyclerView=findViewById(R.id.recycler_view);
        btnSearch=findViewById(R.id.btn_search);



        menuProfile=findViewById(R.id.menu_profile);
//        menuPost=findViewById(R.id.menu_post);
//        menuGroups=findViewById(R.id.menu_group);
        menuLogout=findViewById(R.id.menu_logout);

        String accessToken = getIntent().getStringExtra("access_token");
        if (accessToken != null) {
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("access_token", accessToken);
            editor.apply();
        }

    }

    private void fetchUserProfile() {
        userNameTextView=findViewById(R.id.user_name);
        //userEmailTextView=findViewById(R.id.user_email);
        profileImageView=findViewById(R.id.profile_image);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                "https://dormitorybackend.duckdns.org/api/auth/profile",
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("ProfileAPI", "Response received: " + response.toString());

                            String name = response.optString("name", "User");
                            String profilePicUrl = response.optString("profile_pic", "");
                            String email = getStoredUserEmail();

                            updateProfileUI(name, email, profilePicUrl);

                        } catch (Exception e) {
                            Log.e("ProfileAPI", "Error parsing response: " + e.getMessage());
                            e.printStackTrace();
                            showErrorMessage("Error parsing profile data: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ProfileAPI", "Volley Error: " + error.toString());

                        String errorMessage = "Unknown error";
                        int statusCode = 0;

                        if (error.networkResponse != null) {
                            statusCode = error.networkResponse.statusCode;
                            try {
                                String responseBody = new String(error.networkResponse.data, "utf-8");
                                try {
                                    JSONObject errorJson = new JSONObject(responseBody);
                                    errorMessage = errorJson.optString("message", errorJson.optString("error", "API Error"));
                                } catch (JSONException je) {
                                    errorMessage = "HTTP " + statusCode + ": " + responseBody;
                                }
                            } catch (UnsupportedEncodingException e) {
                                Log.e("ProfileAPI", "Error reading response: " + e.getMessage());
                            }
                        } else if (error instanceof TimeoutError) {
                            errorMessage = "Request timeout";
                        } else if (error instanceof NoConnectionError) {
                            errorMessage = "No internet connection";
                        } else if (error instanceof AuthFailureError) {
                            errorMessage = "Authentication failed - check your token";
                        } else {
                            errorMessage = "Server error";
                        }

                        Log.e("ProfileAPI", "Final Error Message: " + errorMessage);
                        showErrorMessage("API Error: " + errorMessage);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                String accessTokenTemp = prefs.getString("access_token", null);

                // Check if accessToken is valid
                if (accessTokenTemp == null || accessTokenTemp.isEmpty()) {
                    Log.e("ProfileAPI", "Access token is null or empty!");
                    throw new AuthFailureError("Access token is missing");
                }

                headers.put("Authorization", "Bearer " + accessTokenTemp);
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");

                Log.d("ProfileAPI", "Headers: " + headers.toString());
                return headers;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                Log.d("ProfileAPI", "Network Response - Status Code: " + response.statusCode);
                Log.d("ProfileAPI", "Network Response - Headers: " + response.headers.toString());
                return super.parseNetworkResponse(response);
            }
        };

        // Set retry policy for better error handling
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000, // 30 seconds timeout
                1, // Number of retries
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(jsonObjectRequest);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void checkNetworkAndFetchProfile() {
        if (!isNetworkAvailable()) {
            showErrorMessage("No internet connection");
            return;
        }

        if (accessToken == null || accessToken.isEmpty()) {
            showErrorMessage("Please login again");
            return;
        }

        fetchUserProfile();
    }
    private void updateProfileUI(String name, String email, String profilePicUrl) {
        userNameTextView.setText(name);
        //userEmailTextView.setText(email);

        if (!profilePicUrl.isEmpty()) {
            Glide.with(this)
                    .load(profilePicUrl)
                    .placeholder(R.drawable.img)
                    .error(R.drawable.img)
                    .into(profileImageView);
        }
    }
    private String getStoredUserEmail() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("user_email", "user@example.com");
    }

    private void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    private void setupRecyclerView(){
        postList=new ArrayList<>();
        postAdapter=new PostAdapter(postList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(postAdapter);
       // addSamplePosts();
    }


    private void setupClickListeners(){
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(drawerLayout.isDrawerOpen(GravityCompat.START)){
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                else{
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });
        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, ChatBot.class);
                intent.putExtra("access_token", accessToken);
                startActivity(intent);
            }
        });
        btnAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddPostDialog();
            }
        });
        menuProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Profile selected", Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(MainActivity.this, ProfilePage.class);
                intent.putExtra("current_username",currentUsername);
                startActivity(intent);
//                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        menuLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });
    }
    private void logoutUser(){
        SharedPreferences prefs=getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor=prefs.edit();
        editor.remove("access_token");
        editor.remove("username");
        editor.apply();
        Intent intent=new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showAddPostDialog() {
        Dialog dialog=new Dialog(this);
        dialog.setContentView(R.layout.add_post);
        dialog.getWindow().setLayout((int)(getResources().getDisplayMetrics().widthPixels*0.9),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        EditText etPostContent=dialog.findViewById(R.id.et_post_content);
        TextView tvCharCounter=dialog.findViewById(R.id.tv_char_counter);
        Button btnCancel=dialog.findViewById(R.id.btn_cancel);
        Button btnPost=dialog.findViewById(R.id.btn_post);

        etPostContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                tvCharCounter.setText(charSequence.length()+"/500");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String postContent=etPostContent.getText().toString().trim();
                if(!postContent.isEmpty()){
                    btnPost.setEnabled(false);
                    btnPost.setText("Posting...");
                    makePostRequest(postContent,dialog,btnPost);
                }else{
                    Toast.makeText(MainActivity.this, "Please write something to post", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show();
    }


    private void makePostRequest(String content, Dialog dialog, Button btnPost) {
        // Create the request queue
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://dormitorybackend.duckdns.org/api/posts";

        // Create JSON object for the request body
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("title", "You"); // You can modify this to get title from user input
            jsonBody.put("content", content);
//            jsonBody.put("image", ""); // Add image handling if needed
//            jsonBody.put("source_url", ""); // Add source URL if needed
            jsonBody.put("tags", new JSONArray()); // Empty tags array, modify as needed
        } catch (JSONException e) {
            e.printStackTrace();
            resetPostButton(btnPost);
            Toast.makeText(MainActivity.this, "Error creating post data", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the request
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Success response
                        runOnUiThread(() -> {
                            // Add post to local list (you might want to refresh from server instead)
                            Post newPost = new Post(content, "You");
                            postAdapter.addPost(newPost);
                            recyclerView.smoothScrollToPosition(0);

                            Toast.makeText(MainActivity.this, "Post shared successfully!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        });
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        runOnUiThread(() -> {
                            resetPostButton(btnPost);
                            String errorMessage = "Failed to post. Please try again.";

                            if (error.networkResponse != null) {
                                int statusCode = error.networkResponse.statusCode;
                                errorMessage = "Error " + statusCode + ": Failed to post";
                            }

                            Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                // Add authentication headers if required
                headers.put("Authorization", "Bearer " + accessToken);
                return headers;
            }
        };

        // Set timeout (optional)
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000, // 10 seconds timeout
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        // Add request to queue
        queue.add(request);
    }
    private void resetPostButton(Button btnPost) {
        btnPost.setEnabled(true);
        btnPost.setText("Post");
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
    }
}