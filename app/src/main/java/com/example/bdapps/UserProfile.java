package com.example.bdapps;

import java.io.Serializable;

public class UserProfile implements Serializable {
    private String name;
    private String profilePic;
    private int followersCount;
    private String followingCount;
    private String isFollowing;
    private String address;
    private String bio;
    private String personalWebsite;
    private String aboutMe;
    private String gender;
    private String genderDisplay;

    // Constructors
    public UserProfile() {}

    // Getters and Setters
    public String getName() { return name != null ? name : ""; }
    public void setName(String name) { this.name = name; }

    public String getProfilePic() { return profilePic != null ? profilePic : ""; }
    public void setProfilePic(String profilePic) { this.profilePic = profilePic; }

    public int getFollowersCount() { return followersCount != 0 ? followersCount : 0; }
    public void setFollowersCount(int followersCount) { this.followersCount = followersCount; }

    public String getFollowingCount() { return followingCount != null ? followingCount : "0"; }
    public void setFollowingCount(String followingCount) { this.followingCount = followingCount; }

    public String getIsFollowing() { return isFollowing != null ? isFollowing : "false"; }
    public void setIsFollowing(String isFollowing) { this.isFollowing = isFollowing; }

    public String getAddress() { return address != null ? address : ""; }
    public void setAddress(String address) { this.address = address; }

    public String getBio() { return bio != null ? bio : ""; }
    public void setBio(String bio) { this.bio = bio; }

    public String getPersonalWebsite() { return personalWebsite != null ? personalWebsite : ""; }
    public void setPersonalWebsite(String personalWebsite) { this.personalWebsite = personalWebsite; }

    public String getAboutMe() { return aboutMe != null ? aboutMe : ""; }
    public void setAboutMe(String aboutMe) { this.aboutMe = aboutMe; }

    public String getGender() { return gender != null ? gender : ""; }
    public void setGender(String gender) { this.gender = gender; }

    public String getGenderDisplay() { return genderDisplay != null ? genderDisplay : ""; }
    public void setGenderDisplay(String genderDisplay) { this.genderDisplay = genderDisplay; }
}