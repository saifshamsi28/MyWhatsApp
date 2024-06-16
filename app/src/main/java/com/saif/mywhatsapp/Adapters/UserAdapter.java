package com.saif.mywhatsapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.health.connect.datatypes.units.Length;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.saif.mywhatsapp.Activities.ChatsActivity;
import com.saif.mywhatsapp.Models.User;
import com.saif.mywhatsapp.R;
import com.saif.mywhatsapp.databinding.RowConversationBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    public UserAdapter(Context context, ArrayList<User> users) {
        this.context = context;
        this.users = users;
    }

     Context context;
    ArrayList<User> users;
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_conversation,parent,false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user=users.get(position);
        holder.conversationBinding.contactName.setText(user.getName());
        ImageView imageView=Glide.with(context)
                .load(user.getProfileImage())
                .placeholder(R.drawable.avatar)
                .into(holder.conversationBinding.contactImg).getView();
        String senderId= FirebaseAuth.getInstance().getUid();
        String senderRoom=senderId+user.getUid();
        setThemeForHomeScreen(holder.conversationBinding.contactName,
                              holder.conversationBinding.recentMessage,
                              holder.conversationBinding.messageTime);

        FirebaseDatabase.getInstance().getReference().child("chats")
                        .child(senderRoom)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()) {
                                    String lastMessage = snapshot.child("lastMessage").getValue(String.class);
                                    long lastMessageTime = snapshot.child("lastMessageTime").getValue(long.class);
                                    Date date = new Date(lastMessageTime);
                                    SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                                    String formattedTime = formatter.format(date);
                                    holder.conversationBinding.recentMessage.setText(lastMessage);
                                    holder.conversationBinding.messageTime.setText(formattedTime);
//                                    Log.e("UserAdapter", "Last message : "+lastMessage);
//                                    Log.e("UserAdapter", "LAST MESSAGE TIME : "+formattedTime);
                                }
                                else {
                                    holder.conversationBinding.recentMessage.setText("Tap to check");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, ChatsActivity.class);
                intent.putExtra("Contact_name",holder.conversationBinding.contactName.getText().toString());
                intent.putExtra("chat_profile", user.getProfileImage());
                intent.putExtra("number",user.getPhoneNumber().toString());
                intent.putExtra("uid",user.getUid());
                context.startActivity(intent);
            }
        });
    }

    private  void setThemeForHomeScreen(TextView contactName, TextView recentMessage, TextView messageTime) {
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
        contactName.setTextColor(color);
        recentMessage.setTextColor(color2);
        messageTime.setTextColor(color2);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
    public class UserViewHolder extends RecyclerView.ViewHolder{
        RowConversationBinding conversationBinding;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            conversationBinding=RowConversationBinding.bind(itemView);
        }
    }
}

