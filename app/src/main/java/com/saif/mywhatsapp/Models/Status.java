package com.saif.mywhatsapp.Models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Status {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String userId;  // Add this line
    private String imageUrl;
    private long timeStamps;
    private boolean isLocal;

    public Status() {
    }

    public Status(String userId, String imageUrl, long timeStamps) {
        this.userId = userId;
        this.imageUrl = imageUrl;
        this.timeStamps = timeStamps;
        this.isLocal = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTimeStamps() {
        return timeStamps;
    }

    public void setTimeStamps(long timeStamps) {
        this.timeStamps = timeStamps;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public void setLocal(boolean local) {
        isLocal = local;
    }
}
