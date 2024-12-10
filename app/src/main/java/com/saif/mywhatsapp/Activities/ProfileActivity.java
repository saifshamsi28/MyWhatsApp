package com.saif.mywhatsapp.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.canhub.cropper.CropImage;
import com.google.firebase.auth.FirebaseAuth;
import com.saif.mywhatsapp.Database.AppDatabase;
import com.saif.mywhatsapp.Database.DatabaseClient;
import com.saif.mywhatsapp.Models.User;
import com.saif.mywhatsapp.R;
import com.saif.mywhatsapp.databinding.ActivityProfileBinding;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding profileBinding;
    private Uri imageUri;
    private Executor executor= Executors.newSingleThreadExecutor();
    private AppDatabase appDatabase;
    private  String uid;

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null && data.getData() != null) {
                                imageUri = data.getData();
                                startCropImageActivity(imageUri);
                            }
                        }
                    });

    private final ActivityResultLauncher<Intent> cropImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                String croppedImageUriString = data.getStringExtra("croppedImageUri");
                                if (croppedImageUriString != null) {
                                    Uri croppedImageUri = Uri.parse(croppedImageUriString);
                                    // Load cropped image into profileImageView using Glide or any other method
                                    Glide.with(this).load(croppedImageUri).placeholder(R.drawable.avatar).into(profileBinding.profileImg);

                                    // Save cropped image URI or handle as needed
                                    Toast.makeText(this, "Image updated successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.e("profile activity", "croppedImageUri is null ");
                                }
                            } else {
                                Log.e("profile activity", "data is null");
                            }
                        } else if (result.getResultCode() == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                            Toast.makeText(this, "Failed to crop image", Toast.LENGTH_SHORT).show();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileBinding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(profileBinding.getRoot());

        if(setThemeForHomeScreen()==1) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
        }else {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.GreenishBlue));
        }

        Animation expand = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        profileBinding.profileImg.startAnimation(expand);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        appDatabase= DatabaseClient.getInstance(this).getAppDatabase();
        Intent intent = getIntent();
        String imageUriString = intent.getStringExtra("imageUri");
        uid= intent.getStringExtra("uid");
        if (imageUriString != null && uid != null) {
            imageUri = Uri.parse(imageUriString);
            // Load image and other user data using uid
            Glide.with(this).load(imageUri).placeholder(R.drawable.avatar).into(profileBinding.profileImg);
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    User user=appDatabase.userDao().getUserByUid(uid);
                    profileBinding.contactName.setText(user.getName());
                    String phoneNumber=user.getPhoneNumber();
                    profileBinding.contactNumber.setText(phoneNumber.substring(0,phoneNumber.length()-10)
                    +" "+phoneNumber.substring(phoneNumber.length()-10));
                    profileBinding.aboutUser.setText(user.getAbout());
                }
            });

            // Fetch user data from Room database using uid
        } else {
            Log.e("ProfileActivity", "Image URI or UID is null");
        }
        // Handle image click to open image picker
        profileBinding.profileImg.setOnClickListener(v -> {
            if(uid.equals( FirebaseAuth.getInstance().getUid())){
                openImagePicker();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private void startCropImageActivity(Uri imageUri) {
        Intent intent = new Intent(this, CropImageActivity.class);
        intent.putExtra("imageUri", imageUri.toString());
        cropImageLauncher.launch(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent();
        intent.putExtra("userId", uid);
        setResult(Activity.RESULT_OK, intent);
        finish();
        return true;
    }


    @Override
    protected void onResume() {
        getSupportActionBar().setTitle("Profile");
        super.onResume();
    }

    private int setThemeForHomeScreen() {
        int nightModeFlags = this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int color;
        int color2;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                color = ContextCompat.getColor(this, R.color.primaryTextColor);
                color2 = ContextCompat.getColor(this, R.color.secondaryTextColor); // White for dark mode
                profileBinding.contactName.setTextColor(color);
                getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.night_color_background));
                profileBinding.contactNumber.setTextColor(color);
                profileBinding.aboutUser.setTextColor(color2);
                return 1;
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                color = ContextCompat.getColor(this, R.color.secondaryTextColor);
                color2 = ContextCompat.getColor(this, R.color.secondaryTextColor); // White for dark mode
                profileBinding.contactName.setTextColor(color);
                profileBinding.contactNumber.setTextColor(color);
                profileBinding.aboutUser.setTextColor(color2);
                getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.GreenishBlue));
                return 2;
        }
        return 0;
    }
}
