package com.saif.mywhatsapp.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.canhub.cropper.CropImage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.saif.mywhatsapp.Database.AppDatabase;
import com.saif.mywhatsapp.Database.DatabaseClient;
import com.saif.mywhatsapp.Models.User;
import com.saif.mywhatsapp.MyWhatsAppPermissions;
import com.saif.mywhatsapp.R;
import com.saif.mywhatsapp.databinding.ActivitySetUpProfileBinding;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SetUpProfileActivity extends AppCompatActivity {

    private ActivitySetUpProfileBinding setUpProfileBinding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private Uri selectedImg;
    private ProgressDialog progressDialog;
    private static final int IMAGE_PICK_CODE = 28;
    private AppDatabase appDatabase;
    private User currentUser;
    private final Uri defaultUri = Uri.parse("https://firebasestorage.googleapis.com/v0/b/mywhatsapp-2d301.appspot.com/o/avatar.png?alt=media&token=d1196659-20bc-4d9f-af24-ad2f74795faf");
    private final String defaultName = "Unknown User";
    private final String defaultAbout = "Hey there i m using MyWhatsApp";
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private MyWhatsAppPermissions myWhatsAppPermissions;

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null && data.getData() != null) {
                                selectedImg = data.getData();
                                startCropImageActivity(selectedImg);
                            }
                        }
                    });

    private final ActivityResultLauncher<Intent> cropImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                Uri croppedImageUri = Uri.parse(data.getStringExtra("croppedImageUri"));
                                if (croppedImageUri != null) {
                                    selectedImg = croppedImageUri;
                                    Glide.with(this).load(croppedImageUri).placeholder(R.drawable.avatar).into(setUpProfileBinding.profileImg);
                                }
                            } else if (result.getResultCode() == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                                Toast.makeText(this, "Failed to crop image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpProfileBinding = ActivitySetUpProfileBinding.inflate(getLayoutInflater());
        setContentView(setUpProfileBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if(setThemeForHomeScreen()==1) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
        }else {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.GreenishBlue));
        }
        setTitle("Profile");
        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        appDatabase = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase();
        myWhatsAppPermissions=new MyWhatsAppPermissions();


        if (getIntent().getStringExtra("source") != null) {
            if (Objects.equals(getIntent().getStringExtra("source"), "MainActivity")) {
                setUpProfileBinding.nameBox.setText(getIntent().getStringExtra("name"));
                setUpProfileBinding.aboutUser.setText(getIntent().getStringExtra("about"));
                Uri profileUri = defaultUri;
                if (getIntent().getStringExtra("profileUri") != null) {
                    profileUri = Uri.parse(getIntent().getStringExtra("profileUri"));
                }
                Glide.with(this).load(profileUri)
                        .placeholder(R.drawable.avatar)
                        .into(setUpProfileBinding.profileImg);
            }
        }

        setUpProfileBinding.phone.setText(auth.getCurrentUser().getPhoneNumber().substring(0, 3)
                + " " + auth.getCurrentUser().getPhoneNumber().substring(3));
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Setting Up profile...");
        progressDialog.setCancelable(false);

        setUpProfileBinding.profileImg.setOnClickListener(v -> {
            if(myWhatsAppPermissions.isStoragePermissionOk(this)){
                openImagePicker();
            }else {
                if (!myWhatsAppPermissions.isStoragePermissionOk(SetUpProfileActivity.this)) {
                    myWhatsAppPermissions.requestStoragePermission(SetUpProfileActivity.this);
                }
            }
        });

        setUpProfileBinding.updateProfile.setOnClickListener(v -> updateProfile());
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

    private void updateProfile() {
        String name = setUpProfileBinding.nameBox.getText().toString().trim();
        String about = setUpProfileBinding.aboutUser.getText().toString().trim();
        String currentName = getIntent().getStringExtra("name");
        String currentAbout = getIntent().getStringExtra("about");
        Uri profileUri = getIntent().getStringExtra("profileUri") != null ? Uri.parse(getIntent().getStringExtra("profileUri")) : null;

        boolean isNameChanged = !name.equals(currentName);
        boolean isAboutChanged = !about.equals(currentAbout);
        boolean isImageChanged = selectedImg != null;

        if (!isNameChanged && !isAboutChanged && !isImageChanged) {
            Intent intent = new Intent(SetUpProfileActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        progressDialog.show();

        if (isImageChanged) {
            StorageReference reference = storage.getReference().child("Profiles").child(name);
            reference.putFile(selectedImg).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    reference.getDownloadUrl().addOnSuccessListener(uri -> updateUserProfile(name, about, uri.toString()));
                }
            });
        } else {
            String imageUrl = profileUri != null ? profileUri.toString() : defaultUri.toString();
            updateUserProfile(name, about, imageUrl);
        }
    }

    private void updateUserProfile(String name, String about, String imageUrl) {
        String uid = auth.getUid();
        String phone = auth.getCurrentUser().getPhoneNumber();
        if (about.isEmpty()) {
            about = defaultAbout;
        }
        User user = new User(uid, name, phone, imageUrl, about, "online");
        currentUser = user;

        executor.execute(() -> appDatabase.userDao().insertUser(user));

        database.getReference().child("Users").child(uid).setValue(user)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(SetUpProfileActivity.this, "Profile SetUp successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SetUpProfileActivity.this, SplashActivity.class);
                    startActivity(intent);
                    finish();
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private int setThemeForHomeScreen() {
        int nightModeFlags = this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int color;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                color = ContextCompat.getColor(this, R.color.primaryTextColor);
                setUpProfileBinding.aboutUser.setTextColor(color);
                return 1;
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                color = ContextCompat.getColor(this, R.color.secondaryTextColor);
                setUpProfileBinding.aboutUser.setTextColor(color);
                return 2;
        }
        return 0;
    }
}
