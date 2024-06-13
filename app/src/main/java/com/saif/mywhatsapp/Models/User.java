package com.saif.mywhatsapp.Models;

public class User {
    private String uid;
    private String name;
    private String phoneNumber;
    private String profileImage;
    private String about;


    public User(String about){
        // always put an empty constructor when dealing with firebase
    }
    public User(String uid, String name, String phone, String profileImage, String about) {
        this.uid = uid;
        this.name = name;
        this.phoneNumber = phone;
        this.profileImage = profileImage;
        this.about = about;
    }

    public User() {
        this.uid = "i2hwj2si3hd3s3insi3891101jwhs2w39eie39s39su39";
        this.name = "Saif";
        this.phoneNumber = "+910288378283";
        this.profileImage = "No profile";
        this.about = "Developer of this app";
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
