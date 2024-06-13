package com.saif.mywhatsapp.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.saif.mywhatsapp.Activities.MainActivity;
import com.saif.mywhatsapp.Models.Status;
import com.saif.mywhatsapp.Models.UserStatus;
import com.saif.mywhatsapp.R;
import com.saif.mywhatsapp.databinding.ItemStatusBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class TopStatusAdapter extends RecyclerView.Adapter<TopStatusAdapter.TopStatusViewHolder> {
    Context context;
    ArrayList<UserStatus> userStatuses;

    public TopStatusAdapter(Context context, ArrayList<UserStatus> userStatuses) {
        this.context = context;
        this.userStatuses = userStatuses;
    }

    @NonNull
    @Override
    public TopStatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_status,parent,false);
        return new TopStatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopStatusViewHolder holder, int position) {

        UserStatus userStatus=userStatuses.get(position);
        Status lastStatus;
        if(!userStatus.getStatuses().isEmpty()) {
            lastStatus = userStatus.getStatuses().get(userStatus.getStatuses().size() - 1);
            holder.statusBinding.circularStatusView.setPortionsCount(userStatus.getStatuses().size());
            Glide.with(context).load(lastStatus.getImageUrl()).into(holder.statusBinding.statusImage);
            holder.statusBinding.circularStatusView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<MyStory> myStories = new ArrayList<>();

                    for(Status story: userStatus.getStatuses()){
                        myStories.add(new MyStory(
                                story.getImageUrl()
                        ));
                    }
                    Date date=new Date(userStatus.getLastUpdated());
                    SimpleDateFormat formatTime=new SimpleDateFormat("hh:mm a", Locale.getDefault());
                    String statusTime=formatTime.format(date);
                    SimpleDateFormat formatDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                    String statusDate=formatDate.format(date);

                    new StoryView.Builder(((MainActivity)context).getSupportFragmentManager())
                            .setStoriesList(myStories) // Required
                            .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                            .setTitleText(userStatus.getName()) // Default is Hidden
                            .setSubtitleText(statusTime+" "+statusDate) // Default is Hidden
                            .setTitleLogoUrl(userStatus.getProfileImage()) // Default is Hidden
                            .setStoryClickListeners(new StoryClickListeners() {
                                @Override
                                public void onDescriptionClickListener(int position) {
                                    //your action
                                }
                                @Override
                                public void onTitleIconClickListener(int position) {
                                    //your action
                                }
                            }) // Optional Listeners
                            .build() // Must be called before calling show method
                            .show();
                }
            });
        }
        else {
            holder.statusBinding.imageLayout.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return userStatuses.size();
    }

    public class TopStatusViewHolder extends RecyclerView.ViewHolder{

        ItemStatusBinding statusBinding;
        public TopStatusViewHolder(@NonNull View itemView) {
            super(itemView);
            statusBinding=ItemStatusBinding.bind(itemView);
        }
    }
}
