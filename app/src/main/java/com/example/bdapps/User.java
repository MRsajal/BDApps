package com.example.bdapps;

import java.io.Serializable;

public class User implements Serializable {
    private String userId;
    private String username;
    private String email;
    private String profileImageUrl;
    private String displayName;
    private UserProfile profile;

    public User() {}

    public User(String userId, String username, String email, String displayName) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.displayName = displayName;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public UserProfile getProfile() { return profile; }
    public void setProfile(UserProfile profile) { this.profile = profile; }
}
