package com.saif.mywhatsapp.Adapters;

import android.content.Context;
import android.content.res.Configuration;
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
import com.saif.mywhatsapp.Models.Status;
import com.saif.mywhatsapp.Models.UserStatus;
import com.saif.mywhatsapp.R;
import com.saif.mywhatsapp.databinding.ItemStatusMeBinding;
import com.saif.mywhatsapp.databinding.ItemStatusOtherBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class StatusAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ME = 1;
    private static final int VIEW_TYPE_OTHER = 2;

    private final Context context;
    private final ArrayList<UserStatus> userStatuses;

    public StatusAdapter(Context context, ArrayList<UserStatus> userStatuses) {
        this.context = context;
        this.userStatuses = userStatuses;
    }

//    @Override
//    public int getItemViewType(int position) {
//        UserStatus userStatus = userStatuses.get(position);
//        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        return currentUserId.equals(userStatus.getUserId()) ? VIEW_TYPE_ME : VIEW_TYPE_OTHER;
//    }

    @Override
    public int getItemCount() {
        return userStatuses.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        if (viewType == VIEW_TYPE_ME) {
//            View myStatusView = LayoutInflater.from(context).inflate(R.layout.item_status_me, parent, false);
//            return new MyStatusViewHolder(myStatusView);
//        } else {
            View otherStatusView = LayoutInflater.from(context).inflate(R.layout.item_status_other, parent, false);
            return new OtherStatusViewHolder(otherStatusView);
//        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        UserStatus userStatus = userStatuses.get(position);
//        if (holder instanceof MyStatusViewHolder) {
//            ((MyStatusViewHolder) holder).bind(userStatus);
//        else holder instanceof OtherStatusViewHolder ;
            ((OtherStatusViewHolder)holder).bind(userStatus);
//        }
    }

    public class MyStatusViewHolder extends RecyclerView.ViewHolder {
        private final ItemStatusMeBinding binding;

        public MyStatusViewHolder(View itemView) {
            super(itemView);
            binding = ItemStatusMeBinding.bind(itemView);
        }

        public void bind(UserStatus userStatus) {

        }


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
//            myStatusFragmentLayout.addView(myStatusItemLayout);
            if (userStatus.getStatuses() != null && !userStatus.getStatuses().isEmpty()) {
                Date date = new Date(userStatus.getLastUpdated());
                SimpleDateFormat formatTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                String statusTime = formatTime.format(date);
                SimpleDateFormat formatDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                String statusDate = formatDate.format(date);
                if(statusTime.startsWith("0"))
                    statusTime=statusTime.substring(1);
                Status lastStatus = userStatus.getStatuses().get(userStatus.getStatuses().size() - 1);
                binding.othersCircularStatusView.setPortionsCount(userStatus.getStatuses().size());
                Glide.with(context).load(lastStatus.getImageUrl()).into(binding.othersStatusImage);
                binding.othersCircularStatusView.setVisibility(View.VISIBLE);
                String finalStatusTime = statusTime;
                binding.otherStatusLayout.setOnClickListener(v -> showStoryView(userStatus, finalStatusTime,statusDate));
                binding.othersImageLayout.setVisibility(View.VISIBLE);
                binding.othersStatusContactName.setText(userStatus.getName());
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

        private void showStoryView(UserStatus userStatus, String statusTime,String statusDate) {
            ArrayList<MyStory> myStories = new ArrayList<>();
            for (Status story : userStatus.getStatuses()) {
                myStories.add(new MyStory(story.getImageUrl()));
            }
//            Date date = new Date(userStatus.getLastUpdated());
//            SimpleDateFormat formatTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());
//            String statusTime = formatTime.format(date);
            SimpleDateFormat formatDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            String currentDate = formatDate.format(new Date());
            String thisStatusTime=null;
            if(currentDate.equals(statusDate)){
                thisStatusTime="Today, "+statusTime;
            }else {
                thisStatusTime="Yesterday, "+statusTime;
            }

            new StoryView.Builder(((MainActivity) context).getSupportFragmentManager())
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
