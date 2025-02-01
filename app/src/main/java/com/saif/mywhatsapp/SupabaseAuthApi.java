package com.saif.mywhatsapp;

import com.saif.mywhatsapp.Models.SendOtpRequest;
import com.saif.mywhatsapp.Models.SendOtpResponse;
import com.saif.mywhatsapp.Models.User;
import com.saif.mywhatsapp.Models.VerifyOtpRequest;
import com.saif.mywhatsapp.Models.VerifyOtpResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface SupabaseAuthApi {

    @POST("auth/v1/verify")
    Call<VerifyOtpResponse> verifyOtp(@Body VerifyOtpRequest request);

    @POST("auth/v1/otp")
    Call<SendOtpResponse> sendOtp(@Body SendOtpRequest request);

    @GET("/auth/v1/user")
    Call<User> getUser(@Header("Authorization") String authToken);

    @PUT("/rest/v1/Users")
    Call<Void> updateUser(@Header("Authorization") String authToken, @Body User user);

        @GET("auth/v1/user")
        Call<ResponseBody> getUserRaw(@Header("Authorization") String authToken);
}
