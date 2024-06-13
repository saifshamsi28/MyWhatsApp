package com.saif.mywhatsapp.Models;

public class Status {


    private String imageUrl;
    private long timeStamps;

    public Status() {
    }

    public Status(String imageUrl, long timeStamps) {
        this.imageUrl = imageUrl;
        this.timeStamps = timeStamps;
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
}
