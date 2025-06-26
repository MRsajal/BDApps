package com.example.bdapps;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfilePage extends AppCompatActivity {
    private static final String TAG = "ProfileView";
    private static final String API_URL = "https://dormitorybackend.duckdns.org/api/auth/profile";

    // UI Components
    private ShapeableImageView profileImageView;
    private TextView nameTextView;
    private TextView bioTextView;
    private TextView followersCountTextView;
    private TextView followersLabelTextView;

    private RequestQueue requestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager2 viewPager = findViewById(R.id.view_pager);



        ProfileViewTabsAdapter adapter = new ProfileViewTabsAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("About");
            }
            else if (position == 1) {
                tab.setText("Posts");
            } else{
                tab.setText("Activity");
            }
        }).attach();
        tabLayout.post(() -> viewPager.setCurrentItem(1, false));

        requestQueue = Volley.newRequestQueue(this);

        initViews();

    }

    private void initViews() {
        // Find views by their IDs from your XML
        profileImageView = findViewById(R.id.constraintLayout).findViewById(R.id.imageView); // Profile image inside constraint layout
        nameTextView = findViewById(R.id.textView);
        bioTextView = findViewById(R.id.textView2);

        // For followers count, we'll use the existing TextView in linearLayout4
        followersCountTextView = findViewById(R.id.followerCount);// First TextView showing "97"
        //followersLabelTextView = findViewById(R.id.linearLayout4).getChildAt(1); // Second TextView showing "Followers"
    }

    private void fetchProfileData() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                API_URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            parseAndDisplayData(response);
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error: " + e.getMessage());
                            showError("Error parsing data");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "API request error: " + error.getMessage());
                        showError("Failed to load profile data");
                    }
                }
        );

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }
    private void parseAndDisplayData(JSONObject response) throws JSONException {
        // Extract data from JSON response
        String name = response.optString("name", "Unknown User");
        String profilePic = response.optString("profile_pic", "");
        String followersCount = response.optString("followers_count", "0");
        String bio = response.optString("bio", "No bio available");

        // Update UI on main thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateUI(name, profilePic, followersCount, bio);
            }
        });
    }

    private void updateUI(String name, String profilePic, String followersCount, String bio) {
        // Set name
        nameTextView.setText(name);

        // Set bio (replacing the "CSE student" text)
        bioTextView.setText(bio);

        // Set followers count
        if (followersCountTextView instanceof TextView) {
            ((TextView) followersCountTextView).setText(followersCount);
        }

        // Load profile image using Glide
        if (!profilePic.isEmpty()) {
            loadProfileImage(profilePic);
        }

        Log.d(TAG, "Profile data updated successfully");
    }

    private void loadProfileImage(String imageUrl) {
        // Find the actual profile image view (the ShapeableImageView inside the constraint layout)
        ShapeableImageView actualProfileImage = findViewById(R.id.constraintLayout)
                .findViewById(R.id.imageView);

        if (actualProfileImage == null) {
            // If the above doesn't work, try to find it directly
            // You might need to add an ID to the profile image in your XML
            Log.w(TAG, "Profile image view not found");
            return;
        }

        Glide.with(this)
                .load(imageUrl)
                .transform(new CircleCrop())
                .placeholder(R.drawable.avatar) // Your default avatar
                .error(R.drawable.avatar) // Fallback to default avatar on error
                .into(actualProfileImage);
    }

    private void showError(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ProfilePage.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel all pending requests to avoid memory leaks
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }
}