package com.saif.mywhatsapp.Adapters;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.saif.mywhatsapp.Activities.MainActivity;
import com.saif.mywhatsapp.Database.AppDatabase;
import com.saif.mywhatsapp.Database.DatabaseClient;
import com.saif.mywhatsapp.Fragments.StatusFragment;
import com.saif.mywhatsapp.Models.Status;
import com.saif.mywhatsapp.Models.User;
import com.saif.mywhatsapp.Models.UserStatus;
import com.saif.mywhatsapp.R;
import com.saif.mywhatsapp.databinding.ItemStatusOtherBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.OnStoryChangedCallback;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class OthersStatusAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final ArrayList<UserStatus> userStatuses;
    private AppDatabase appDatabase;
    private final Executor executor= Executors.newSingleThreadExecutor();
    private final Handler handler=new Handler(Looper.getMainLooper());
    private User user;
    private StoryView.Builder storyBuilder;
    private StatusFragment statusFragment;
    private Runnable statusUpdateRunnable;

    public OthersStatusAdapter(Context context, ArrayList<UserStatus> userStatuses, StatusFragment statusFragment) {
        this.context = context;
        this.userStatuses = userStatuses;
        this.statusFragment=statusFragment;
        appDatabase= DatabaseClient.getInstance(context).getAppDatabase();
    }

    @Override
    public int getItemCount() {
        return userStatuses.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View otherStatusView = LayoutInflater.from(context).inflate(R.layout.item_status_other, parent, false);
            return new OtherStatusViewHolder(otherStatusView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        UserStatus userStatus = userStatuses.get(position);
            ((OtherStatusViewHolder)holder).bind(userStatus);
    }

    public class OtherStatusViewHolder extends RecyclerView.ViewHolder {
        private final ItemStatusOtherBinding binding;

        public OtherStatusViewHolder(View itemView) {
            super(itemView);
            binding = ItemStatusOtherBinding.bind(itemView);
        }

        public void bind(UserStatus userStatus) {
            if (userStatus != null && !userStatus.getStatuses().isEmpty()) {
                Status lastStatus = userStatus.getStatuses().get(userStatus.getStatuses().size() - 1);

                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        user=appDatabase.userDao().getUserByUid(userStatus.getUserId());
                        binding.othersStatusContactName.setText(user.getName());
                    }
                });

                binding.othersCircularStatusView.setPortionsCount(userStatus.getStatuses().size());
                Glide.with(context).load(lastStatus.getImageUrl()).into(binding.othersStatusImage);
                updateStatusTime(userStatus,lastStatus.getTimeStamps());
                binding.othersCircularStatusView.setVisibility(View.VISIBLE);
                binding.otherStatusLayout.setOnClickListener(v -> showStoryView(userStatus));
                binding.othersImageLayout.setVisibility(View.VISIBLE);

            } else {
                binding.othersImageLayout.setVisibility(View.GONE);
                binding.otherStatusLayout.setVisibility(View.GONE);
            }
            setThemeForHomeScreen(binding.othersStatusContactName, binding.othersStatusTime);
        }
        
        private void showStoryView(UserStatus userStatus) {
            ArrayList<MyStory> myStories = new ArrayList<>();
            ArrayList<String> timestamps = new ArrayList<>();
            for (Status story : userStatus.getStatuses()) {
                myStories.add(new MyStory(story.getImageUrl()));
                timestamps.add(story.getTimeStamps()+"");
            }
            storyBuilder=new StoryView.Builder(((MainActivity) context).getSupportFragmentManager())
                    .setStoriesList(myStories)
                    .setStoryDuration(5000)
                    .setTitleText(user.getName())
                    .setSubtitleText(getTimeAgo(timestamps.get(0)))
                    .setTitleLogoUrl(user.getProfileImage())
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
                    .setOnStoryChangedCallback(new OnStoryChangedCallback() {
                        @Override
                        public void storyChanged(int position) {
                            storyBuilder.setSubtitleText(getTimeAgo(timestamps.get(position)));
                        }
                    })
                    .build();
            storyBuilder.show();
        }

        private void updateStatusTime(UserStatus userStatus, long statusTime) {
            Log.d("updateStatusTime", "updateStatusTime method calling " );
            if (statusUpdateRunnable != null) {
                handler.removeCallbacks(statusUpdateRunnable);
            }
            statusUpdateRunnable = new Runnable() {
                @Override
                public void run() {
                    if (!userStatus.getStatuses().isEmpty()) {
                        String timeAgo = getTimeAgo(statusTime + "");
                        Log.e("updateStatusMethod","status time: "+timeAgo);
                        binding.othersStatusTime.setText(timeAgo);

                        long currentTime = System.currentTimeMillis();
                        long diff = currentTime - statusTime;
                        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
                        Log.d("updateStatusTime", "statusTime: " + statusTime + ", currentTime: " + currentTime + ", minutes: " + minutes);
                        if (minutes >= 60) {
                            handler.removeCallbacks(this); // Stop updating if more than 1 hour
                            String currentDate = formatStatusTime(new Date())[1];
                            String statusDate = formatStatusTime(new Date(statusTime))[1];
                            String lastStatusTime = formatStatusTime(new Date(statusTime))[0];
                            if (!currentDate.equals(statusDate)) {
                                binding.othersStatusTime.setText("Yesterday");
                            }else {
                                binding.othersStatusTime.setText(lastStatusTime);
                            }
                            Log.e("updateStatusTimeMethod","lastStatusTime: "+lastStatusTime);
                        } else {
                            handler.postDelayed(this, 60000); // Update every minute if less than 1 hour
                        }
                    } else {
                        binding.othersStatusTime.setText("Tap to add status update");
                        Log.e("updateStatusTimeMethod","other status is null");
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
            Log.d("TimeAgo", "statusTime: " + statusTime + ", currentTime: " + currentTime + ", minutes: " + minutes);
            if (minutes < 1) {
                return "Just now";
            } else if (minutes < 60) {
                return minutes + " minutes ago";
            } else {
                String currentDate=formatStatusTime(new Date())[1];
                String statusDate=formatStatusTime(new Date(statusTime))[1];
                if(currentDate.equals(statusDate)){
                    Log.e("getTimeAgo return value","status time is : Today ,"+formatStatusTime(new Date(statusTime))[0]);
                    return "Today, "+formatStatusTime(new Date(statusTime))[0];
                }else {
                    Log.e("getTimeAgo return value","status time is : Yesterday ,"+formatStatusTime(new Date(statusTime))[0]);
                    return "Yesterday, "+formatStatusTime(new Date(statusTime))[0];
                }
            }
        }
    }

    private String[] formatStatusTime(Date date) {
        SimpleDateFormat formatTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String statusTime = formatTime.format(date);
        SimpleDateFormat formatDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        String statusDate = formatDate.format(date);
        return new String[]{statusTime,statusDate};
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
    }
}
