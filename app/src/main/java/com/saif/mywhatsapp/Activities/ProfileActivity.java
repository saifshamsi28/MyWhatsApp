package com.saif.mywhatsapp.Activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.saif.mywhatsapp.AppDatabase;
import com.saif.mywhatsapp.DatabaseClient;
import com.saif.mywhatsapp.Models.User;
import com.saif.mywhatsapp.R;
import com.saif.mywhatsapp.databinding.ActivityProfileBinding;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {
    ActivityProfileBinding profileBinding;
    private final Executor executor= Executors.newSingleThreadExecutor();
    private final Handler handler=new Handler(Looper.getMainLooper());
    AppDatabase appDatabase;
    private User user;

    @Override
    protected void onResume() {
        this.setTitle("Profile");
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileBinding=ActivityProfileBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(profileBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        appDatabase= DatabaseClient.getInstance(this).getAppDatabase();
        profileBinding.contactName.setText(getIntent().getStringExtra("name"));

        String uid=getIntent().getStringExtra("uid");


        executor.execute(() -> {
            if(uid!=null) {
                user=appDatabase.userDao().getUserByUid(uid);
                profileBinding.aboutUser.setText(user.getAbout());
                profileBinding.contactName.setText(user.getName());
                String phoneNumber=user.getPhoneNumber();
                profileBinding.contactNumber.setText(phoneNumber.substring(0,3)+" "+phoneNumber.substring(3));
            }
        });
//        String profile= user.getProfileImage();
        if(getIntent().getStringExtra("imageUri")!=null)
                Glide.with(this).load(getIntent().getStringExtra("imageUri")).placeholder(R.drawable.avatar).into(profileBinding.profileImg);
        else
            Toast.makeText(this, "profile image is null", Toast.LENGTH_SHORT).show();

        profileBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ProfileActivity.this, ChatsActivity.class);
                intent.putExtra("chatUid",uid);
                intent.putExtra("source","ProfileActivity");
                startActivity(intent);
            }
        });
        if (setThemeForHomeScreen() == 2) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.GreenishBlue));
        } else {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
        }
        setThemeForHomeScreen();
    }

    private  int setThemeForHomeScreen() {
        int nightModeFlags = this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int color;
        int color2;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                color = ContextCompat.getColor(this, R.color.primaryTextColor); // White for dark mode
                color2 = ContextCompat.getColor(this, R.color.secondaryTextColor); // White for dark mode
                profileBinding.aboutUser.setTextColor(color2);
                profileBinding.contactName.setTextColor(color);
                return 1;
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
            default:
                color = ContextCompat.getColor(this, R.color.primaryTextColor); // Black for light mode
                color2 = ContextCompat.getColor(this, R.color.secondaryTextColor); // Black for light mode
                profileBinding.aboutUser.setTextColor(color2);
                profileBinding.contactName.setTextColor(color);
                return 2;
        }
    }
}