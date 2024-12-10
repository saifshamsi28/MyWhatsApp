package com.saif.mywhatsapp.Adapters;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.saif.mywhatsapp.Activities.ChatsActivity;
import com.saif.mywhatsapp.Fragments.FullScreenImageFragment;
import com.saif.mywhatsapp.Models.User;
import com.saif.mywhatsapp.R;
import com.saif.mywhatsapp.databinding.RowConversationBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private ArrayList<User> users;
    private OnItemClickListener listener;
    private boolean isChat;

    public UserAdapter(Context context, ArrayList<User> users, boolean isChat) {
        this.context = context;
        this.users = users;
        this.isChat = isChat;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_conversation, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(user.getUid())) {
            holder.conversationBinding.contactName.setText(user.getName() + " (You)");
        } else {
            holder.conversationBinding.contactName.setText(user.getName());
        }

        if (isChat) {
//            Toast.makeText(context, "is chat true", Toast.LENGTH_SHORT).show();
            if (!user.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                if (user.getStatus().equals("online")) {
                    holder.conversationBinding.status.setVisibility(View.VISIBLE);
                } else {
                    holder.conversationBinding.status.setVisibility(View.GONE);
                }
            } else {
                holder.conversationBinding.status.setVisibility(View.GONE);
            }
        } else {
            holder.conversationBinding.status.setVisibility(View.GONE);
        }

         Glide.with(context)
                .load(user.getProfileImage())
                .placeholder(R.drawable.avatar)
                .into(holder.conversationBinding.contactImg);
        String senderId = FirebaseAuth.getInstance().getUid();
        String senderRoom = senderId + user.getUid();
        setThemeForHomeScreen(holder.conversationBinding.contactName,
                holder.conversationBinding.recentMessage,
                holder.conversationBinding.messageTime);

        // Listen for typing status
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(user.getUid())
                .child("status")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String status = snapshot.getValue(String.class);
                            if ("typing...".equals(status)) {
                                holder.conversationBinding.recentMessage.setText("typing...");
                                holder.conversationBinding.recentMessage.setTextColor(ContextCompat.getColor(context,R.color.GreenishBlue));
                            } else {
                                // Fetch the last message if the user is not typing
                                updateLastMessage(holder, senderRoom);
                            }
                        } else {
                            // Fetch the last message if there's no status
                            updateLastMessage(holder, senderRoom);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error if needed
                    }
                });

        holder.conversationBinding.contactImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMiniProfile(user, v);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatsActivity.class);
                intent.putExtra("Contact_name", holder.conversationBinding.contactName.getText().toString());
                intent.putExtra("chat_profile", user.getProfileImage());
                intent.putExtra("number", user.getPhoneNumber().toString());
                intent.putExtra("userId", user.getUid());
                intent.putExtra("receiverFcmToken", user.getFcmToken());
                context.startActivity(intent);
            }
        });
    }

//    private void showMiniProfile(User user, View clickedView) {
//        Dialog dialog = new Dialog(context);
//        dialog.setContentView(R.layout.mini_profile_layout);
//
//        // Get the position of the clicked view
//        int[] location = new int[2];
//        clickedView.getLocationOnScreen(location);
//        float clickedX = location[0];
//        float clickedY = location[1];
//
//        // Set dialog animations
//        dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
//
//        // Get the dialog's root layout
//        View dialogRoot = dialog.findViewById(android.R.id.content);
//
//        // Measure and layout the dialog content view
//        dialogRoot.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
//        int dialogWidth = dialogRoot.getMeasuredWidth();
//        int dialogHeight = dialogRoot.getMeasuredHeight();
//
//        // Calculate the pivot points relative to the dialog's content view
//
//        // Set pivot points
//        dialogRoot.setPivotX(clickedX);
//        dialogRoot.setPivotY(clickedY);
//
//        // Create the scale and translation animations
//        ObjectAnimator scaleX = ObjectAnimator.ofFloat(dialogRoot, View.SCALE_X, 0.5f, 1.0f);
//        ObjectAnimator scaleY = ObjectAnimator.ofFloat(dialogRoot, View.SCALE_Y, 0.5f, 1.0f);
//        ObjectAnimator alpha = ObjectAnimator.ofFloat(dialogRoot, View.ALPHA, 0.0f, 1.0f);
//
//        AnimatorSet animatorSet = new AnimatorSet();
//        animatorSet.playTogether(scaleX, scaleY, alpha);
//        animatorSet.setDuration(300);
//        animatorSet.start();
//
//        ImageView miniImageView = dialog.findViewById(R.id.mini_profile_img);
//        TextView name = dialog.findViewById(R.id.mini_profile_name);
//
//        // Load mini profile data
//        Glide.with(context)
//                .load(user.getProfileImage())
//                .placeholder(R.drawable.avatar)
//                .into(miniImageView);
//        name.setText(user.getName());
//
//        // Click listener for mini profile image view
//        miniImageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Switch to full profile fragment
//                dialog.dismiss();
//
//                // Make the fragment container visible
//                View fragmentContainer = ((Activity) context).findViewById(R.id.fragment_container);
//                fragmentContainer.setVisibility(View.VISIBLE);
//
//                FullScreenProfileFragment fullScreenProfileFragment = FullScreenProfileFragment.newInstance(user.getProfileImage(), user.getName());
//                fullScreenProfileFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "FullScreenProfileFragment");
//
//                // Set a back stack listener to hide the fragment container when the fragment is popped
////                fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
////                    @Override
////                    public void onBackStackChanged() {
////                        if (fragmentManager.getBackStackEntryCount() == 0) {
////                            fragmentContainer.setVisibility(View.GONE);
////                            fragmentManager.removeOnBackStackChangedListener(this);
////                        }
////                    }
////                });
//            }
//        });
//
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        dialog.show();
//    }

    private void showMiniProfile(User user, View clickedView) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.mini_profile_layout);

        ImageView miniImageView = dialog.findViewById(R.id.mini_profile_img);
        TextView name = dialog.findViewById(R.id.mini_profile_name);

        // Load mini profile data
        Glide.with(context)
                .load(user.getProfileImage())
                .placeholder(R.drawable.avatar)
                .into(miniImageView);
        name.setText(user.getName());

        // Click listener for mini profile image view
        miniImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
                FullScreenImageFragment fullScreenImageFragment = FullScreenImageFragment.newInstance(context,user.getProfileImage(), user.getName());

                fragmentManager.beginTransaction()
                        .add(android.R.id.content, fullScreenImageFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }


    // Insi
    private void updateLastMessage(UserViewHolder holder, String senderRoom) {
        FirebaseDatabase.getInstance().getReference().child("chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            if(snapshot.child("lastMessage").exists() && snapshot.child("lastMessageTime").exists()){
                                String lastMessage = snapshot.child("lastMessage").getValue(String.class);
                                long lastMessageTime = snapshot.child("lastMessageTime").getValue(long.class);
                                Date date = new Date(lastMessageTime);
                                SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                                String formattedTime = formatter.format(date);
                                holder.conversationBinding.recentMessage.setText(lastMessage);
                                holder.conversationBinding.messageTime.setText(formattedTime);
                            }
                        } else {
                            holder.conversationBinding.recentMessage.setText("Tap to check");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error if needed
                    }
                });
    }

    private void setThemeForHomeScreen(TextView contactName, TextView recentMessage, TextView messageTime) {
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

    public class UserViewHolder extends RecyclerView.ViewHolder {
        RowConversationBinding conversationBinding;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            conversationBinding = RowConversationBinding.bind(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClick(users.get(getAdapterPosition()));
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(User user);
    }
}
