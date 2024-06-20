package com.saif.mywhatsapp.Activities;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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
        String phoneNumber=getIntent().getStringExtra("number");
        String uid=getIntent().getStringExtra("uid");
        profileBinding.contactNumber.setText(phoneNumber.substring(0,3)+" "+phoneNumber.substring(3));
//        profileBinding.aboutUser.setText();
        executor.execute(() -> {
            if(uid!=null) {
                User user=appDatabase.userDao().getUserByUid(uid);
                profileBinding.aboutUser.setText(user.getAbout());
            }
        });
        String image=getIntent().getStringExtra("imageUri");
        if(image!=null){
            Uri imageURI= Uri.parse(image);
            Glide.with(this).load(imageURI).placeholder(R.drawable.avatar).into(profileBinding.profileImg);
        }else {
            Toast.makeText(this, "Image URI is null", Toast.LENGTH_SHORT).show();
        }

        profileBinding.backBtn.setOnClickListener(v -> finish());
    }
}