package com.saif.mywhatsapp.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.saif.mywhatsapp.Database.AppDatabase;
import com.saif.mywhatsapp.Database.DatabaseClient;
import com.saif.mywhatsapp.Models.User;
import com.saif.mywhatsapp.MyWhatsAppPermissions;
import com.saif.mywhatsapp.R;
import com.saif.mywhatsapp.SupabaseAuthApi;
import com.saif.mywhatsapp.SupabaseClient;
import com.saif.mywhatsapp.databinding.ActivitySetUpProfileBinding;

import org.json.JSONObject;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetUpProfileActivity extends AppCompatActivity {

    private ActivitySetUpProfileBinding setUpProfileBinding;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private Uri selectedImg;
    private ProgressDialog progressDialog;
    private static final int IMAGE_PICK_CODE = 28;
    private AppDatabase appDatabase;
    private User currentUser;
    private final Uri defaultUri = Uri.parse("https://firebasestorage.googleapis.com/v0/b/mywhatsapp-2d301.appspot.com/o/avatar.png?alt=media&token=d1196659-20bc-4d9f-af24-ad2f74795faf");
    private final String defaultAbout = "Hey there i m using MyWhatsApp";
    private MyWhatsAppPermissions myWhatsAppPermissions;
    private SupabaseClient supabaseClient;
    private String supabaseUid;
    private String phoneNumber;

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
        currentUser=new User();
        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);



//        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        appDatabase = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase();
        myWhatsAppPermissions=new MyWhatsAppPermissions();


        Intent intent=getIntent();
        phoneNumber=intent.getStringExtra("Phone_number");
        String accessToken=intent.getStringExtra("access_token");

        supabaseClient = new SupabaseClient();
        fetchSupabaseUser(accessToken); // Fetch the user when activity starts

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

        setUpProfileBinding.phone.setText(phoneNumber.substring(0, 3)
                + " " + phoneNumber.substring(3));
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

//    private void updateProfile() {
//        String name = setUpProfileBinding.nameBox.getText().toString().trim();
//        String about = setUpProfileBinding.aboutUser.getText().toString().trim();
//        String currentName = getIntent().getStringExtra("name");
//        String currentAbout = getIntent().getStringExtra("about");
//        Uri profileUri = getIntent().getStringExtra("profileUri") != null ? Uri.parse(getIntent().getStringExtra("profileUri")) : null;
//
//        boolean isNameChanged = !name.equals(currentName);
//        boolean isAboutChanged = !about.equals(currentAbout);
//        boolean isImageChanged = selectedImg != null;
//
//        if (!isNameChanged && !isAboutChanged && !isImageChanged) {
//            Intent intent = new Intent(SetUpProfileActivity.this, MainActivity.class);
//            startActivity(intent);
//            finish();
//            return;
//        }
//
//        progressDialog.show();
//
//        if (isImageChanged) {
//            StorageReference reference = storage.getReference().child("Profiles").child(name);
//            reference.putFile(selectedImg).addOnCompleteListener(task -> {
//                if (task.isSuccessful()) {
//                    reference.getDownloadUrl().addOnSuccessListener(uri -> updateUserProfile(name, about, uri.toString()));
//                }
//            });
//        } else {
//            String imageUrl = profileUri != null ? profileUri.toString() : defaultUri.toString();
//            updateUserProfile(name, about, imageUrl);
//        }
//    }

//    private void updateUserProfile(String name, String about, String imageUrl) {
//        String uid = auth.getUid();
//        String phone = auth.getCurrentUser().getPhoneNumber();
//        if (about.isEmpty()) {
//            about = defaultAbout;
//        }
//        User user = new User(uid, name, phone, imageUrl, about, "online");
//        currentUser = user;
//
//        executor.execute(() -> appDatabase.userDao().insertUser(user));
//
//        database.getReference().child("Users").child(uid).setValue(user)
//                .addOnSuccessListener(unused -> {
//                    progressDialog.dismiss();
//                    Toast.makeText(SetUpProfileActivity.this, "Profile SetUp successfully", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(SetUpProfileActivity.this, SplashActivity.class);
//                    startActivity(intent);
//                    finish();
//                });
//    }

    // Fetch the authenticated user from Supabase
    private void fetchSupabaseUser(String accessToken) {
        Log.d("SetUpProfileActivity", "Fetching user from Supabase");

        if (accessToken == null || accessToken.isEmpty()) {
            Log.e("Supabase Error", "Access token is null or empty");
            return;
        }

        SupabaseClient supabaseClient = new SupabaseClient();
        SupabaseAuthApi authApi = supabaseClient.getAuthApi();

        Log.d("Supabase Access Token", "Token: Bearer " + accessToken);

        Call<ResponseBody> call = authApi.getUserRaw("Bearer " + accessToken);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String rawResponse = response.body().string();
                        Log.d("Supabase Raw Response", rawResponse);

                        JSONObject jsonObject = new JSONObject(rawResponse);
                        supabaseUid = jsonObject.getString("id");  // Extract UID
                        phoneNumber = jsonObject.getString("phone");  // Extract Phone Number

                        Log.d("Supabase User", "UID: " + supabaseUid + ", Phone: " + phoneNumber);

                        currentUser.setUid(supabaseUid);
                        currentUser.setPhoneNumber(phoneNumber);
                    } else {
                        Log.e("Supabase Error", "Response failed: " + response.errorBody().string());
                    }
                } catch (Exception e) {
                    Log.e("Supabase Error", "Exception while parsing response", e);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Supabase API Error", "Failed to fetch user", t);
            }
        });
    }

    private void updateProfile() {
        String name = setUpProfileBinding.nameBox.getText().toString().trim();
        String about = setUpProfileBinding.aboutUser.getText().toString().trim();
        boolean isImageChanged = selectedImg != null;

        //if user is null then return to previous activity
        if (supabaseUid == null || phoneNumber == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        if (isImageChanged) {
            // Upload image to Supabase storage if needed
            StorageReference reference = storage.getReference().child("Profiles").child(supabaseUid);
            reference.putFile(selectedImg).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    reference.getDownloadUrl().addOnSuccessListener(uri -> saveUserToFirebase(name, about, uri.toString()));
                }
            });
        } else {
            saveUserToFirebase(name, about, defaultUri.toString());
        }
    }

    // Store the user data in Supabase
    private void saveUserToFirebase(String name, String about, String profileImageUrl) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(supabaseUid);

        User user = new User(supabaseUid, name, phoneNumber, profileImageUrl, about, "online");

        databaseRef.setValue(user).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(SetUpProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SetUpProfileActivity.this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(SetUpProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                Log.e("Firebase Error", "Failed to update profile", task.getException());
            }
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
//
//package com.saif.mywhatsapp.Activities;
//
//import android.app.Activity;
//import android.app.ProgressDialog;
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.bumptech.glide.Glide;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageReference;
//import com.saif.mywhatsapp.Models.User;
//import com.saif.mywhatsapp.R;
//import com.saif.mywhatsapp.databinding.ActivitySetUpProfileBinding;
//
//import java.util.concurrent.Executor;
//import java.util.concurrent.Executors;
//
//public class SetUpProfileActivity extends AppCompatActivity {
//
//    private ActivitySetUpProfileBinding setUpProfileBinding;
//    private FirebaseDatabase database;
//    private FirebaseStorage storage;
//    private Uri selectedImg;
//    private ProgressDialog progressDialog;
//    private static final int IMAGE_PICK_CODE = 28;
//    private final Uri defaultUri = Uri.parse("https://firebasestorage.googleapis.com/v0/b/mywhatsapp-2d301.appspot.com/o/avatar.png?alt=media&token=d1196659-20bc-4d9f-af24-ad2f74795faf");
//    private final String defaultName = "Unknown User";
//    private final String defaultAbout = "Hey there! I'm using MyWhatsApp.";
//    private final Executor executor = Executors.newSingleThreadExecutor();
//    private final Handler mainHandler = new Handler(Looper.getMainLooper());
//
//    private String phoneNumber;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setUpProfileBinding = ActivitySetUpProfileBinding.inflate(getLayoutInflater());
//        setContentView(setUpProfileBinding.getRoot());
//
//        database = FirebaseDatabase.getInstance();
//        storage = FirebaseStorage.getInstance();
//        progressDialog = new ProgressDialog(this);
//        progressDialog.setMessage("Saving Profile...");
//        progressDialog.setCancelable(false);
//
//        // Get phone number from Intent (from OtpActivity)
//        phoneNumber = getIntent().getStringExtra("PhoneNumber");
//
//        if (phoneNumber == null || phoneNumber.isEmpty()) {
//            Toast.makeText(this, "Phone number is missing!", Toast.LENGTH_SHORT).show();
//            finish(); // Close activity if phone number is not available
//            return;
//        }
//
//        setUpProfileBinding.phone.setText(phoneNumber);
//
//        setUpProfileBinding.profileImg.setOnClickListener(v -> pickImageFromGallery());
//
//        setUpProfileBinding.updateProfile.setOnClickListener(v -> saveUserProfile());
//    }
//
//    private void pickImageFromGallery() {
//        Intent intent = new Intent(Intent.ACTION_PICK);
//        intent.setType("image/*");
//        startActivityForResult(intent, IMAGE_PICK_CODE);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
//            selectedImg = data.getData();
//            Glide.with(this).load(selectedImg).placeholder(R.drawable.avatar).into(setUpProfileBinding.profileImg);
//        }
//    }
//
//    private void saveUserProfile() {
//        String name = setUpProfileBinding.nameBox.getText().toString().trim();
//        String about = setUpProfileBinding.aboutUser.getText().toString().trim();
//        if (name.isEmpty()) {
//            name = defaultName;
//        }
//        if (about.isEmpty()) {
//            about = defaultAbout;
//        }
//
//        progressDialog.show();
//
//        if (selectedImg != null) {
//            StorageReference reference = storage.getReference().child("profile_images").child(phoneNumber + ".jpg");
//            reference.putFile(selectedImg).addOnCompleteListener(task -> {
//                if (task.isSuccessful()) {
//                    reference.getDownloadUrl().addOnSuccessListener(uri -> {
////                        saveToDatabase(name, about, uri.toString());
//                    });
//                } else {
//                    Toast.makeText(this, "Image Upload Failed", Toast.LENGTH_SHORT).show();
//                    progressDialog.dismiss();
//                }
//            });
//        } else {
////            saveToDatabase(name, about, defaultUri.toString());
//        }
//    }

//    private void saveToDatabase(String name, String about, String profileImageUrl) {
//        DatabaseReference userRef = database.getReference().child("Users").child(phoneNumber);
//
//        User user = new User(phoneNumber, name, about, profileImageUrl);
//        userRef.setValue(user).addOnCompleteListener(task -> {
//            progressDialog.dismiss();
//            if (task.isSuccessful()) {
//                Toast.makeText(this, "Profile Saved Successfully", Toast.LENGTH_SHORT).show();
//                startActivity(new Intent(this, MainActivity.class));
//                finish();
//            } else {
//                Toast.makeText(this, "Profile Save Failed", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//}
//
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
