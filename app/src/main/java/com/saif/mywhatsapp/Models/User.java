package com.saif.mywhatsapp.Models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {

    @PrimaryKey
    @NonNull
    private String phoneNumber;

    private String uid;
    private String name;
    private String profileImage;
    private String about;

    // Default no-args constructor required by Firebase
    public User() {
    }

    public User(String uid, String name, @NonNull String phoneNumber, String profileImage, String about) {
        this.uid = uid;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.profileImage = profileImage;
        this.about = about;
    }

    @NonNull
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(@NonNull String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }
}
