package com.saif.mywhatsapp.Activities;
//package com.saif.mywhatsapp.Activities;
//
//import android.app.ProgressDialog;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.view.Window;
//import android.view.inputmethod.InputMethodManager;
//import android.widget.Toast;
//
//import androidx.activity.EdgeToEdge;
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.content.ContextCompat;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.FirebaseException;
//import com.google.firebase.auth.AuthResult;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.PhoneAuthCredential;
//import com.google.firebase.auth.PhoneAuthOptions;
//import com.google.firebase.auth.PhoneAuthProvider;
//import com.mukeshsolanki.OnOtpCompletionListener;
//import com.saif.mywhatsapp.R;
//import com.saif.mywhatsapp.databinding.ActivityOtpBinding;
//
//import java.util.Timer;
//import java.util.TimerTask;
//import java.util.concurrent.TimeUnit;
//
//public class OtpActivity extends AppCompatActivity {
//    ActivityOtpBinding otpBinding;
//    FirebaseAuth auth;
//    String verificationId,phoneNumber;//for testing otp
//
//    PhoneAuthProvider.ForceResendingToken resendingToken;
//    ProgressDialog progressDialog;
//    Long timeOutSeconds=60L;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        otpBinding = ActivityOtpBinding.inflate(getLayoutInflater());
//        setContentView(otpBinding.getRoot());
//        Window window = getWindow();
//        window.setStatusBarColor(ContextCompat.getColor(this, R.color.GreenishBlue));
//        auth=FirebaseAuth.getInstance();
//        progressDialog=new ProgressDialog(this);
//        progressDialog.setMessage("Sending OTP...");
//        progressDialog.setCancelable(false);
//
//        this.setTitle("Verify OTP");
//        phoneNumber = getIntent().getStringExtra("Phone_number");
//        sendOtp(phoneNumber,false);
//        otpBinding.enteredNumber.setText("Verify "+phoneNumber.substring(0,3)+" " + phoneNumber.substring(3));
//
//
//            progressDialog.show();
//        otpBinding.otpView.setOtpCompletionListener(new OnOtpCompletionListener() {
//            @Override
//            public void onOtpCompleted(String otp) {
//                if (verificationId != null) {
//                    if (!otp.isEmpty()) {
//                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
//                        signIn(credential);
//                    } else {
//                        Toast.makeText(OtpActivity.this, " OTP is null", Toast.LENGTH_SHORT).show();
//                    }
//                }else {
//                    Toast.makeText(OtpActivity.this, "verificationId is null", Toast.LENGTH_SHORT).show();
//                }
//            }
//                });
//
//        otpBinding.resendOtpBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                sendOtp(phoneNumber,true);
//            }
//        });
//    }
//
//    private void sendOtp(String phoneNumber, boolean isResend) {
//        resendOtpTimer();
//        progressDialog.setMessage("Sending OTP...");
//        progressDialog.setCancelable(false);
//        progressDialog.show(); // Show progress only after ensuring OTP is being sent
//
//        Log.d("OtpActivity", "Sending OTP to " + phoneNumber);
//
//        PhoneAuthOptions.Builder options = PhoneAuthOptions.newBuilder(auth)
//                .setPhoneNumber(phoneNumber)
//                .setTimeout(timeOutSeconds, TimeUnit.SECONDS)
//                .setActivity(OtpActivity.this)
//                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//                    @Override
//                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
//                        signIn(phoneAuthCredential);
//                        progressDialog.dismiss();
//                    }
//
//                    @Override
//                    public void onVerificationFailed(@NonNull FirebaseException e) {
//                        Toast.makeText(OtpActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
//                        Log.e("Verification Failed", "Error: " + e.getMessage(), e);
//                        progressDialog.dismiss();
//                    }
//
//                    @Override
//                    public void onCodeSent(@NonNull String verifyId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
//                        super.onCodeSent(verifyId, forceResendingToken);
//                        verificationId = verifyId;
//                        resendingToken = forceResendingToken;
//                        progressDialog.dismiss();
//                        Toast.makeText(OtpActivity.this, "OTP sent successfully", Toast.LENGTH_SHORT).show();
//
//                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                        if (inputMethodManager != null) {
//                            inputMethodManager.showSoftInput(otpBinding.otpView, InputMethodManager.SHOW_IMPLICIT);
//                        }
//                        otpBinding.otpView.requestFocus();
//                    }
//                });
//
//        if (isResend && resendingToken != null) {
//            PhoneAuthProvider.verifyPhoneNumber(options.setForceResendingToken(resendingToken).build());
//        } else {
//            PhoneAuthProvider.verifyPhoneNumber(options.build());
//        }
//    }
//
//    private void signIn(PhoneAuthCredential phoneAuthCredential) {
//        auth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(@NonNull Task<AuthResult> task) {
//                if (task.isSuccessful()) {
//                    Toast.makeText(OtpActivity.this, "Logged in", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(OtpActivity.this, SetUpProfileActivity.class);
//                    intent.putExtra("PhoneNumber", phoneNumber);
//                    startActivity(intent);
//                    finishAffinity();
//                } else {
//                    Toast.makeText(OtpActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                    Log.e("Login failed", "Error: " + task.getException().getMessage(), task.getException());
//                }
//            }
//        });
//    }
//
//    private void resendOtpTimer(){
//        otpBinding.resendOtpBtn.setEnabled(false);
//        otpBinding.resendTimer.setVisibility(View.VISIBLE);
//        Timer timer=new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                timeOutSeconds--;
//                otpBinding.resendTimer.setText("Resend OTP in "+timeOutSeconds+" seconds");
//                if(timeOutSeconds<=0){
//                    timeOutSeconds=60L;
//                    timer.cancel();
//                    runOnUiThread(new TimerTask() {
//                        @Override
//                        public void run() {
//                            otpBinding.resendOtpBtn.setEnabled(true);
//                            otpBinding.resendTimer.setVisibility(View.GONE);
//                        }
//                    });
//
//                }
//            }
//        },0,1000);
//
//    }
//}

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.saif.mywhatsapp.Models.SendOtpRequest;
import com.saif.mywhatsapp.Models.SendOtpResponse;
import com.saif.mywhatsapp.Models.VerifyOtpRequest;
import com.saif.mywhatsapp.Models.VerifyOtpResponse;
import com.saif.mywhatsapp.SupabaseAuthApi;
import com.saif.mywhatsapp.SupabaseClient;
import com.saif.mywhatsapp.databinding.ActivityOtpBinding;

import java.io.IOException;

import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//public class OtpActivity extends AppCompatActivity {
//
//    ActivityOtpBinding otpBinding;
//    private SupabaseClient supabaseClient;
//    private String phoneNumber;
//    private String otpCode;
//    private ProgressDialog progressDialog;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        otpBinding = ActivityOtpBinding.inflate(getLayoutInflater());
//        setContentView(otpBinding.getRoot());
//
//        phoneNumber = getIntent().getStringExtra("Phone_number");
//        Log.d("OtpActivity", "Received phone number: " + phoneNumber);
//        supabaseClient = new SupabaseClient();
//        progressDialog = new ProgressDialog(this);
//
//        sendOtp(phoneNumber);
//
//        otpBinding.otpView.setOtpCompletionListener(otp -> {
//            otpCode = otp;
//            verifyOtp(phoneNumber, otpCode);
//        });
//    }
//
//    private void sendOtp(String phoneNumber) {
//        progressDialog.setMessage("Sending OTP...");
//        progressDialog.show();
//
//        SendOtpRequest request = new SendOtpRequest(phoneNumber);
//
//        SupabaseAuthApi api = supabaseClient.getAuthApi();
//        api.sendOtp(request).enqueue(new Callback<SendOtpResponse>() {
//            @Override
//            public void onResponse(Call<SendOtpResponse> call, Response<SendOtpResponse> response) {
//                progressDialog.dismiss();
//                if (response.isSuccessful()) {
//                    Toast.makeText(OtpActivity.this, "OTP Sent", Toast.LENGTH_SHORT).show();
//                    //log the otp sent
//                    Log.d("OTP Sent", "OTP sent successfully");
//                    if (response.body() != null) {
//                        SendOtpResponse otpResponse = response.body();
//                        Log.d("OTP Sent", "OTP: " + otpResponse.getOtp_sent());
//                    }else {
//                        Log.d("OTP Sent", "Response body is null");
//                    }
//                } else {
//                    try {
//                        String errorBody = response.errorBody().string();
//                        Log.e("OTP Error", "Error: " + response.message());
//                        Log.e("OTP Error", "Response Body: " + errorBody);
//                    } catch (Exception e) {
//                        Log.e("OTP Error", "Error reading the response body: " + e.getMessage());
//                    }
//                    Toast.makeText(OtpActivity.this, "Error Sending OTP", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<SendOtpResponse> call, Throwable t) {
//                progressDialog.dismiss();
//                Toast.makeText(OtpActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//
//
//
//    private void verifyOtp(String phoneNumber, String otpCode) {
//        progressDialog.setMessage("Verifying OTP...");
//        progressDialog.show();
//
//        VerifyOtpRequest request = new VerifyOtpRequest(phoneNumber, otpCode);
//
//        SupabaseAuthApi api = supabaseClient.getAuthApi();
//        api.verifyOtp(request).enqueue(new Callback<VerifyOtpResponse>() {
//            @Override
//            public void onResponse(Call<VerifyOtpResponse> call, Response<VerifyOtpResponse> response) {
//                progressDialog.dismiss();
//                if (response.isSuccessful()) {
//                    Toast.makeText(OtpActivity.this, "OTP Verified", Toast.LENGTH_SHORT).show();
//                    Log.d("OTP Verified", "OTP verified successfully");
////                    Headers headers = response.headers();
////                    Log.d("OTP Verified", "Response Headers: " + headers);
////                    try {
////                        String rawResponse = response.body() != null ? new Gson().toJson(response.body()) : response.errorBody().string();
////                        Log.d("OTP Verified", "Raw Response Body: " + rawResponse);
////                    } catch (IOException e) {
////                        e.printStackTrace();
////                    }
//                    Intent intent=new Intent(OtpActivity.this, SetUpProfileActivity.class);
//                    intent.putExtra("Phone_number",phoneNumber);
//                    startActivity(intent);
//                    finishAffinity();
//                } else {
//                    Toast.makeText(OtpActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<VerifyOtpResponse> call, Throwable t) {
//                progressDialog.dismiss();
//                Toast.makeText(OtpActivity.this, "Verification Failed", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//}

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.saif.mywhatsapp.Models.SendOtpRequest;
import com.saif.mywhatsapp.Models.SendOtpResponse;
import com.saif.mywhatsapp.Models.VerifyOtpRequest;
import com.saif.mywhatsapp.Models.VerifyOtpResponse;
import com.saif.mywhatsapp.databinding.ActivityOtpBinding;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpActivity extends AppCompatActivity {

    private ActivityOtpBinding otpBinding;
    private ProgressDialog progressDialog;
    private SupabaseClient supabaseClient;
    private String phoneNumber, otpCode;
    private Long timeOutSeconds = 60L; // Timeout for resending OTP

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        otpBinding = ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(otpBinding.getRoot());

        phoneNumber = getIntent().getStringExtra("Phone_number");
        Log.d("OtpActivity", "Received phone number: " + phoneNumber);

        supabaseClient = new SupabaseClient();
        progressDialog = new ProgressDialog(this);

        sendOtp(phoneNumber, false);

        otpBinding.otpView.setOtpCompletionListener(otp -> {
            otpCode = otp;
            verifyOtp(phoneNumber, otpCode);
        });

        otpBinding.resendOtpBtn.setOnClickListener(v -> sendOtp(phoneNumber, true));
    }

    private void sendOtp(String phoneNumber, boolean isResend) {
        if (isResend) {
            otpBinding.resendOtpBtn.setEnabled(false);
            startResendOtpTimer();
        }

        progressDialog.setMessage("Sending OTP...");
        progressDialog.show();

        SendOtpRequest request = new SendOtpRequest(phoneNumber);
        SupabaseAuthApi api = supabaseClient.getAuthApi();

        api.sendOtp(request).enqueue(new Callback<SendOtpResponse>() {
            @Override
            public void onResponse(Call<SendOtpResponse> call, Response<SendOtpResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    Toast.makeText(OtpActivity.this, "OTP Sent successfully", Toast.LENGTH_SHORT).show();
                    Log.d("OTP Sent", "OTP sent successfully");

                    if (response.body() != null) {
                        Log.d("OTP Sent", "OTP Response: " + response.body().getOtp_sent());
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("OTP Error", "Error: " + response.message());
                        Log.e("OTP Error", "Response Body: " + errorBody);
                    } catch (IOException e) {
                        Log.e("OTP Error", "Error reading the response body: " + e.getMessage());
                    }
                    Toast.makeText(OtpActivity.this, "Error Sending OTP", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SendOtpResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(OtpActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyOtp(String phoneNumber, String otpCode) {
        progressDialog.setMessage("Verifying OTP...");
        progressDialog.show();

        VerifyOtpRequest request = new VerifyOtpRequest(phoneNumber, otpCode);
        SupabaseAuthApi api = supabaseClient.getAuthApi();

        api.verifyOtp(request).enqueue(new Callback<VerifyOtpResponse>() {
            @Override
            public void onResponse(Call<VerifyOtpResponse> call, Response<VerifyOtpResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    Toast.makeText(OtpActivity.this, "OTP Verified", Toast.LENGTH_SHORT).show();
                    Log.d("OTP Verified", "OTP verified successfully");

                    Intent intent = new Intent(OtpActivity.this, SetUpProfileActivity.class);
                    intent.putExtra("Phone_number", phoneNumber);
                    startActivity(intent);
                    finishAffinity();
                } else {
                    Toast.makeText(OtpActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<VerifyOtpResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(OtpActivity.this, "Verification Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startResendOtpTimer() {
        otpBinding.resendOtpBtn.setEnabled(false);
        otpBinding.resendTimer.setVisibility(View.VISIBLE);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    timeOutSeconds--;
                    otpBinding.resendTimer.setText("Resend OTP in " + timeOutSeconds + " seconds");

                    if (timeOutSeconds <= 0) {
                        timeOutSeconds = 60L;
                        timer.cancel();
                        otpBinding.resendOtpBtn.setEnabled(true);
                        otpBinding.resendTimer.setVisibility(View.GONE);
                    }
                });
            }
        }, 0, 1000);
    }
}

