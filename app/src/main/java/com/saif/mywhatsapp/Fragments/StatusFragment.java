package com.saif.mywhatsapp.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.saif.mywhatsapp.Activities.MyStatusViewActivity;
import com.saif.mywhatsapp.Adapters.OthersStatusAdapter;
import com.saif.mywhatsapp.Database.AppDatabase;
import com.saif.mywhatsapp.Database.DatabaseClient;
import com.saif.mywhatsapp.Models.Status;
import com.saif.mywhatsapp.Models.User;
import com.saif.mywhatsapp.Models.UserStatus;
import com.saif.mywhatsapp.R;
import com.saif.mywhatsapp.StatusUpdateCallback;
import com.saif.mywhatsapp.databinding.FragmentStatusBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import omari.hamza.storyview.StoryView;

public class StatusFragment extends Fragment implements StatusUpdateCallback {
    private FragmentStatusBinding fragmentStatusBinding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private ProgressDialog progressDialog;
    private User user;
    private Context context;
    private OthersStatusAdapter othersStatusAdapter;
    private ArrayList<UserStatus> userStatuses = new ArrayList<>();
    private RecyclerView otherStatusRecyclerView;
    private static UserStatus myUserStatus;
    private boolean isUserOwnStatusUploaded = false;
    private User currentUser;
    private AppDatabase appDatabase;

    private final Executor executor= Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler();
    private StoryView.Builder builder;
    private Runnable statusUpdateRunnable;
    private long lastStatusTimestamp = -1;

    private final Runnable removeExpiredStatusesTask = new Runnable() {
        @Override
        public void run() {
            removeExpiredStatuses();
            handler.postDelayed(this, 60 * 1000); // Run every hour
        }
    };
    @Override
    public void onResume() {
        super.onResume();
        Activity activity=getActivity();
        activity.setTitle("Updates");
        refreshUserLayout();
        fetchStatuses();
    }
    private final ActivityResultLauncher<Intent> statusLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        // Save to local database first
                        Date date = new Date();
                        String userId = auth.getUid();  // Get the current user's ID
                        Status localStatus = new Status(userId, selectedImageUri.toString(), date.getTime());
                        localStatus.setLocal(true); // Custom flag to indicate local status
                        insertStatusToLocalDatabase(localStatus);
                        updateUIWithLocalStatus(localStatus);
                        // Upload to Firebase in background
                        uploadStatusToFirebase(selectedImageUri, date);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentStatusBinding = FragmentStatusBinding.inflate(inflater, container, false);
        return fragmentStatusBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        context = getContext();
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Uploading image...");
        progressDialog.setCancelable(false);

        otherStatusRecyclerView = fragmentStatusBinding.othersStatusRecyclerview;
        otherStatusRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        othersStatusAdapter = new OthersStatusAdapter(getContext(), userStatuses,this);
        otherStatusRecyclerView.setAdapter(othersStatusAdapter);

        appDatabase = DatabaseClient.getInstance(getContext()).getAppDatabase();

        fragmentStatusBinding.userOwnStatusItems.usersOwnStatusLayout.setOnClickListener(v -> {
            if (myUserStatus != null && !myUserStatus.getStatuses().isEmpty()) {
                if (isUserOwnStatusUploaded) {
                    Intent intent=new Intent(context, MyStatusViewActivity.class);
                    intent.putExtra("myStatuses",myUserStatus);
                    MyStatusViewActivity.setStatusUpdateCallback(this);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    statusLauncher.launch(intent);
                }
            } else {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                statusLauncher.launch(intent);
            }
        });
        fragmentStatusBinding.addNewStatus.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            statusLauncher.launch(intent);
        });

        fetchCurrentUser();
        fetchStatuses();
        sortUserStatuses();
        othersStatusAdapter.notifyDataSetChanged();
        setThemeForHomeScreen();
    }

    private void insertStatusToLocalDatabase(Status status) {
        new Thread(() -> {
            appDatabase.statusDao().insertStatus(status);
        }).start();
    }

    private void updateUIWithLocalStatus(Status status) {
        if (myUserStatus == null) {
            myUserStatus = new UserStatus();
            myUserStatus.setUserId(auth.getUid());
            myUserStatus.setName(currentUser.getName());
            myUserStatus.setProfileImage(currentUser.getProfileImage());
            myUserStatus.setStatuses(new ArrayList<>());
        }
        myUserStatus.getStatuses().add(status);
        isUserOwnStatusUploaded = true;
        setUserOwnStatus();
    }

    private void uploadStatusToFirebase(Uri selectedImageUri, Date date) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference reference = storage.getReference().child("status").child(date.getTime() + "");
        reference.putFile(selectedImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            UserStatus userStatus = new UserStatus();
                            if (user != null) {
                                userStatus.setUserId(user.getUid());
                                userStatus.setName(user.getName());
                                userStatus.setProfileImage(user.getProfileImage());
                            }
                            userStatus.setLastUpdated(date.getTime());

                            HashMap<String, Object> obj = new HashMap<>();
                            obj.put("name", userStatus.getName());
                            obj.put("profileImage", userStatus.getProfileImage());
                            obj.put("lastUpdated", userStatus.getLastUpdated());

                            String imageUri = uri.toString();
                            Status status = new Status(userStatus.getUserId(),imageUri, userStatus.getLastUpdated());

                            database.getReference().child("status")
                                    .child(auth.getUid())
                                    .updateChildren(obj);

                            database.getReference().child("status")
                                    .child(auth.getUid())
                                    .child("statuses")
                                    .push()
                                    .setValue(status);

                            // Update the local status with the Firebase URL
                            updateLocalStatusWithFirebaseUrl(status, imageUri);
                        }
                    });
                }
            }
        });
    }

    private void updateLocalStatusWithFirebaseUrl(Status localStatus, String firebaseUrl) {
        localStatus.setImageUrl(firebaseUrl);
        localStatus.setLocal(false); // Mark as non-local once uploaded
        new Thread(() -> {
            appDatabase.statusDao().updateStatus(localStatus);
        }).start();
    }

    private void sortUserStatuses() {
        Collections.sort(userStatuses, new Comparator<UserStatus>() {
            @Override
            public int compare(UserStatus userStatus1, UserStatus userStatus2) {
                return Long.compare(userStatus2.getLastUpdated(), userStatus1.getLastUpdated());
            }
        });
    }

    private void fetchCurrentUser() {
        new Thread(() -> {
            currentUser = appDatabase.userDao().getUserByUid(auth.getUid());
            if (currentUser != null) {
                getActivity().runOnUiThread(() -> {
                    Glide.with(context)
                            .asBitmap()
                            .load(currentUser.getProfileImage())
                            .placeholder(R.drawable.avatar)
                            .into(fragmentStatusBinding.userOwnStatusItems.myStatusImage);
                });
            }
        }).start();
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
                                    Status statusObj = status.getValue(Status.class);
                                    statuses.add(statusObj);
                                }
                                userStatus.setStatuses(statuses);

                                if (statusSnapshot.getKey().equals(FirebaseAuth.getInstance().getUid())) {
                                    myUserStatus = userStatus;
                                    isUserOwnStatusUploaded=true;
                                    setUserOwnStatus();
                                } else {
                                    userStatuses.add(userStatus);
                                }
                            }
                            sortUserStatuses();
                            refreshUserLayout();
                            othersStatusAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle the error
                    }
                });
    }

    private void refreshUserLayout() {
        if (myUserStatus != null && !myUserStatus.getStatuses().isEmpty()) {
            Glide.with(context)
                    .load(myUserStatus.getStatuses().get(myUserStatus.getStatuses().size() - 1).getImageUrl())
                    .placeholder(R.drawable.avatar)
                    .into(fragmentStatusBinding.userOwnStatusItems.myStatusImage);
            fragmentStatusBinding.userOwnStatusItems.add.setVisibility(View.GONE);
            fragmentStatusBinding.userOwnStatusItems.myCircularStatusView.setVisibility(View.VISIBLE);
//            fragmentStatusBinding.userOwnStatusItems.myCircularStatusView.setPortionsCount(myUserStatus.getStatuses().size());
//            Toast.makeText(context, "portion count is : "+myUserStatus.getStatuses().size(), Toast.LENGTH_SHORT).show();
        } else {
            // Load user's profile image instead
            if (currentUser != null && currentUser.getProfileImage() != null) {
                Glide.with(context)
                        .asBitmap()
                        .load(currentUser.getProfileImage())
                        .placeholder(R.drawable.avatar)
                        .into(fragmentStatusBinding.userOwnStatusItems.myStatusImage);
            } else {
                Glide.with(context)
                        .load(R.drawable.avatar)
                        .into(fragmentStatusBinding.userOwnStatusItems.myStatusImage);
            }
            fragmentStatusBinding.userOwnStatusItems.statusTime.setText("Tap to add status update");
            fragmentStatusBinding.userOwnStatusItems.add.setVisibility(View.VISIBLE);
            fragmentStatusBinding.userOwnStatusItems.myCircularStatusView.setVisibility(View.GONE);
        }
    }

    private void setUserOwnStatus() {
        if (myUserStatus != null && !myUserStatus.getStatuses().isEmpty()) {
            Status lastStatus = myUserStatus.getStatuses().get(myUserStatus.getStatuses().size() - 1);
            Glide.with(context)
                    .load(lastStatus.getImageUrl())
                    .placeholder(R.drawable.avatar)
                    .into(fragmentStatusBinding.userOwnStatusItems.myStatusImage);
            fragmentStatusBinding.userOwnStatusItems.add.setVisibility(View.GONE);
            fragmentStatusBinding.userOwnStatusItems.statusTime.setText("sending");
            fragmentStatusBinding.userOwnStatusItems.myCircularStatusView.setVisibility(View.VISIBLE);
            fragmentStatusBinding.userOwnStatusItems.myCircularStatusView.setPortionsCount(myUserStatus.getStatuses().size());
            //this is because portion count was not taking effect immediately
            fragmentStatusBinding.userOwnStatusItems.myCircularStatusView.invalidate();
            fragmentStatusBinding.userOwnStatusItems.myCircularStatusView.requestLayout();
            updateStatusTime(myUserStatus.getStatuses().get(myUserStatus.getStatuses().size()-1).getTimeStamps());
            refreshUserLayout();
        } else {
            // If there are no statuses, load the user's profile image
            if (context!=null && currentUser != null && currentUser.getProfileImage() != null) {
                Glide.with(this)
                        .asBitmap()
                        .load(currentUser.getProfileImage())
                        .placeholder(R.drawable.avatar)
                        .into(fragmentStatusBinding.userOwnStatusItems.myStatusImage);
            } else {
                Glide.with(this)
                        .load(R.drawable.avatar)
                        .into(fragmentStatusBinding.userOwnStatusItems.myStatusImage);
            }
            // Show the add button and hide the circular status view
            fragmentStatusBinding.userOwnStatusItems.add.setVisibility(View.VISIBLE);
            fragmentStatusBinding.userOwnStatusItems.myCircularStatusView.setVisibility(View.GONE);
        }
        isUserOwnStatusUploaded=true;
        refreshUserLayout();
    }

    private void updateStatusTime(long statusTime) {
        // to Remove any existing callback
        if (statusUpdateRunnable != null) {
            handler.removeCallbacks(statusUpdateRunnable);
        }

        statusUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (!myUserStatus.getStatuses().isEmpty()) {
                    String timeAgo = getTimeAgo(statusTime + "");
                    fragmentStatusBinding.userOwnStatusItems.statusTime.setText(timeAgo);

                    long currentTime = System.currentTimeMillis();
                    long diff = currentTime - statusTime;
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
                    if (minutes >= 60) {
                        handler.removeCallbacks(this); // Stop updating if more than 1 hour
                        String currentDate = formatStatusTime(new Date())[1];
                        String statusDate = formatStatusTime(new Date(statusTime))[1];
                        String lastStatusTime = formatStatusTime(new Date(statusTime))[0];
                        if (!currentDate.equals(statusDate)) {
                            lastStatusTime = "Yesterday";
                        }
                        fragmentStatusBinding.userOwnStatusItems.statusTime.setText(lastStatusTime);
                    } else {
                        handler.postDelayed(this, 60000); // Update every minute if less than 1 hour
                    }
                } else {
                    fragmentStatusBinding.userOwnStatusItems.statusTime.setText("Tap to add status update");
                    handler.removeCallbacks(this);
                }
            }
        };
        handler.post(statusUpdateRunnable);
    }

    private String getTimeAgo(String time) {
        long statusTime=Long.parseLong(time);
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - statusTime;

        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        if (minutes < 1) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " minutes ago";
        } else {
            String currentDate=formatStatusTime(new Date())[1];
            String statusDate=formatStatusTime(new Date(statusTime))[1];
            if(currentDate.equals(statusDate)){
                return "Today, "+formatStatusTime(new Date(statusTime))[0];
            }else {
                return "Yesterday, "+formatStatusTime(new Date(statusTime))[0];
            }
        }
    }

    private static final long STATUS_EXPIRY_DURATION = 3* 60 * 1000; // 24 hours in milliseconds

    private void removeExpiredStatuses() {
        long currentTime = System.currentTimeMillis();
        Log.e("removeExpireStatusesMethod","method calling at "+currentTime);
        for (UserStatus userStatus : userStatuses) {
            ArrayList<Status> validStatuses = new ArrayList<>();
            ArrayList<Status> expiredStatuses = new ArrayList<>();

            for (Status status : userStatus.getStatuses()) {
                if (currentTime - status.getTimeStamps() <= STATUS_EXPIRY_DURATION) {
                    validStatuses.add(status);
                } else {
                    expiredStatuses.add(status);
                }
            }

            for (Status status : expiredStatuses) {
                executor.execute(() -> {
                    // Remove from local database
                    appDatabase.statusDao().deleteStatusByTimeStamp(status.getTimeStamps());
                    Log.e("expired status","removed status from local database: "+status.getTimeStamps());

                    // Remove from Firebase
                    database.getReference().child("status")
                            .child(userStatus.getUserId())
                            .child("statuses")
                            .orderByChild("timeStamps")
                            .equalTo(status.getTimeStamps())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        for (DataSnapshot statusSnapshot : snapshot.getChildren()) {
                                            statusSnapshot.getRef().removeValue();
                                            Log.e("expired status","removed status from firebase database: "+status.getTimeStamps());
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                });
            }
            userStatus.setStatuses(validStatuses);
        }
        othersStatusAdapter.notifyDataSetChanged();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(removeExpiredStatusesTask);
    }

    private String[] formatStatusTime(Date date) {
        SimpleDateFormat formatTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                String statusTime = formatTime.format(date);
        SimpleDateFormat formatDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        String statusDate = formatDate.format(date);
        return new String[]{statusTime,statusDate};
    }

    private  void setThemeForHomeScreen() {
        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int color;
        int color2;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                color = ContextCompat.getColor(context, R.color.primaryTextColor); // White for dark mode
                color2 = ContextCompat.getColor(context, R.color.secondaryTextColor); // White for dark mode
                fragmentStatusBinding.addNewStatus.setSupportBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(),R.color.white)));
                break;
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
            default:
                color = ContextCompat.getColor(context, R.color.primaryTextColor); // Black for light mode
                color2 = ContextCompat.getColor(context, R.color.secondaryTextColor); // Black for light mode
                fragmentStatusBinding.addNewStatus.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.GreenishBlue));
                fragmentStatusBinding.addNewStatus.setImageTintList(ContextCompat.getColorStateList(getContext(),R.color.white));
                break;
        }
        fragmentStatusBinding.userOwnStatusItems.statusHeader.setTextColor(color);
        fragmentStatusBinding.userOwnStatusItems.myStatus.setTextColor(color);
        fragmentStatusBinding.userOwnStatusItems.statusTime.setTextColor(color2);
        fragmentStatusBinding.userOwnStatusItems.recent.setTextColor(color2);
        fragmentStatusBinding.addNewStatus.setBackgroundColor(Color.WHITE);
    }

    @Override
    public void onStatusUpdated(Status status1) {
        Log.e("call from Mystatus Adapter","removed status uploaded at "+status1.getTimeStamps());
        if (othersStatusAdapter != null) {
            // Notify the adapter about the data change
            othersStatusAdapter.notifyDataSetChanged();
        }
    }
}
