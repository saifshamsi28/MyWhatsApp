package com.saif.mywhatsapp.Models;

import com.google.gson.annotations.SerializedName;

public class VerifyOtpResponse {
    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("refresh_token")
    private String refreshToken;

    @SerializedName("expires_in")
    private Integer expiresIn; // Use Integer to avoid null errors

    @SerializedName("token_type")
    private String tokenType;

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }
    public String getTokenType() {
        return tokenType;
    }

}

