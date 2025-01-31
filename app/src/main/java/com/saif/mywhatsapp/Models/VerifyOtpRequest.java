package com.saif.mywhatsapp.Models;

public class VerifyOtpRequest {
    String type;
    String phone;
    String token;

    public VerifyOtpRequest(String phone, String token) {
        this.type = "sms";  // Always use "sms" for phone OTPs
        this.phone = phone;
        this.token = token;
    }
}

