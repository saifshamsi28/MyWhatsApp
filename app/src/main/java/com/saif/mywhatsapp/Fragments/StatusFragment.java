package com.saif.mywhatsapp.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.saif.mywhatsapp.Activities.MainActivity;
import com.saif.mywhatsapp.Adapters.StatusAdapter;
import com.saif.mywhatsapp.Models.Status;
import com.saif.mywhatsapp.Models.User;
import com.saif.mywhatsapp.Models.UserStatus;
import com.saif.mywhatsapp.R;
import com.saif.mywhatsapp.databinding.FragmentStatusBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class StatusFragment extends Fragment {
    private FragmentStatusBinding fragmentStatusBinding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private ProgressDialog progressDialog;
    private User user;
    private Context context;
    private StatusAdapter statusAdapter;
    private ArrayList<UserStatus> userStatuses = new ArrayList<>();
    private RecyclerView otherStatusRecyclerView;
    private UserStatus myUserStatus;
    private boolean isUserOwnStatusUploaded=false;
    private String currentUserProfileUri =null;

    private Handler handler = new Handler();
    private Runnable removeExpiredStatusesTask = new Runnable() {
        @Override
        public void run() {
            removeExpiredStatuses();
            handler.postDelayed(this, 15000); // Run every hour
        }
    };

    private final ActivityResultLauncher<Intent> statusLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        progressDialog.show();
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        Date date = new Date();
                        StorageReference reference = storage.getReference().child("status").child(date.getTime() + "");
                        reference.putFile(selectedImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            progressDialog.dismiss();
                                            UserStatus userStatus = new UserStatus();
                                            if (user != null) {
                                                userStatus.setUserId(user.getUid());
                                                userStatus.setName(user.getName());
                                                userStatus.setProfileImage(user.getProfileImage());
                                            } else {
                                                Toast.makeText(requireContext(), "User is null", Toast.LENGTH_SHORT).show();
                                            }
                                            userStatus.setLastUpdated(date.getTime());

                                            HashMap<String, Object> obj = new HashMap<>();
                                            obj.put("name", userStatus.getName());
                                            obj.put("profileImage", userStatus.getProfileImage());
                                            obj.put("lastUpdated", userStatus.getLastUpdated());

                                            String imageUri = uri.toString();
                                            Status status = new Status(imageUri, userStatus.getLastUpdated());

                                            database.getReference().child("status")
                                                    .child(auth.getUid())
                                                    .updateChildren(obj);

                                            database.getReference().child("status")
                                                    .child(auth.getUid())
                                                    .child("statuses")
                                                    .push()
                                                    .setValue(status);
                                        }
                                    });
                                } else {
                                    progressDialog.dismiss();
                                    Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout using view binding
        fragmentStatusBinding = FragmentStatusBinding.inflate(inflater, container, false);
        return fragmentStatusBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        context=getContext();
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Uploading image...");
        progressDialog.setCancelable(false);

        otherStatusRecyclerView = fragmentStatusBinding.othersStatusRecyclerview;
        otherStatusRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        statusAdapter = new StatusAdapter(getContext(), userStatuses);
        otherStatusRecyclerView.setAdapter(statusAdapter);

//        Glide.with(context).load(user.getProfileImage()).into(fragmentStatusBinding.userOwnStatusItems.myStatusImage);
        handler.post(removeExpiredStatusesTask);
        removeExpiredStatuses();

        fragmentStatusBinding.userOwnStatusItems.usersOwnStatusLayout.setOnClickListener(v -> {
            if (myUserStatus != null && !myUserStatus.getStatuses().isEmpty()) {
                if(isUserOwnStatusUploaded)
                    showMyStatuses(myUserStatus);
            } else {
                // User has no statuses, launch the image picker
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                statusLauncher.launch(intent);
            }
        });

        fragmentStatusBinding.addNewStatus.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            statusLauncher.launch(intent);
        });

        // Fetch user data
        fetchCurrentUser();

        fetchStatuses();
        sortUserStatuses();

        // Notify adapter after sorting
        statusAdapter.notifyDataSetChanged();
    }

    private void sortUserStatuses() {
        Collections.sort(userStatuses, new Comparator<UserStatus>() {
            @Override
            public int compare(UserStatus userStatus1, UserStatus userStatus2) {
                Toast.makeText(requireContext(), "sorting the values", Toast.LENGTH_SHORT).show();
                return Long.compare(userStatus2.getLastUpdated(), userStatus1.getLastUpdated());
            }
        });
    }

    private void fetchCurrentUser(){
        database.getReference().child("Users").child(auth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        user = snapshot.getValue(User.class);
                        if (user != null && user.getProfileImage() != null) {
                            currentUserProfileUri =user.getProfileImage();
                            Glide.with(context)
                                    .asBitmap()
                                    .load(currentUserProfileUri)
                                    .placeholder(R.drawable.avatar)
                                    .into(fragmentStatusBinding.userOwnStatusItems.myStatusImage);
//                                    .into(new CustomTarget<Bitmap>() {
//                                        @Override
//                                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//                                            fragmentStatusBinding.userOwnStatusItems.myStatusImage.setImageBitmap(resource);
//                                            currentUserProfileUri=resource;
//                                        }
//                                        @Override
//                                        public void onLoadCleared(@Nullable Drawable placeholder) {
//                                            fragmentStatusBinding.userOwnStatusItems.myStatusImage.setImageResource(R.drawable.avatar);
//                                        }
//                                    });
                        } else {
                            Toast.makeText(requireContext(), "User profile is null", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle the error
                    }
                });
    }

    private void fetchStatuses() {
        database.getReference().child("status")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            userStatuses.clear();
                            for (DataSnapshot statusSnapshot : snapshot.getChildren()) {
                                UserStatus userStatus = new UserStatus();
                                userStatus.setUserId(statusSnapshot.getKey()); // Set userId
                                userStatus.setName(statusSnapshot.child("name").getValue(String.class));
                                userStatus.setProfileImage(statusSnapshot.child("profileImage").getValue(String.class));
                                userStatus.setLastUpdated(statusSnapshot.child("lastUpdated").getValue(Long.class));

                                ArrayList<Status> statuses = new ArrayList<>();
                                for (DataSnapshot status : statusSnapshot.child("statuses").getChildren()) {
                                    Status singleStatus = status.getValue(Status.class);
                                    statuses.add(singleStatus);
                                }
                                userStatus.setStatuses(statuses);
                                if(userStatus.getUserId().equals(user.getUid()) && userStatus.getStatuses()!=null){
                                    myUserStatus=userStatus;
                                    isUserOwnStatusUploaded=true;
                                    setUserOwnStatus(userStatus);
                                }else
                                    userStatuses.add(userStatus);
                            }
                        }
                        sortUserStatuses();
                        statusAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle the error
                    }
                });
    }

    private void setUserOwnStatus(UserStatus userStatus) {
        if (userStatus != null && !userStatus.getStatuses().isEmpty()) {
            Status lastStatus = userStatus.getStatuses().get(userStatus.getStatuses().size() - 1);
            Glide.with(context)
                    .load(lastStatus.getImageUrl())
                    .placeholder(R.drawable.avatar)
                    .into(fragmentStatusBinding.userOwnStatusItems.myStatusImage);
            fragmentStatusBinding.userOwnStatusItems.myCircularStatusView.setVisibility(View.VISIBLE);
            fragmentStatusBinding.userOwnStatusItems.add.setVisibility(View.GONE);
            fragmentStatusBinding.userOwnStatusItems.myCircularStatusView.setPortionsCount(userStatus.getStatuses().size());
            showMyStatuses(userStatus);

        }else {
            fragmentStatusBinding.userOwnStatusItems.myCircularStatusView.setVisibility(View.GONE);
            fragmentStatusBinding.userOwnStatusItems.add.setVisibility(View.VISIBLE);
            fragmentStatusBinding.userOwnStatusItems.statusTime.setText("Tap to add status update");
            Glide.with(context).load(currentUserProfileUri)
                    .placeholder(R.drawable.avatar)
                    .into(fragmentStatusBinding.userOwnStatusItems.myStatusImage);
            isUserOwnStatusUploaded=false;
        }
    }

    private void showMyStatuses(UserStatus userStatus) {
        if (userStatus.getStatuses() != null && !userStatus.getStatuses().isEmpty()) {
//                // Get the latest status
            Status lastStatus = userStatus.getStatuses().get(userStatus.getStatuses().size() - 1);
//
//                // Set the visibility and image for the latest status
//                binding.myImageLayout.setVisibility(View.VISIBLE);
//                binding.add.setVisibility(View.GONE);
//                binding.myCircularStatusView.setPortionsCount(userStatus.getStatuses().size());
//                Glide.with(context).load(lastStatus.getImageUrl()).into(binding.myStatusImage);
//                binding.myCircularStatusView.setVisibility(View.VISIBLE);
//
//                // Set the time for the latest status
            String []statusTimeAndDate=formatStatusTime(new Date(lastStatus.getTimeStamps()));

//            SimpleDateFormat formatTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            String statusTime = statusTimeAndDate[0];
//            SimpleDateFormat formatDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            String statusDate = statusTimeAndDate[1];
            if (statusTime.startsWith("0")) {
                statusTime = statusTime.substring(1);
            }
//
            String []currentTimeAndDate = formatStatusTime(new Date());
            String currentDate = currentTimeAndDate[1];
//            Log.e("current date",currentDate);
            if (currentDate.equals(statusDate)) {
                fragmentStatusBinding.userOwnStatusItems.statusTime.setText(statusTime);
            } else {
                fragmentStatusBinding.userOwnStatusItems.statusTime.setText("Yesterday");
            }
            String finalStatusTime = statusTime;
            fragmentStatusBinding.userOwnStatusItems.usersOwnStatusLayout.setOnClickListener(v -> showStoryView(userStatus, finalStatusTime, statusDate));
        } else {
            fragmentStatusBinding.userOwnStatusItems.myImageLayout.setVisibility(View.GONE);
        }
        setThemeForHomeScreen(fragmentStatusBinding.userOwnStatusItems.myStatus,fragmentStatusBinding.userOwnStatusItems.statusTime);
    }

    private String[] formatStatusTime(Date date) {
//        Date date = new Date(lastStatus.getTimeStamps());
        SimpleDateFormat formatTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String statusTime = formatTime.format(date);
        SimpleDateFormat formatDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        String statusDate = formatDate.format(date);

        return new String[]{statusTime,statusDate};
    }
    private void showStoryView(UserStatus userStatus,String statusTime,String statusDate) {
        ArrayList<MyStory> myStories = new ArrayList<>();
        for (Status story : userStatus.getStatuses()) {
            myStories.add(new MyStory(story.getImageUrl()));
        }
        SimpleDateFormat formatDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        String currentDate = formatDate.format(new Date());
        String thisStatusTime=null;
        if(currentDate.equals(statusDate)){
            thisStatusTime="Today, "+statusTime;
        }else {
            thisStatusTime="Yesterday, "+statusTime;
        }

        new StoryView.Builder(((MainActivity) getContext()).getSupportFragmentManager())
                .setStoriesList(myStories)
                .setStoryDuration(5000)
                .setTitleText(userStatus.getName())
                .setSubtitleText(thisStatusTime)
                .setTitleLogoUrl(userStatus.getProfileImage())
                .setStoryClickListeners(new StoryClickListeners() {
                    @Override
                    public void onDescriptionClickListener(int position) {
                        // Handle story description click
                    }

                    @Override
                    public void onTitleIconClickListener(int position) {
                        // Handle title icon click
                    }
                })
                .build()
                .show();
    }

    private void removeExpiredStatuses() {
        database.getReference().child("status").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long currentTime = System.currentTimeMillis();
                long expiryTime = 60000; // 20 minutes in milliseconds


                statusAdapter.notifyDataSetChanged();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    boolean hasValidStatuses = false;
                    for (DataSnapshot statusSnapshot : userSnapshot.child("statuses").getChildren()) {
                        Status status = statusSnapshot.getValue(Status.class);
                        if (status != null && (currentTime - status.getTimeStamps() > expiryTime)) {
                            database.getReference().child("status")
                                    .child(userSnapshot.getKey())
                                    .child("statuses")
                                    .child(statusSnapshot.getKey())
                                    .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            for (UserStatus userStatus : userStatuses) {
                                                ArrayList<Status> validStatuses = new ArrayList<>();
                                                for (Status status : userStatus.getStatuses()) {
                                                    if (currentTime <= status.getTimeStamps() + (expiryTime)) {
                                                        validStatuses.add(status);
                                                    }
                                                }
                                                userStatus.setStatuses(validStatuses);
                                            }
                                            // Update UI to reflect expired statuses removal
                                            if (myUserStatus != null) {
                                                ArrayList<Status> validStatuses = new ArrayList<>();
                                                for (Status status : myUserStatus.getStatuses()) {
                                                    if (currentTime <= status.getTimeStamps() + (expiryTime)) {
                                                        validStatuses.add(status);
                                                    }
                                                }
                                                myUserStatus.setStatuses(validStatuses);
                                                setUserOwnStatus(myUserStatus);
                                            }
                                            // Remove user statuses that are completely expired
                                            userStatuses.removeIf(userStatus -> userStatus.getStatuses().isEmpty());
                                        }
                                    });
                        } else {
                            hasValidStatuses = true;
                        }
                    }

                    if (!hasValidStatuses) {
                        database.getReference().child("status")
                                .child(userSnapshot.getKey())
                                .removeValue();
                    }
                }
//
//                fetchCurrentUser();
                fetchStatuses(); // Refresh statuses
                statusAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
            }
        });
    }

    private void refreshUserLayout() {
        isUserOwnStatusUploaded=false;
        if (myUserStatus != null && !myUserStatus.getStatuses().isEmpty()) {
            fragmentStatusBinding.userOwnStatusItems.add.setVisibility(View.GONE);
            fragmentStatusBinding.userOwnStatusItems.myCircularStatusView.setVisibility(View.VISIBLE);
        } else {
            fragmentStatusBinding.userOwnStatusItems.add.setVisibility(View.VISIBLE);
            fragmentStatusBinding.userOwnStatusItems.myCircularStatusView.setVisibility(View.GONE);
            Glide.with(context)
                    .asBitmap()
                    .load(currentUserProfileUri)
                    .placeholder(R.drawable.avatar)
                    .into(fragmentStatusBinding.userOwnStatusItems.myStatusImage);
        }
    }
    private  void setThemeForHomeScreen(TextView statusContactName, TextView statusTime) {
        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int color;
        int color2;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                color = ContextCompat.getColor(context, R.color.primaryTextColor); // White for dark mode
                color2 = ContextCompat.getColor(context, R.color.secondaryTextColor); // White for dark mode
                break;
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
            default:
                color = ContextCompat.getColor(context, R.color.primaryTextColor); // Black for light mode
                color2 = ContextCompat.getColor(context, R.color.secondaryTextColor); // Black for light mode
                break;
        }
        statusContactName.setTextColor(color);
        statusTime.setTextColor(color2);
        fragmentStatusBinding.userOwnStatusItems.statusHeader.setTextColor(color);
    }

    @Override
    public void onResume() {
        super.onResume();

        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle("Updates");
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(removeExpiredStatusesTask);
    }
}