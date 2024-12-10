package com.saif.mywhatsapp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.saif.mywhatsapp.Activities.SplashActivity;
import com.saif.mywhatsapp.Database.AppDatabase;
import com.saif.mywhatsapp.Database.DatabaseClient;
import com.saif.mywhatsapp.Models.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FCMNotificationService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "chat_notifications";
    private static final String CHANNEL_NAME = "Chat Notifications";
    private static final String CHANNEL_DESCRIPTION = "Notifications for chat messages";
    private static final int NOTIFICATION_ID = 28;
    private AppDatabase appDatabase;
    private final Executor executor= Executors.newSingleThreadExecutor();
    private final Handler handler=new Handler(Looper.getMainLooper());
    private static final String STORAGE_DIRECTORY = "Android/media/com.saif.mywhatsapp/MyWhatsApp/Media/ChatRecordings/receive";
    private static final String STORAGE_SUBDIRECTORY = "receive";
    private String recordingName;
    String receiverRoom;


    //    // To store messages for each user
    private Map<String, StringBuilder> userMessages = new HashMap<>();

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

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("FCMNotificationService", "onMessageReceived called");

        if (!remoteMessage.getData().isEmpty()) {
            Log.d("FCMNotificationService", "Data received: " + remoteMessage.getData());
            appDatabase= DatabaseClient.getInstance(getApplicationContext()).getAppDatabase();

            String title = remoteMessage.getData().get("title");
            String message = remoteMessage.getData().get("body");
            String imageUrl = remoteMessage.getData().get("chat_profile");
            String userId = remoteMessage.getData().get("userId");
            String contactName = remoteMessage.getData().get("Contact_name");
            String chatProfile = remoteMessage.getData().get("chat_profile");
            long timeStamp = Long.parseLong(remoteMessage.getData().get("timeStamp"));
            boolean isAudio = Boolean.parseBoolean(remoteMessage.getData().get("isAudio"));
            int status = Integer.parseInt(remoteMessage.getData().get("status"));
            String key = remoteMessage.getData().get("key");
            String senderRoom = remoteMessage.getData().get("senderRoom");
            receiverRoom = remoteMessage.getData().get("receiverRoom");
            String number = remoteMessage.getData().get("number");
            String receiverFcmToken = remoteMessage.getData().get("receiverFcmToken");
            Message newMessage=new Message(message,userId,timeStamp,key,status,isAudio,senderRoom,receiverRoom);

            StringBuilder messages = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                messages = userMessages.getOrDefault(userId, new StringBuilder());
            }
            if (messages != null) {
                if(newMessage.isAudioMessage()){
                    messages.append(getVoiceMessageDuration(newMessage)).append("\n");
                    Log.d("onMessageReceived","calling audio handling method");
                    handleAudioMessage(newMessage);
                }else {
                    messages.append(message).append("\n");
                    storeMessageLocally(newMessage);
                    Log.d("onMessageReceived","setting messages in inbox style");
                }
            }else {
                Log.d("onMessageReceived","message is null");
            }
            userMessages.put(userId, messages);

            // Display notification and handle message status updates
            displayNotification(newMessage,title, Objects.requireNonNull(messages).toString(), imageUrl, userId, contactName, chatProfile, number, receiverFcmToken);
        }
    }

    private void displayNotification(Message newMessage, String title, String messages, String imageUrl, String userId, String contactName, String chatProfile, String number, String receiverFcmToken) {
        Log.d("FCMNotificationService", "displayNotification called with title: " + title);

        Intent intent = new Intent(this, SplashActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("Contact_name", contactName);
        intent.putExtra("chat_profile", chatProfile);
        intent.putExtra("number", number);
        intent.putExtra("receiverFcmToken", receiverFcmToken);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        RemoteViews notificationLayout;
        if(newMessage.isAudioMessage()){
            notificationLayout = new RemoteViews(getPackageName(), R.layout.custom_notification_audio);
            notificationLayout.setTextViewText(R.id.sender_name, contactName);
            notificationLayout.setTextViewText(R.id.voice_message_duration, getVoiceMessageDuration(newMessage));
        }else {
            notificationLayout = new RemoteViews(getPackageName(), R.layout.custom_notification_text);
            notificationLayout.setTextViewText(R.id.sender_name, contactName);
            notificationLayout.setTextViewText(R.id.message_text, messages);
        }

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        String[] lines = messages.split("\\n");
        for (String line : lines) {
            inboxStyle.addLine(line);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setCustomHeadsUpContentView(notificationLayout)
                .setContentTitle(title)
                .setContentText(messages)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setCustomContentView(notificationLayout)
                .setGroup(userId)
                .setGroupSummary(true)
                .setAutoCancel(true);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Log.d("FCMNotificationService", "Loading image from URL: " + imageUrl);

            Glide.with(this)
                    .asBitmap()
                    .load(imageUrl)
                    .placeholder(R.drawable.avatar)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            Bitmap circularBitmap = getCircularBitmap(resource);
                            notificationLayout.setImageViewBitmap(R.id.profile_image, circularBitmap);
                            sendNotification(builder);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {}
                    });
        } else {
            Log.d("FCMNotificationService", "No image URL provided, using default icon");
            Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.avatar);
            Bitmap circularBitmap = getCircularBitmap(largeIcon);
            notificationLayout.setImageViewBitmap(R.id.profile_image, circularBitmap);
            sendNotification(builder);
        }
    }

    private CharSequence getVoiceMessageDuration(Message newMessage) {
        MediaPlayer mediaPlayer=new MediaPlayer();
        try {
            mediaPlayer.setDataSource(newMessage.getMessage());
            mediaPlayer.prepare();
            return "Voice message("+formatTime(mediaPlayer.getDuration())+")";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String formatTime(int milliseconds) {
        int seconds = milliseconds / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private Bitmap getCircularBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int minEdge = Math.min(width, height);

        int dx = (width - minEdge) / 2;
        int dy = (height - minEdge) / 2;

        Bitmap squaredBitmap = Bitmap.createBitmap(bitmap, dx, dy, minEdge, minEdge);

        Bitmap circularBitmap = Bitmap.createBitmap(minEdge, minEdge, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(circularBitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        float r = minEdge / 2f;
        canvas.drawCircle(r, r, r, paint);

        return circularBitmap;
    }

    private void sendNotification(NotificationCompat.Builder builder) {
        Log.d("FCMNotificationService", "sendNotification called");

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        int notificationId = generateNotificationId();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.d("FCMNotificationService", "Notification permission not granted");
            return;
        }
        notificationManager.notify(notificationId, builder.build());
    }

    private int generateNotificationId() {
        return (int) System.currentTimeMillis();
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(uid)
                    .child("fcmToken")
                    .setValue(token)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("FCMNotificationService", "New FCM token saved successfully");
                        } else {
                            Log.e("FCMNotificationService", "Failed to save new FCM token: " + task.getException());
                        }
                    });
        } else {
            Log.e("FCMNotificationService", "User not authenticated. Token not saved.");
        }
        Log.d("FCMNotificationService", "New FCM token: " + token);
    }

    private void handleAudioMessage(Message newMessage) {
//        String audioUrl = remoteMessage.getData().get("audioUrl");

        Log.d("handleAudioMessage","handleAudioMessage method called");
        // Download audio file from Firebase Storage
        downloadAudioFromStorage(newMessage,newMessage.getMessage(),newMessage.getKey(),newMessage.getSenderId(),newMessage.getSenderRoom());
    }

    private void downloadAudioFromStorage(Message newMessage, String audioUrl, String messageId, String senderId, String receiverId) {
        ContentResolver resolver = getContentResolver();
        ContentValues contentValues = new ContentValues();
        long currentTimeMillis = System.currentTimeMillis();
        String fileCreatedTime = formatAudioCreatedTime(currentTimeMillis)[0] + "_" + formatAudioCreatedTime(currentTimeMillis)[1];
        String uniqueID = UUID.randomUUID().toString().substring(0, 8);
        recordingName="MyAud-" + fileCreatedTime + "_" + uniqueID + ".3gp";
        contentValues.put(MediaStore.Audio.Media.DISPLAY_NAME, recordingName );
        contentValues.put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp");
        contentValues.put(MediaStore.Audio.Media.RELATIVE_PATH, STORAGE_DIRECTORY);

        Uri audioUri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues);
        Log.d("initMediaPlayer","audio uri: "+audioUri);

        String audioFilePath = "";
        if (audioUri != null) {
            audioFilePath = getPathFromUri(audioUri);
            Log.d("initMediaPlayer", "audioFilePath: " + audioFilePath);
        }

        Log.d("downloadAudioFromStorage","local filePath to store audio "+audioFilePath);
        String finalAudioFilePath = audioFilePath;
        Glide.with(this)
                .downloadOnly()
                .load(audioUrl)
                .into(new CustomTarget<File>() {
                    @Override
                    public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                        // Move or copy downloaded file to local storage
                        moveFile(Uri.fromFile(resource), finalAudioFilePath);
                        Log.d("downloadAudioFromStorage","resource is ready "+resource.getAbsolutePath());

                        // Store the path in local database
                        storeAudioLocally(newMessage,messageId, senderId, receiverId, finalAudioFilePath);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Handle clear placeholder if needed
                    }
                });
    }

    private String[] formatAudioCreatedTime(long audioCreatedTime) {
        SimpleDateFormat formatTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String recordAt = formatTime.format(new Date(audioCreatedTime));
        SimpleDateFormat formatDate = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
        String recordOn = formatDate.format(new Date(audioCreatedTime));
        return new String[]{recordOn,recordAt};
    }


    private String getPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Audio.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(columnIndex);
            cursor.close();
            return path;
        }
        return null;
    }

    private void moveFile(Uri sourceUri, String destinationPath) {
        try {
            ContentResolver resolver = getContentResolver();
            ParcelFileDescriptor pfd = resolver.openFileDescriptor(sourceUri, "r");
            if (pfd == null) {
                throw new FileNotFoundException("ParcelFileDescriptor is null for URI: " + sourceUri);
            }

            FileInputStream inputStream = new FileInputStream(pfd.getFileDescriptor());
            FileOutputStream outputStream = new FileOutputStream(destinationPath);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();
            pfd.close();
            Log.d("moveFile", "File moved successfully to: " + destinationPath);
        } catch (IOException e) {
            Log.e("moveFile", "Exception occurred in moving file", e);
        }
    }

    private void storeAudioLocally(Message newMessage, String messageId, String senderId, String receiverId, String localFilePath) {
        executor.execute(() -> {
            newMessage.setMessage(localFilePath);
                appDatabase.messageDao().insertMessage(newMessage);
                notifySenderMessageReceived(newMessage, newMessage.getSenderRoom(),receiverRoom, newMessage.getKey());
        });
    }
    private void storeMessageLocally(Message newMessage) {
        executor.execute(() -> {
            appDatabase.messageDao().insertMessage(newMessage);
            notifySenderMessageReceived(newMessage, newMessage.getSenderRoom(),receiverRoom, newMessage.getKey());
        });
    }

    private void notifySenderMessageReceived(Message newMessage, String senderRoom, String receiverRoom, String key) {
        executor.execute(() -> {
            // Fetch the latest status from the local database
            Message existingMessage = appDatabase.messageDao().getMessageByKey(key);

            if (existingMessage != null) {
                if (existingMessage.getStatus() != Message.STATUS_READ) {
                    // Only update to "delivered" if the message is not "seen"
                    newMessage.setStatus(Message.STATUS_DELIVERED);
                    appDatabase.messageDao().insertMessage(newMessage);

                    // Update the status in Firebase
                    FirebaseDatabase.getInstance().getReference()
                            .child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(key)
                            .child("status")
                            .setValue(Message.STATUS_DELIVERED);
                    FirebaseDatabase.getInstance().getReference()
                            .child("chats")
                            .child(receiverRoom)
                            .child("messages")
                            .child(key)
                            .child("status")
                            .setValue(Message.STATUS_DELIVERED);
                }
            } else {
                // If message does not exist in the local database, insert it
                newMessage.setStatus(Message.STATUS_DELIVERED);
                appDatabase.messageDao().insertMessage(newMessage);

                // Update the status in Firebase
                FirebaseDatabase.getInstance().getReference()
                        .child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .child(key)
                        .child("status")
                        .setValue(Message.STATUS_DELIVERED);
                FirebaseDatabase.getInstance().getReference()
                        .child("chats")
                        .child(receiverRoom)
                        .child("messages")
                        .child(key)
                        .child("status")
                        .setValue(Message.STATUS_DELIVERED);
            }
        });
    }

}