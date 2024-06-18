package com.saif.mywhatsapp.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.saif.mywhatsapp.AppDatabase;
import com.saif.mywhatsapp.DatabaseClient;
import com.saif.mywhatsapp.Models.User;
import com.saif.mywhatsapp.R;
import com.saif.mywhatsapp.databinding.ActivitySetUpProfileBinding;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SetUpProfileActivity extends AppCompatActivity {

    ActivitySetUpProfileBinding setUpProfileBinding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri selectedImg;
    ProgressDialog progressDialog;
    private static final int IMAGE_PICK_CODE = 28;
    AppDatabase appDatabase;
    User currentUser;
    private final Executor executor= Executors.newSingleThreadExecutor();
    private final Handler mainHandler=new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setUpProfileBinding = ActivitySetUpProfileBinding.inflate(getLayoutInflater());
        setContentView(setUpProfileBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Window window=getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.GreenishBlue));
        this.setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        appDatabase = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase();

        if(getIntent().getStringExtra("source")!=null && getIntent().getStringExtra("source").equals("MainActivity")){
            setUpProfileBinding.nameBox.setText(getIntent().getStringExtra("name"));
            setUpProfileBinding.aboutUser.setText(getIntent().getStringExtra("about"));
            Uri profileUri= Uri.parse(getIntent().getStringExtra("profileUri"));
//
//            setUpProfileBinding.nameBox.setText(currentUser.getName());
//            setUpProfileBinding.aboutUser.setText(currentUser.getAbout());
//            setUpProfileBinding.nameBox.setText(currentUser.getName());
            Glide.with(this).load(profileUri)
                    .placeholder(R.drawable.avatar)
                    .into(setUpProfileBinding.profileImg);
        }


        setUpProfileBinding.phone.setText(auth.getCurrentUser().getPhoneNumber().substring(0,3)
                +" "+auth.getCurrentUser().getPhoneNumber().substring(3));
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Setting Up profile...");
        progressDialog.setCancelable(false);

        setUpProfileBinding.profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent();
                intent1.setAction(Intent.ACTION_GET_CONTENT);
                intent1.setType("image/*");
                startActivityForResult(Intent.createChooser(intent1, "Select Picture"), IMAGE_PICK_CODE);
            }
        });

        setUpProfileBinding.updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = setUpProfileBinding.nameBox.getText().toString().trim();
                String about = setUpProfileBinding.aboutUser.getText().toString().trim();
//                if(getIntent().getStringExtra())
                String currentName = getIntent().getStringExtra("name");
                String currentAbout = getIntent().getStringExtra("about");
                Uri profileUri = null;
                if(getIntent().getStringExtra("profileUri")!=null)
                    profileUri= Uri.parse(getIntent().getStringExtra("profileUri"));

                boolean isNameChanged = !name.equals(currentName);
                boolean isAboutChanged = !about.equals(currentAbout);
                boolean isImageChanged = selectedImg != null;

                if (!isNameChanged && !isAboutChanged && !isImageChanged) {
                    // No changes detected, just return to MainActivity
                    Intent intent = new Intent(SetUpProfileActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }

                progressDialog.show();
                if (isImageChanged) {
                    StorageReference reference = storage.getReference().child("Profiles").child(name);
                    reference.putFile(selectedImg).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String imageUrl = uri.toString();
                                        updateUserProfile(name, about, imageUrl);
                                    }
                                });
                            }
                        }
                    });
                } else {
                    updateUserProfile(name, about, profileUri.toString());
                }
            }
        });
    }

    private void updateUserProfile(String name, String about, String imageUrl) {
        String uid = auth.getUid();
        String phone = auth.getCurrentUser().getPhoneNumber();
        if (about.length() == 0) {
            about = "Hey there I am using MyWhatsApp";
        }
        User user = new User(uid, name, phone, imageUrl, about);
        currentUser=user;

        executor.execute(() -> {
            appDatabase.userDao().insertUser(user);
        });

        database.getReference().child("Users")
                .child(uid)
                .setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(SetUpProfileActivity.this, "Profile SetUp successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SetUpProfileActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            selectedImg = data.getData();
            setUpProfileBinding.profileImg.setImageURI(selectedImg);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
