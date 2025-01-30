package com.saif.mywhatsapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.saif.mywhatsapp.Models.User;
import com.saif.mywhatsapp.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (FirebaseAuth.getInstance().getCurrentUser() != null && getIntent().getExtras() != null) {
                String userId = getIntent().getExtras().getString("userId");
                if (userId != null) {
                    FirebaseDatabase.getInstance().getReference()
                            .child("Users")
                            .child(userId)
                            .get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        User user = task.getResult().getValue(User.class);

                                        Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                                        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                        startActivity(mainIntent);
                                        Intent intent = new Intent(SplashActivity.this, ChatsActivity.class);
                                        intent.putExtra("userId", userId);
                                        intent.putExtra("Contact_name", user.getName());
                                        intent.putExtra("chat_profile", user.getProfileImage());
                                        intent.putExtra("number", user.getPhoneNumber().toString());
                                        intent.putExtra("receiverFcmToken", user.getFcmToken());
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                }
            }else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(FirebaseAuth.getInstance().getCurrentUser()!=null) {
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }else {
                        Intent intent = new Intent(SplashActivity.this, SignUpLoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }, 1000);
        }
    }
}