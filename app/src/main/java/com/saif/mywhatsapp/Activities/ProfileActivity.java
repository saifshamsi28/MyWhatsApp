package com.saif.mywhatsapp.Activities;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.saif.mywhatsapp.R;
import com.saif.mywhatsapp.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {
    ActivityProfileBinding profileBinding;

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
        profileBinding.contactName.setText(getIntent().getStringExtra("name"));
        String phoneNumber=getIntent().getStringExtra("number");
        profileBinding.contactNumber.setText(phoneNumber.substring(0,3)+" "+phoneNumber.substring(3));
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