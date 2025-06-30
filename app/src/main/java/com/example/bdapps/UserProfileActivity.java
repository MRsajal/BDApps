package com.example.bdapps;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = "UserProfileActivity";
    private static final String BASE_URL = "https://dormitorybackend.duckdns.org/api/auth/follow/";
    private OkHttpClient client;
    private ImageView profileImageView;
    private TextView nameTextView;
    private TextView usernameTextView;
    private TextView emailTextView;
    private TextView followersTextView;
    private TextView followingTextView;
    private TextView bioTextView;
    private TextView addressTextView;
    private TextView websiteTextView;
    private TextView aboutMeTextView;
    private TextView genderTextView;
    private Button followButton;
    String accessToken;

    private User user;
    private String currentUsername;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        SharedPreferences pref=getSharedPreferences("MyPrefs", MODE_PRIVATE);
        accessToken=pref.getString("access_token","");


        user = (User) getIntent().getSerializableExtra("user");
        if (user == null) {
            finish();
            return;
        }
        client = new OkHttpClient();
        currentUsername=user.getUsername();
        initViews();
        populateUserData();
        setupFollowButton();
    }

    private void setupFollowButton() {
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                followButton.setEnabled(false);

                UserProfile profile = user.getProfile();
                boolean isCurrentlyFollowing = "true".equals(profile.getIsFollowing());

                if (isCurrentlyFollowing) {
                    unfollowUser();
                } else {
                    followUser();
                }
            }
        });
    }
    private void followUser() {
        String url = BASE_URL + user.getUsername();

        // Create JSON request body
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("follower_username", currentUsername);
            jsonBody.put("following_username", user.getUsername());
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON body", e);
            followButton.setEnabled(true);
            return;
        }

        RequestBody body = RequestBody.create(
                jsonBody.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                // Add authorization header if needed
                .addHeader("Authorization", "Bearer " + getAuthToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Follow request failed", e);
                runOnUiThread(() -> {
                    followButton.setEnabled(true);
                    Toast.makeText(UserProfileActivity.this, "Failed to follow user", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    followButton.setEnabled(true);

                    if (response.isSuccessful()) {
                        // Update UI to show following state
                        followButton.setText("Unfollow");
                        user.getProfile().setIsFollowing("true");

                        // Update followers count
                        int currentFollowers = user.getProfile().getFollowersCount();
                        user.getProfile().setFollowersCount(currentFollowers + 1);
                        followersTextView.setText("Followers: " + (currentFollowers + 1));

                        Toast.makeText(UserProfileActivity.this, "Successfully followed " + user.getUsername(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(UserProfileActivity.this, "Failed to follow user", Toast.LENGTH_SHORT).show();
                    }
                });
                response.close();
            }
        });
    }
    private void unfollowUser() {
        String url = BASE_URL + user.getUsername();

        Request request = new Request.Builder()
                .url(url)
                .delete()
                // Add authorization header if needed
                .addHeader("Authorization", "Bearer " + getAuthToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Unfollow request failed", e);
                runOnUiThread(() -> {
                    followButton.setEnabled(true);
                    Toast.makeText(UserProfileActivity.this, "Failed to unfollow user", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    followButton.setEnabled(true);

                    if (response.isSuccessful()) {
                        // Update UI to show not following state
                        followButton.setText("Follow");
                        user.getProfile().setIsFollowing("false");

                        // Update followers count
                        int currentFollowers = user.getProfile().getFollowersCount();
                        user.getProfile().setFollowersCount(Math.max(0, currentFollowers - 1));
                        followersTextView.setText("Followers: " + Math.max(0, currentFollowers - 1));

                        Toast.makeText(UserProfileActivity.this, "Successfully unfollowed " + user.getUsername(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(UserProfileActivity.this, "Failed to unfollow user", Toast.LENGTH_SHORT).show();
                    }
                });
                response.close();
            }
        });
    }
    private String getAuthToken() {
        // Implement this based on your authentication system
         SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
         return prefs.getString("access_token", "");

    }

    private void initViews() {
        profileImageView = findViewById(R.id.profileImageView);
        nameTextView = findViewById(R.id.nameTextViewProfile);
        usernameTextView = findViewById(R.id.usernameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        followersTextView = findViewById(R.id.followersTextView);
        followingTextView = findViewById(R.id.followingTextView);
        bioTextView = findViewById(R.id.bioTextView);
        addressTextView = findViewById(R.id.addressTextView);
        websiteTextView = findViewById(R.id.websiteTextView);
        aboutMeTextView = findViewById(R.id.aboutMeTextView);
        genderTextView = findViewById(R.id.genderTextView);
        followButton = findViewById(R.id.followButton);
    }

    private void populateUserData() {
        UserProfile profile = user.getProfile();

        nameTextView.setText(profile.getName().isEmpty() ? "No name provided" : profile.getName());
        usernameTextView.setText("@" + user.getUsername());
        emailTextView.setText(user.getEmail());

        followersTextView.setText("Followers: " + profile.getFollowersCount());
        followingTextView.setText("Following: " + profile.getFollowingCount());

        // Set bio or hide if empty
        if (!profile.getBio().isEmpty()) {
            bioTextView.setText(profile.getBio());
            bioTextView.setVisibility(View.VISIBLE);
        } else {
            bioTextView.setVisibility(View.GONE);
        }

        // Set address or hide if empty
        if (!profile.getAddress().isEmpty()) {
            addressTextView.setText("📍 " + profile.getAddress());
            addressTextView.setVisibility(View.VISIBLE);
        } else {
            addressTextView.setVisibility(View.GONE);
        }

        // Set website or hide if empty
        if (!profile.getPersonalWebsite().isEmpty()) {
            websiteTextView.setText("🌐 " + profile.getPersonalWebsite());
            websiteTextView.setVisibility(View.VISIBLE);
        } else {
            websiteTextView.setVisibility(View.GONE);
        }

        // Set about me or hide if empty
        if (!profile.getAboutMe().isEmpty()) {
            aboutMeTextView.setText(profile.getAboutMe());
            aboutMeTextView.setVisibility(View.VISIBLE);
        } else {
            aboutMeTextView.setVisibility(View.GONE);
        }

        // Set gender or hide if empty
        if (!profile.getGenderDisplay().isEmpty()) {
            genderTextView.setText("Gender: " + profile.getGenderDisplay());
            genderTextView.setVisibility(View.VISIBLE);
        } else {
            genderTextView.setVisibility(View.GONE);
        }

        // Set follow button text
        boolean isFollowing = "true".equals(profile.getIsFollowing());
        followButton.setText(isFollowing ? "Unfollow" : "Follow");

        // Load profile image using Glide
        if (!profile.getProfilePic().isEmpty()) {
            Glide.with(this)
                    .load(profile.getProfilePic())
                    .placeholder(R.drawable.ic_person_placeholder) // Shows while loading
                    .error(R.drawable.ic_person_placeholder) // Shows if loading fails
                    .circleCrop() // Optional: makes image circular
                    .into(profileImageView);
        } else {
            profileImageView.setImageResource(R.drawable.ic_person_placeholder);
        }
    }
}