package com.saif.mywhatsapp.Models;

import java.util.ArrayList;

public class UserStatus {
    private String userId; // New field for user ID
    private String name;
    private String profileImage;
    private long lastUpdated;
    private ArrayList<Status> statuses;

    public UserStatus() {
    }

    public UserStatus(String userId, String name, String profileImage, long lastUpdated, ArrayList<Status> statuses) {
        this.userId = userId;
        this.name = name;
        this.profileImage = profileImage;
        this.lastUpdated = lastUpdated;
        this.statuses = statuses;
    }

    public String getUserId() {
        return userId; // Getter for user ID
    }

    public void setUserId(String userId) {
        this.userId = userId; // Setter for user ID
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public ArrayList<Status> getStatuses() {
        return statuses;
    }

    public void setStatuses(ArrayList<Status> statuses) {
        this.statuses = statuses;
    }
}
