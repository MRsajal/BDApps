package com.example.bdapps;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class UserProfileActivity extends AppCompatActivity {

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

    private User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        user = (User) getIntent().getSerializableExtra("user");
        if (user == null) {
            finish();
            return;
        }

        initViews();
        populateUserData();
    }
    private void initViews() {
        profileImageView = findViewById(R.id.profileImageView);
        nameTextView = findViewById(R.id.nameTextView);
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

        // Load profile image using Glide (you'll need to add Glide dependency)
        if (!profile.getProfilePic().isEmpty()) {
            // Glide.with(this).load(profile.getProfilePic()).into(profileImageView);
            // For now, just set a placeholder
            profileImageView.setImageResource(R.drawable.ic_person_placeholder);
        } else {
            profileImageView.setImageResource(R.drawable.ic_person_placeholder);
        }
    }
}