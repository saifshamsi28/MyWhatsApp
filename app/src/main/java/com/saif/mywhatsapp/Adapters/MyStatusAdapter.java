package com.saif.mywhatsapp.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.saif.mywhatsapp.Activities.MyStatusViewActivity;
import com.saif.mywhatsapp.Database.AppDatabase;
import com.saif.mywhatsapp.Database.DatabaseClient;
import com.saif.mywhatsapp.Models.Status;
import com.saif.mywhatsapp.Models.User;
import com.saif.mywhatsapp.Models.UserStatus;
import com.saif.mywhatsapp.R;
import com.saif.mywhatsapp.StatusUpdateCallback;
import com.saif.mywhatsapp.databinding.MyStatusesLayoutBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.OnStoryChangedCallback;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class MyStatusAdapter extends RecyclerView.Adapter<MyStatusAdapter.MyStatusViewHolder> {
    private Context context;
    private UserStatus statuses;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private StoryView.Builder builder;
    private AppDatabase appDatabase;
    private final Executor executor= Executors.newSingleThreadExecutor();
    private User currentUser;
    private FirebaseDatabase database;
    private StatusUpdateCallback statusUpdateCallback;

    public MyStatusAdapter(Context context, UserStatus statuses, StatusUpdateCallback statusUpdateCallback) {
        this.context = context;
        this.statuses = statuses;
        appDatabase= DatabaseClient.getInstance(context).getAppDatabase();
        database=FirebaseDatabase.getInstance();
        this.statusUpdateCallback=statusUpdateCallback;
    }

    @NonNull
    @Override
    public MyStatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        MyStatusesLayoutBinding binding = MyStatusesLayoutBinding.inflate(inflater, parent, false);
        return new MyStatusViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyStatusViewHolder holder, int position) {
        Status status = statuses.getStatuses().get(position);
        holder.binding.statusViews.setText("0 views");
        Glide.with(context).load(status.getImageUrl()).into(holder.binding.statusImage);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                currentUser=appDatabase.userDao().getUserByUid(statuses.getUserId());
            }
        });

            updateStatusTime(holder,status.getTimeStamps());

        holder.binding.imageAndViewsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMyStatuses(statuses,position);
            }
        });
        holder.binding.deleteMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu=new PopupMenu(context,holder.binding.deleteMenu);
                MenuInflater menuInflater= popupMenu.getMenuInflater();
                menuInflater.inflate(R.menu.menu_delete,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.delete) {
                            Dialog dialog=new Dialog(context);
                            dialog.setContentView(R.layout.logout_confirmation_dialog);
                            dialog.getWindow().setBackgroundDrawable(null);
                            TextView textView_header=dialog.findViewById(R.id.header);
                            TextView textView_delete=dialog.findViewById(R.id.confirm);
                            textView_header.setText("Confirm delete 1 status update ?\nThis action will not revert");
                            dialog.findViewById(R.id.account_name).setVisibility(View.GONE);
                            textView_delete.setText("Delete");
                            dialog.show();
                            dialog.findViewById(R.id.cancel).setOnClickListener(v1 -> dialog.dismiss());
                            textView_delete.setOnClickListener(v1 -> {
                            Toast.makeText(context, "Deleting status", Toast.LENGTH_SHORT).show();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                            //deleting from local database
                                            appDatabase.statusDao().deleteStatusByTimeStamp(status.getTimeStamps());
                                    mainHandler.post(() -> {
                                        //Deleting from Firebase Realtime Database
                                        if(statuses.getUserId() != null){
                                            database.getReference("status")
                                                    .child(statuses.getUserId()).child("statuses").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            for (DataSnapshot keySnapshot : snapshot.getChildren()) {
                                                                Status status1 = keySnapshot.getValue(Status.class);
                                                                if (status1 != null && status1.getTimeStamps() == status.getTimeStamps()) {
                                                                    database.getReference().child("status")
                                                                            .child(statuses.getUserId())
                                                                            .child("statuses")
                                                                            .child(Objects.requireNonNull(keySnapshot.getKey())).removeValue()
                                                                            .addOnCompleteListener(task -> {
                                                                                if (task.isSuccessful()) {
                                                                                    // Deleting from Firebase Storage
                                                                                    if (status.getImageUrl() != null) {
                                                                                        Log.e("url", "status url" + status.getImageUrl());
                                                                                        try {
                                                                                            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(status.getImageUrl());
                                                                                            storageReference.delete().addOnCompleteListener(storageTask -> {
                                                                                                if (storageTask.isSuccessful()) {
                                                                                                    Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                                                                                                } else {
                                                                                                    Toast.makeText(context, "Failed to delete from the status", Toast.LENGTH_SHORT).show();
                                                                                                }
                                                                                            });
                                                                                        } catch (IllegalArgumentException e) {
                                                                                            Toast.makeText(context, "Invalid image URL", Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    } else {
                                                                                        Log.e("url", "status url is not found" + status.getImageUrl());
                                                                                    }
                                                                                } else {
                                                                                    Log.e("Firebase Error", "Failed to delete status from Firebase Realtime Database");
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        }
                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {
                                                            Toast.makeText(context, "Error fetching status from Firebase", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        } else {
                                            Log.e("user check", "Error: User ID is null");
                                        }

                                        Status status1=statuses.getStatuses().remove(position);
                                        notifyItemRemoved(position);

                                        if(statusUpdateCallback!=null){
                                            statusUpdateCallback.onStatusUpdated(status1);
                                        }

                                        if(statuses.getStatuses().isEmpty()){
                                            ((MyStatusViewActivity)context).finish();
                                        }
                                    });
                                }
                            }).start();
                            dialog.dismiss();
                            });
                            return true;
                        } else {
                            Toast.makeText(context, "Forwarding status", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    }
                });
                popupMenu.show();
            }
        });
//        sortUserStatuses();
        setThemeForHomeScreen(holder);
    }

    private void updateStatusTime(MyStatusViewHolder holder, long statusTime) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if(!statuses.getStatuses().isEmpty()) {
                    String timeAgo = getTimeAgo(statusTime + "");
                    holder.binding.statusTime.setText(timeAgo);

                    // Check if the status time is more than 1 hour ago
                    long currentTime = System.currentTimeMillis();
                    long diff = currentTime - statusTime;
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
                    if (minutes >= 60) {
                        mainHandler.removeCallbacks(this); // Stop the thread if the status time is more than 1 hour
                        holder.binding.statusTime.setText(getTimeAgo(statusTime + "")); // Set the exact status time
                    } else {
                        mainHandler.postDelayed(this, 60000); // Update every minute if less than 1 hour
                    }
                }else {
                    mainHandler.removeCallbacks(this);
                }
            }
        });
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
                time="Today, "+formatStatusTime(new Date(statusTime))[0];
            }else {
                time="Yesterday, "+formatStatusTime(new Date(statusTime))[0];
            }
//            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            return time;
        }
    }

    private void showMyStatuses(UserStatus myUserStatus,int position) {
        ArrayList<MyStory> myStories = new ArrayList<>();
        ArrayList<String> timestamps = new ArrayList<>();

        SimpleDateFormat formatDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());

        for (Status status : myUserStatus.getStatuses()) {
            myStories.add(new MyStory(status.getImageUrl()));

            timestamps.add(status.getTimeStamps()+"");
        }

        builder= new StoryView.Builder(((MyStatusViewActivity)context).getSupportFragmentManager())
                .setStoriesList(myStories) // Required
                .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                .setTitleText("My status") // Default is Hidden
                .setSubtitleText(getTimeAgo(timestamps.get(0))) // Set the subtitle text of the first status
                .setTitleLogoUrl(currentUser.getProfileImage()) // Default is Hidden
                .setStartingIndex(position)
                .setStoryClickListeners(new StoryClickListeners() {
                    @Override
                    public void onDescriptionClickListener(int position) {
                        // Update the subtitle text when user clicks on a status
                    }

                    @Override
                    public void onTitleIconClickListener(int position) {
                        //your action
                    }
                }) // Optional Listeners
                .setOnStoryChangedCallback(new OnStoryChangedCallback() {
                    @Override
                    public void storyChanged(int position) {
                        if (position < timestamps.size()) {
                            builder.setSubtitleText(getTimeAgo(timestamps.get(position)));
                        }
                    }})
                .build();
        builder.show();
    }

    private String[] formatStatusTime(Date date) {
        SimpleDateFormat formatTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String statusTime = formatTime.format(date);
        SimpleDateFormat formatDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        String statusDate = formatDate.format(date);
        return new String[]{statusTime,statusDate};
    }

    @Override
    public int getItemCount() {
        return statuses.getStatuses().size();
    }

    private  void setThemeForHomeScreen(MyStatusViewHolder holder) {
        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int color;
        int color2;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                color = ContextCompat.getColor(context, R.color.primaryTextColor); // White for dark mode
                color2 = ContextCompat.getColor(context, R.color.secondaryTextColor); // White for dark mode
                holder.binding.deleteMenu.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                break;
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
            default:
                color = ContextCompat.getColor(context, R.color.primaryTextColor); // Black for light mode
                color2 = ContextCompat.getColor(context, R.color.secondaryTextColor); // Black for light mode
                holder.binding.deleteMenu.setImageTintList(ColorStateList.valueOf(Color.BLACK));
                break;
        }
        holder.binding.statusViews.setTextColor(color);
        holder.binding.statusTime.setTextColor(color2);
    }
    public class MyStatusViewHolder extends RecyclerView.ViewHolder {
        MyStatusesLayoutBinding binding;

        public MyStatusViewHolder(@NonNull MyStatusesLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
