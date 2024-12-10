package com.saif.mywhatsapp.Models;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "messages", indices = {@Index("senderRoom"), @Index("receiverRoom")})
public class Message {
    public static final int STATUS_SENT = 0;
    public static final int STATUS_DELIVERED = 1;
    public static final int STATUS_READ = 2;

    @PrimaryKey
    private long timeStamp;
    private String message;
    private String senderId;
    private int status;
    private String key;
    private boolean isAudioMessage;
    private String senderRoom;
    private String receiverRoom;

    // Default no-args constructor required by Firebase
    public Message(){
    }


    public Message(String message, String senderId, long timeStamp, String key, int status, boolean isAudioMessage, String senderRoom,String receiverRoom) {
        this.message = message;
        this.senderId = senderId;
        this.timeStamp = timeStamp;
        this.status = status;
        this.key = key;
        this.isAudioMessage = isAudioMessage;
        this.senderRoom=senderRoom;
        this.receiverRoom=receiverRoom;
    }

    public String getReceiverRoom() {
        return receiverRoom;
    }

    public void setReceiverRoom(String receiverRoom) {
        this.receiverRoom = receiverRoom;
    }

    public String getSenderRoom() {
        return senderRoom;
    }

    public void setSenderRoom(String senderRoom) {
        this.senderRoom = senderRoom;
    }

    public boolean isAudioMessage() {
        return isAudioMessage;
    }
    public void setAudioMessage(boolean audioMessage) {
        isAudioMessage = audioMessage;
    }
    public String getMessage() { return message; }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public long getTimeStamp() { return timeStamp; }
    public void setTimeStamp(long timeStamp) {this.timeStamp = timeStamp;
    }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

}
