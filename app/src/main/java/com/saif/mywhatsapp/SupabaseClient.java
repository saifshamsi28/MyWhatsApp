package com.saif.mywhatsapp;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseClient {

    private static final String BASE_URL = "https://issnpiizowpkyzkuselm.supabase.co"; // Replace with your actual base URL
    public static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imlzc25waWl6b3dwa3l6a3VzZWxtIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzgxMDQ1MzMsImV4cCI6MjA1MzY4MDUzM30.pFEpj0E_NIqcmd0UIQXuOUVLAQoW8j_c-Gd56XPYoZo"; // Replace with your actual API key
//    const phoneNumber = "+15551234567"; // Example phone number
//    const url = `https://your-supabase-project-url/auth/v1/phone/sign_in_with_otp?phone=${phoneNumber}`;

    // Make an API call to this URL to send the OTP

    private Retrofit retrofit;

    public SupabaseAuthApi getAuthApi() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request newRequest = chain.request().newBuilder()
                                    .addHeader("apikey", API_KEY) // Include API key/ Required for auth
                                    .addHeader("Content-Type", "application/json")
                                    .build();
                            return chain.proceed(newRequest);
                        }
                    }).build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(SupabaseAuthApi.class);
    }
}
