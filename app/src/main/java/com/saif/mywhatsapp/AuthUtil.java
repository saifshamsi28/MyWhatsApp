package com.saif.mywhatsapp;//package com.saif.mywhatsapp;
//
//import android.content.Context;
//
//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.auth.oauth2.ServiceAccountCredentials;
//
//import java.io.InputStream;
//import java.io.IOException;
//
//public class AuthUtil {
//    public static String getAccessToken(Context context) throws IOException {
//        // Access the JSON file from the raw directory
//        InputStream serviceAccountStream = context.getResources().openRawResource(R.raw.service_account);
//        GoogleCredentials credentials = ServiceAccountCredentials.fromStream(serviceAccountStream)
//                .createScoped("https://www.googleapis.com/auth/firebase.messaging");
//        credentials.refreshIfExpired();
//        return credentials.getAccessToken().getTokenValue();
//    }
//}
import android.content.Context;
import android.util.Log;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;

import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPrivateKey;
import java.util.Collections;
import java.util.Date;

public class AuthUtil {

    public static String getAccessToken(Context context) throws IOException {

        Log.d("AuthUtil", "getAccessToken called");
        // Load the service account private key file
        InputStream stream = context.getResources().openRawResource(R.raw.service_account);

        // Parse the private key file
        GoogleCredentials credential = ServiceAccountCredentials.fromStream(stream)
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/firebase.messaging"));

        // Generate JWT
        ServiceAccountCredentials credentials = (ServiceAccountCredentials) credential;
        RSAPrivateKey privateKey = (RSAPrivateKey) credentials.getPrivateKey();
        Algorithm algorithm = Algorithm.RSA256(null, privateKey);
        String jwt = JWT.create()
                .withIssuer(credentials.getAccount())
                .withAudience("https://oauth2.googleapis.com/token")
                .withClaim("scope", "https://www.googleapis.com/auth/firebase.messaging")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 3600 * 1000))
                .sign(algorithm);

        // Request access token
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
                .add("assertion", jwt)
                .build();
        Request request = new Request.Builder()
                .url("https://oauth2.googleapis.com/token")
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            String responseBody = null;
            if (response.body() != null) {
                responseBody = response.body().string();
            }

            // Parse and return the access token
            JSONObject jsonObject = new JSONObject(responseBody);
            return jsonObject.getString("access_token");
        } catch (IOException | JSONException e) {
            Log.e("AuthUtil", "Failed to obtain access token: " + e.getMessage());
            return null; // Handle the exception or return null
        }
    }
}

