package com.saif.mywhatsapp.Adapters;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.saif.mywhatsapp.Activities.MainActivity;
import com.saif.mywhatsapp.AppDatabase;
import com.saif.mywhatsapp.DatabaseClient;
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

    public OthersStatusAdapter(Context context, ArrayList<UserStatus> userStatuses) {
        this.context = context;
        this.userStatuses = userStatuses;
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
        ConstraintLayout myStatusItemLayout;
        LinearLayout myStatusFragmentLayout;

        public OtherStatusViewHolder(View itemView) {
            super(itemView);
            binding = ItemStatusOtherBinding.bind(itemView);
            myStatusItemLayout=itemView.findViewById(R.id.main_layout_me);
            myStatusFragmentLayout=itemView.findViewById(R.id.my_status_layout);
        }

        public void bind(UserStatus userStatus) {
            if (userStatus.getStatuses() != null && !userStatus.getStatuses().isEmpty()) {
                Date date = new Date(userStatus.getLastUpdated());
                SimpleDateFormat formatTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                String statusTime = formatTime.format(date);
                SimpleDateFormat formatDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                String statusDate = formatDate.format(date);
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        user = appDatabase.userDao().getUserByUid(userStatus.getUserId());
                    }
                });
                if(statusTime.startsWith("0"))
                    statusTime=statusTime.substring(1);
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
                binding.othersCircularStatusView.setVisibility(View.VISIBLE);
                String finalStatusTime = statusTime;
                binding.otherStatusLayout.setOnClickListener(v -> showStoryView(userStatus));
                binding.othersImageLayout.setVisibility(View.VISIBLE);
                binding.othersStatusContactName.setText(userStatus.getName());
                Log.e("log","log in bind method : "+userStatus.getUserId());
                String currentDate=formatDate.format(new Date());
                if(currentDate.equals(statusDate)) {
                    binding.othersStatusTime.setText(statusTime);
                }else {
//                    Toast.makeText(context, "current date is different", Toast.LENGTH_SHORT).show();
                    binding.othersStatusTime.setText("Yesterday");
                }
            } else {
                binding.othersImageLayout.setVisibility(View.GONE);
            }
            setThemeForHomeScreen(binding.othersStatusContactName,binding.othersStatusTime);
        }

        private void showStoryView(UserStatus userStatus) {
            ArrayList<MyStory> myStories = new ArrayList<>();
            ArrayList<String> timestamps = new ArrayList<>();
            String currentDate=formatStatusTime(new Date())[1];
            for (Status story : userStatus.getStatuses()) {
                myStories.add(new MyStory(story.getImageUrl()));

                String statusTime = formatStatusTime(new Date(story.getTimeStamps()))[0];
                String statusDate = formatStatusTime(new Date(story.getTimeStamps()))[1];

                if(statusTime.startsWith("0"))
                    statusTime=statusTime.substring(1);

                if (currentDate.equals(statusDate)) {
                    statusTime = "Today, " + statusTime;
                } else {
                    statusTime = "Yesterday, " + statusTime;
                }

                timestamps.add(statusTime);
            }

            Log.e("log","log in showStoryView method : "+userStatus.getUserId());
            storyBuilder=new StoryView.Builder(((MainActivity) context).getSupportFragmentManager())
                    .setStoriesList(myStories)
                    .setStoryDuration(5000)
                    .setTitleText(user.getName())
                    .setSubtitleText(timestamps.get(0))
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
                            storyBuilder.setSubtitleText(timestamps.get(position));
                        }
                    })
                    .build();
            storyBuilder.show();
        }
    }

    private String[] formatStatusTime(Date date) {
// Date date = new Date(lastStatus.getTimeStamps());
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
