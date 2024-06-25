package com.saif.mywhatsapp.Models;

public class Message {
    public static final int STATUS_SENT = 0;
    public static final int STATUS_DELIVERED = 1;
    public static final int STATUS_READ = 2;
    private String message;
    private String senderId;
    private long timeStamp;
    private int status;
    private String key; // Add this field

    public Message(){

    }

    // Existing constructor
    public Message(String message, String senderId, long timeStamp) {
        this.message = message;
        this.senderId = senderId;
        this.timeStamp = timeStamp;
        this.status = 0; // Default status
    }

    // New constructor with key
    public Message(String message, String senderId, long timeStamp, String key,int status) {
        this.message = message;
        this.senderId = senderId;
        this.timeStamp = timeStamp;
        this.status = status; // Default status
        this.key = key;
    }

    // Getters and setters
    public String getMessage() { return message; }
    public String getSenderId() { return senderId; }
    public long getTimeStamp() { return timeStamp; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
}
