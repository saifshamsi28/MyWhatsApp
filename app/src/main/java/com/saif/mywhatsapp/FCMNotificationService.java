package com.saif.mywhatsapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.saif.mywhatsapp.Activities.SplashActivity;

public class FCMNotificationService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "chat_notifications";
    private static final String CHANNEL_NAME = "Chat Notifications";
    private static final String CHANNEL_DESCRIPTION = "Notifications for chat messages";
    private static final int NOTIFICATION_ID = 28;

    // Method to create notification channel (call this in onCreate or onNewToken method)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }
//9430820499   7352252816
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            String title = remoteMessage.getData().get("title");
            String message = remoteMessage.getData().get("body");
            String imageUrl = remoteMessage.getData().get("image_url");
            String userId = remoteMessage.getData().get("user_id");
            String contactName = remoteMessage.getData().get("contact_name");
            String chatProfile = remoteMessage.getData().get("chat_profile");
            String number = remoteMessage.getData().get("number");
            String receiverFcmToken = remoteMessage.getData().get("receiver_fcm_token");

            displayNotification(title, message, imageUrl, userId, contactName, chatProfile, number, receiverFcmToken);
        }
    }

    private void displayNotification(String title, String message, String imageUrl, String userId, String contactName, String chatProfile, String number, String receiverFcmToken) {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("Contact_name", contactName);
        intent.putExtra("chat_profile", chatProfile);
        intent.putExtra("number", number);
        intent.putExtra("receiverFcmToken", receiverFcmToken);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Create the custom notification layouts
        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_small);
        RemoteViews notificationLayoutExpanded = new RemoteViews(getPackageName(), R.layout.notification_large);

        // Set the title and message in the custom layouts
        notificationLayout.setTextViewText(R.id.notification_title, title);
        notificationLayoutExpanded.setTextViewText(R.id.notification_title, title);
        notificationLayoutExpanded.setTextViewText(R.id.notification_body, message);

        // Build the notification with the custom layouts
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomBigContentView(notificationLayoutExpanded)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .asBitmap()
                    .load(chatProfile)
                    .placeholder(R.drawable.avatar)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            builder.setLargeIcon(resource);
                            notificationManager.notify(NOTIFICATION_ID, builder.build());
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.avatar));
                            notificationManager.notify(NOTIFICATION_ID, builder.build()); // Show notification with default avatar
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // Optional: handle placeholder clearing if needed
                        }
                    });
        } else {
            builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.avatar)); // Set default avatar if imageUrl is null or empty
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            notificationManager.notify(NOTIFICATION_ID, builder.build()); // Display the notification with the default avatar
        }
    }

    @Override
    public void onNewToken(String token) {
        // Handle new FCM token generation
        // You can send the token to your server here
    }
}
