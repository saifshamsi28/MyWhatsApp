package com.saif.mywhatsapp;

import com.saif.mywhatsapp.Models.SendOtpRequest;
import com.saif.mywhatsapp.Models.SendOtpResponse;
import com.saif.mywhatsapp.Models.VerifyOtpRequest;
import com.saif.mywhatsapp.Models.VerifyOtpResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseAuthApi {

    @POST("auth/v1/verify")
    Call<VerifyOtpResponse> verifyOtp(@Body VerifyOtpRequest request);

    @POST("auth/v1/otp")
    Call<SendOtpResponse> sendOtp(@Body SendOtpRequest request);
    // Phone number as query param
}
