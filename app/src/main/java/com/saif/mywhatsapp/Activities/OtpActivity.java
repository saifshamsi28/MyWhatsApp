package com.saif.mywhatsapp.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.mukeshsolanki.OnOtpCompletionListener;
import com.saif.mywhatsapp.R;
import com.saif.mywhatsapp.databinding.ActivityOtpBinding;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class OtpActivity extends AppCompatActivity {
    ActivityOtpBinding otpBinding;
    FirebaseAuth auth;
    String verificationId,phoneNumber;//for testing otp

    PhoneAuthProvider.ForceResendingToken resendingToken;
    ProgressDialog progressDialog;
    Long timeOutSeconds=60L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        otpBinding = ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(otpBinding.getRoot());
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.GreenishBlue));
        auth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Sending OTP...");
        progressDialog.setCancelable(false);

        this.setTitle("Verify OTP");
        phoneNumber = getIntent().getStringExtra("Phone_number");
        sendOtp(phoneNumber,false);
        otpBinding.enteredNumber.setText("Verify "+phoneNumber.substring(0,3)+" " + phoneNumber.substring(3));


            progressDialog.show();
        otpBinding.otpView.setOtpCompletionListener(new OnOtpCompletionListener() {
            @Override
            public void onOtpCompleted(String otp) {
                if (verificationId != null) {
                    if (!otp.isEmpty()) {
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
                        signIn(credential);
                    } else {
                        Toast.makeText(OtpActivity.this, " OTP is null", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(OtpActivity.this, "verificationId is null", Toast.LENGTH_SHORT).show();
                }
            }
                });

        otpBinding.resendOtpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendOtp(phoneNumber,true);
            }
        });
    }

    private void sendOtp(String phoneNumber, boolean isResend) {
        resendOtpTimer();
        progressDialog.setMessage("Sending OTP...");
        progressDialog.setCancelable(false);
        progressDialog.show(); // Show progress only after ensuring OTP is being sent

        Log.d("OtpActivity", "Sending OTP to " + phoneNumber);

        PhoneAuthOptions.Builder options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(timeOutSeconds, TimeUnit.SECONDS)
                .setActivity(OtpActivity.this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        signIn(phoneAuthCredential);
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(OtpActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("Verification Failed", "Error: " + e.getMessage(), e);
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verifyId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verifyId, forceResendingToken);
                        verificationId = verifyId;
                        resendingToken = forceResendingToken;
                        progressDialog.dismiss();
                        Toast.makeText(OtpActivity.this, "OTP sent successfully", Toast.LENGTH_SHORT).show();

                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (inputMethodManager != null) {
                            inputMethodManager.showSoftInput(otpBinding.otpView, InputMethodManager.SHOW_IMPLICIT);
                        }
                        otpBinding.otpView.requestFocus();
                    }
                });

        if (isResend && resendingToken != null) {
            PhoneAuthProvider.verifyPhoneNumber(options.setForceResendingToken(resendingToken).build());
        } else {
            PhoneAuthProvider.verifyPhoneNumber(options.build());
        }
    }

    private void signIn(PhoneAuthCredential phoneAuthCredential) {
        auth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(OtpActivity.this, "Logged in", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(OtpActivity.this, SetUpProfileActivity.class);
                    intent.putExtra("PhoneNumber", phoneNumber);
                    startActivity(intent);
                    finishAffinity();
                } else {
                    Toast.makeText(OtpActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Login failed", "Error: " + task.getException().getMessage(), task.getException());
                }
            }
        });
    }

    private void resendOtpTimer(){
        otpBinding.resendOtpBtn.setEnabled(false);
        otpBinding.resendTimer.setVisibility(View.VISIBLE);
        Timer timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timeOutSeconds--;
                otpBinding.resendTimer.setText("Resend OTP in "+timeOutSeconds+" seconds");
                if(timeOutSeconds<=0){
                    timeOutSeconds=60L;
                    timer.cancel();
                    runOnUiThread(new TimerTask() {
                        @Override
                        public void run() {
                            otpBinding.resendOtpBtn.setEnabled(true);
                            otpBinding.resendTimer.setVisibility(View.GONE);
                        }
                    });

                }
            }
        },0,1000);

    }
}