package com.saif.mywhatsapp.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import com.saif.mywhatsapp.R;
import com.saif.mywhatsapp.Models.User;
import com.saif.mywhatsapp.databinding.ActivitySetUpProfileBinding;

public class SetUpProfileActivity extends AppCompatActivity {

    ActivitySetUpProfileBinding setUpProfileBinding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri selectedImg;
    ProgressDialog progressDialog;
    private static final int IMAGE_PICK_CODE = 28;

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

        if(getIntent().getStringExtra("source").equals("MainActivity")){
            setUpProfileBinding.nameBox.setText(getIntent().getStringExtra("name"));
            setUpProfileBinding.aboutUser.setText(getIntent().getStringExtra("about"));
            Uri profileUri= Uri.parse(getIntent().getStringExtra("profileUri"));
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
                if (name.isEmpty()) {
                    setUpProfileBinding.nameBox.setError("Please enter your name");
                    return;
                }
                progressDialog.show();
                if (selectedImg != null) {
                    StorageReference reference = storage.getReference().child("Profiles").child(name);
                    reference.putFile(selectedImg).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String imageUrl = uri.toString();
                                        String uid = auth.getUid();
                                        String phone = auth.getCurrentUser().getPhoneNumber();
                                        String about= setUpProfileBinding.aboutUser.getText().toString().trim();
                                        if(about.length()==0){
                                            about="Hey there i m using MyWhatsApp";
                                        }
                                        User user = new User(uid, name, phone, imageUrl,about);
                                        database.getReference().child("Users")
                                                .child(uid)
                                                .setValue(user)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(SetUpProfileActivity.this, "Profile SetUp successfully", Toast.LENGTH_SHORT).show();
                                                        Intent intent1 = new Intent(SetUpProfileActivity.this, MainActivity.class);
                                                        startActivity(intent1);
                                                        finish();
                                                    }
                                                });
                                    }
                                });
                            }
                        }
                    });
                }else {
                    String uid = auth.getUid();
                    String phone = auth.getCurrentUser().getPhoneNumber();
                    String about= setUpProfileBinding.aboutUser.getText().toString().trim();
                    if(about.length()==0){
                        about="Hey there i m using MyWhatsApp";
                    }
                    User user = new User(uid, name, phone, "No image",about);
                    database.getReference().child("Users")
                            .child(uid)
                            .setValue(user)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    progressDialog.dismiss();
                                    Toast.makeText(SetUpProfileActivity.this, "Profile SetUp successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent1 = new Intent(SetUpProfileActivity.this, MainActivity.class);
                                    startActivity(intent1);
                                    finish();
                                }
                            });
                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            if (data.getData() != null) {
                selectedImg = data.getData();
                setUpProfileBinding.profileImg.setImageURI(selectedImg);
            }
        }
    }
}
