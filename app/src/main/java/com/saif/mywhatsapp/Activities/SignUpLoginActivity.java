package com.saif.mywhatsapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.saif.mywhatsapp.R;
import com.saif.mywhatsapp.databinding.ActivitySignUpLoginBinding;

public class SignUpLoginActivity extends AppCompatActivity {
    ActivitySignUpLoginBinding signUpLoginBinding;
    String phoneNumberwithCountryCode;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        signUpLoginBinding =ActivitySignUpLoginBinding.inflate(getLayoutInflater());
        setContentView(signUpLoginBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Window window=getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.GreenishBlue));
        auth=FirebaseAuth.getInstance();
        if(auth.getCurrentUser()!=null){
            Intent intent=new Intent(SignUpLoginActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }

        this.setTitle("Login");
        signUpLoginBinding.continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SignUpLoginActivity.this,OtpActivity.class);
                phoneNumberwithCountryCode = signUpLoginBinding.phoneNumberBox.getText().toString().trim();

                if (phoneNumberwithCountryCode.length()==0){
                    Toast.makeText(SignUpLoginActivity.this, "Please enter a phone number with country code", Toast.LENGTH_SHORT).show();
                } else {
                    if(phoneNumberwithCountryCode.length()>3) {
                        if(!phoneNumberwithCountryCode.startsWith("+")) {
                            Toast.makeText(SignUpLoginActivity.this, "Invalid format please enter like this\n" +
                                    "eg-> +9187499384", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            String actualNumber = phoneNumberwithCountryCode.substring(3);
                            if (actualNumber.length() == 10) {
                                intent.putExtra("Phone_number", phoneNumberwithCountryCode);
                                startActivity(intent);
                            } else {
                                Toast.makeText(SignUpLoginActivity.this, "Invalid Phone number\nplease enter a 10 digit valid number", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    else{
                        if(!phoneNumberwithCountryCode.startsWith("+")) {
                            Toast.makeText(SignUpLoginActivity.this, "Invalid format please enter like this\n" +
                                    "eg-> +9187499384", Toast.LENGTH_SHORT).show();
                        }
                        else
                            Toast.makeText(SignUpLoginActivity.this,"Please enter 10 digit phone number also",Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
}