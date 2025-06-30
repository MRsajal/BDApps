package com.example.bdapps.Recommendation;

public class User {
    private int id;
    private String username;
    private String email;
    private Profile profile;
    private boolean isFollowing;
    public User() {}

    public User(int id, String username, String email, Profile profile) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.profile = profile;
        this.isFollowing = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public boolean isFollowing() {
        return isFollowing;
    }

    public void setFollowing(boolean following) {
        isFollowing = following;
    }
}
