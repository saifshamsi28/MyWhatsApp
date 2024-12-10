package com.saif.mywhatsapp.Activities;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.devlomi.record_view.OnRecordClickListener;
import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.saif.mywhatsapp.Adapters.MessagesAdapter;
import com.saif.mywhatsapp.Database.AppDatabase;
import com.saif.mywhatsapp.AuthUtil;
import com.saif.mywhatsapp.Database.DatabaseClient;
import com.saif.mywhatsapp.Database.MessageDao;
import com.saif.mywhatsapp.Models.Message;
import com.saif.mywhatsapp.Models.User;
import com.saif.mywhatsapp.MyWhatsAppPermissions;
import com.saif.mywhatsapp.R;
import com.saif.mywhatsapp.UserStatusObserver;
import com.saif.mywhatsapp.databinding.ActivityChatsBinding;
import org.json.JSONObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class ChatsActivity extends AppCompatActivity {
    ActivityChatsBinding chatsBinding;
    MessagesAdapter messagesAdapter;
    ArrayList<Message> messages;
    String senderRoom, receiverRoom,receiverName;
    FirebaseDatabase database;
    private AppDatabase appDatabase;
    private long lastSeen;
    String receiverUid,receiverFcmToken,profile_image_uri;
    DatabaseReference receiverReference;
    private UserStatusObserver userStatusObserver;
    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private MyWhatsAppPermissions myWhatsAppPermissions;
    private String recordingName;
    private String senderUid;
    private Executor executor= Executors.newSingleThreadExecutor();
    private Handler handler=new Handler(Looper.getMainLooper());
    private MessageDao messageDao;
    private long lastFetchedTime;
    private Set<String> messageKeys = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatsBinding = ActivityChatsBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(chatsBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        database = FirebaseDatabase.getInstance();
        appDatabase = DatabaseClient.getInstance(this).getAppDatabase();
        messageDao = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase().messageDao();
        myWhatsAppPermissions = new MyWhatsAppPermissions();
        chatsBinding.status.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        chatsBinding.status.setSingleLine(true);
        chatsBinding.status.setMarqueeRepeatLimit(1);
        chatsBinding.status.setSelected(true);
        receiverName = getIntent().getStringExtra("Contact_name");
        chatsBinding.name.setText(receiverName);
        senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        profile_image_uri = getIntent().getStringExtra("chat_profile");
        if (getIntent() != null && getIntent().hasExtra("userId")) {
            receiverUid = getIntent().getStringExtra("userId");
        }

        if(receiverUid.equals(senderUid)){
            chatsBinding.videoCall.setVisibility(View.GONE);
            chatsBinding.audioCall.setVisibility(View.GONE);
            chatsBinding.status.setText("Message yourself");
        }
        receiverFcmToken = getIntent().getStringExtra("receiverFcmToken");
        // to get the user's status and last seen updates
        userStatusObserver = new UserStatusObserver(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(userStatusObserver);

        if (receiverUid != null) {
            listenForUserStatus(receiverUid);
        }

        String senderUid = FirebaseAuth.getInstance().getUid();
        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;
        Glide.with(getBaseContext())
                .load(profile_image_uri)
                .placeholder(R.drawable.avatar)
                .into(chatsBinding.profile);

        chatsBinding.backBtn.setOnClickListener(v -> finish());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            WindowInsetsCompat imeInsets = insets;
            Insets systemBars = imeInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            int imeHeight = imeInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom;

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            if (imeHeight > 0) {
                // Keyboard is open
                float offset = 0.91f;
                chatsBinding.cardView.setTranslationY((-imeHeight * offset));
                chatsBinding.sendBtn.setTranslationY((-imeHeight * offset));
                chatsBinding.chatRecyclerView.setTranslationY((-imeHeight * offset));
                chatsBinding.recordButton.setTranslationY((-imeHeight * offset));
                chatsBinding.recordView.setTranslationY((-imeHeight * offset));
                chatsBinding.bottomScrollBtn.setTranslationY(-imeHeight*offset);
                chatsBinding.chatRecyclerView.setTranslationZ(-5f);
                chatsBinding.recordView.setTranslationY((-imeHeight * offset));
            } else {
                // Keyboard is closed
                chatsBinding.cardView.setTranslationY(0);
                chatsBinding.sendBtn.setTranslationY(0);
                chatsBinding.chatRecyclerView.setTranslationY(0);
                chatsBinding.recordButton.setTranslationY(0);
                chatsBinding.recordView.setTranslationY(0);
                chatsBinding.chatRecyclerView.setTranslationZ(-5f);
                chatsBinding.bottomScrollBtn.setTranslationY(0);
                chatsBinding.recordView.setTranslationY(0);
                chatsBinding.chatRecyclerView.scrollToPosition(messagesAdapter.getItemCount() - 1);
            }
            return WindowInsetsCompat.CONSUMED;
        });
        chatsBinding.chatRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                checkLastMessageVisibility();
            }
        });

        RecordView recordView = findViewById(R.id.record_view);
        RecordButton recordButton = findViewById(R.id.record_button);
        recordButton.setRecordView(recordView);

        // Hide send button initially
        chatsBinding.sendBtn.setVisibility(View.GONE);
        recordButton.setVisibility(View.VISIBLE);

        recordView.setLockEnabled(true);
        recordView.setRecordLockImageView(findViewById(R.id.record_lock));
        recordView.setRecordLockImageView(findViewById(R.id.record_lock));

        chatsBinding.nameStatusLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatsActivity.this, ProfileActivity.class);
                intent.putExtra("imageUri", profile_image_uri);
                intent.putExtra("uid", receiverUid);
                startActivity(intent);
            }
        });

        messages = new ArrayList<>();
        messagesAdapter = new MessagesAdapter(this, messages);
        chatsBinding.chatRecyclerView.setHasFixedSize(true);
        chatsBinding.chatRecyclerView.setItemViewCacheSize(20);
        chatsBinding.chatRecyclerView.setDrawingCacheEnabled(true);
        chatsBinding.chatRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        chatsBinding.chatRecyclerView.setLayoutManager(linearLayoutManager);
        chatsBinding.chatRecyclerView.setAdapter(messagesAdapter);

        chatsBinding.messageBox.addTextChangedListener(new TextWatcher() {
            private Handler typingHandler = new Handler();
            private Runnable typingRunnable = () -> {
                userStatusObserver.setUserTypingStatus("online");
            };

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()==0){
                    chatsBinding.camera.setVisibility(View.VISIBLE);
                    chatsBinding.sendBtn.setVisibility(View.GONE);
                    recordButton.setVisibility(View.VISIBLE);
                }else {
                    chatsBinding.camera.setVisibility(View.GONE);
                    chatsBinding.sendBtn.setVisibility(View.VISIBLE);
                    recordButton.setVisibility(View.GONE);
                }
                userStatusObserver.setUserTypingStatus("typing...");

                typingHandler.removeCallbacks(typingRunnable);
                typingHandler.postDelayed(typingRunnable, 2000);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    userStatusObserver.setUserTypingStatus("online");
                }
                int lineCount = chatsBinding.messageBox.getLineCount();
                if (lineCount > 1 && lineCount <= 6) {
                    chatsBinding.messageBox.setMaxLines(lineCount);
                } else if (lineCount > 6) {
                    chatsBinding.messageBox.setMaxLines(6);
                }
            }
        });
        chatsBinding.sendBtn.setOnClickListener(v -> {
            String messageText = chatsBinding.messageBox.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessage(messageText);
            }
        });
        if (setThemeForHomeScreen() == 2) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.GreenishBlue));
            chatsBinding.toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.GreenishBlue));
        } else {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
            chatsBinding.toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.black));
        }

        recordButton.setOnRecordClickListener(new OnRecordClickListener() {
            @Override
            public void onClick(View v) {
                chatsBinding.cardView.setVisibility(View.VISIBLE);
                if (myWhatsAppPermissions.isRecordingOk(ChatsActivity.this) && myWhatsAppPermissions.isMediaOk(ChatsActivity.this)) {
                    recordButton.setListenForRecord(true);
                    Log.d("RecordButton", "RECORD BUTTON CLICKED");
                } else {
                    if (!myWhatsAppPermissions.isRecordingOk(ChatsActivity.this)) {
                        myWhatsAppPermissions.requestRecordingPermission(ChatsActivity.this);
                    }
                    if (!myWhatsAppPermissions.isMediaOk(ChatsActivity.this)) {
                        myWhatsAppPermissions.requestMediaPermission(ChatsActivity.this);
                    }
                }
            }
        });

        // RecordView listener
        recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                if (myWhatsAppPermissions.isRecordingOk(ChatsActivity.this) && myWhatsAppPermissions.isMediaOk(ChatsActivity.this)) {
                    new Handler().postDelayed(() -> {
                        initMediaRecorder();
                        Toast.makeText(ChatsActivity.this, "Recording started", Toast.LENGTH_SHORT).show();
                    }, 1000);
                    chatsBinding.cardView.setVisibility(View.GONE);
                    chatsBinding.recordingCardview.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(ChatsActivity.this, "Recording permission denied", Toast.LENGTH_SHORT).show();
                    if (!myWhatsAppPermissions.isRecordingOk(ChatsActivity.this)) {
                        myWhatsAppPermissions.requestRecordingPermission(ChatsActivity.this);
                    }
                    if (!myWhatsAppPermissions.isMediaOk(ChatsActivity.this)) {
                        myWhatsAppPermissions.requestMediaPermission(ChatsActivity.this);
                    }
                }
            }

            @Override
            public void onCancel() {
                // On Swipe To Cancel

//                stopRecording();
                if(mediaRecorder!=null){
                    try {
                        mediaRecorder.stop();
                        mediaRecorder.release();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                Toast.makeText(ChatsActivity.this, "Recording cancelled", Toast.LENGTH_SHORT).show();
                Log.d("RecordView", "Recording canceled");
                if(audioFilePath!=null) {
                    File file = new File(audioFilePath);
                    if (file.exists())
                        file.delete();
                }
                chatsBinding.cardView.setVisibility(View.VISIBLE);
                chatsBinding.recordingCardview.setVisibility(View.GONE);
            }

            @Override
            public void onFinish(long recordTime, boolean limitReached) {
                try {
                        mediaRecorder.stop();
                        mediaRecorder.release();
                }catch (Exception e){
                    e.printStackTrace();
                }
//                Toast.makeText(ChatsActivity.this, "onFinish called", Toast.LENGTH_SHORT).show();
                String time = getHumanTimeText(recordTime);
                Log.d("RecordView", "Recording finished");
                Log.d("RecordTime", time);
                Log.d("Record save path", "recording save here: "+audioFilePath);

                // Send the recorded audio file
                sendMessage(audioFilePath,true);

                chatsBinding.cardView.setVisibility(View.VISIBLE);
                chatsBinding.recordingCardview.setVisibility(View.GONE);
            }

            @Override
            public void onLessThanSecond() {
                // When the record time is less than One Second
                if (mediaRecorder != null) {
                    try {
                        mediaRecorder.reset();
                    } catch (IllegalStateException e) {
                        Log.e("RecordView", "Failed to stop MediaRecorder: " + e.getMessage());
                    }
                    mediaRecorder.release();
                    mediaRecorder = null;
                }

                Log.d("RecordView", "Recording too short");

                // Optionally delete the recorded file if too short
                if (audioFilePath != null) {
                    File file = new File(audioFilePath);
                    if (file.exists()) {
                        file.delete();
                    }
                }

                chatsBinding.cardView.setVisibility(View.VISIBLE);
                chatsBinding.recordingCardview.setVisibility(View.GONE);
            }


            @Override
            public void onLock() {
                // When Lock gets activated
                chatsBinding.recordLock.setLockColor(ContextCompat.getColor(ChatsActivity.this,R.color.white));
                chatsBinding.recordLock.setCircleLockedColor(ContextCompat.getColor(ChatsActivity.this,R.color.GreenishBlue));
                chatsBinding.recordLock.animate();
                chatsBinding.recordLock.bringToFront();
                chatsBinding.recordLock.setActivated(true);
                chatsBinding.recordLock.setVisibility(View.VISIBLE);
                Log.d("RecordView", "Recording locked");
            }
        });
        chatsBinding.moreVertical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpVerticalMore();
            }
        });

//        new Thread(() -> {
//            List<Message> initialMessages = messageDao.getLast20Messages(senderRoom);
//            if (!initialMessages.isEmpty()) {
//                lastFetchedTime = initialMessages.get(initialMessages.size() - 1).getTimeStamp();
//                runOnUiThread(() -> {
//                    messages.clear();
//                    messages.addAll(0, initialMessages);
//                    messagesAdapter.notifyDataSetChanged();
//                    chatsBinding.chatRecyclerView.scrollToPosition(messages.size() - 1);
//                });
//                // Load remaining messages in the background
//                loadRemainingMessages();
//            } else {
//                // If no messages are found in the local database, fetch from Firebase
//                fetchMessagesFromFirebase();
//            }
//        }).start();
        fetchMessages();

        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Message message = snapshot.getValue(Message.class);
                        if (message != null && !messageKeys.contains(message.getKey())) {
                            messageKeys.add(message.getKey());
                            messages.add(message);
                            messagesAdapter.notifyItemInserted(messages.size() - 1);
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
        checkIndexes();
    }
    private void fetchMessages() {
//        Toast.makeText(this, "Fetching messages from local", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            Log.d("fetchMessages", "Fetching messages for senderRoom: " + senderRoom);
            List<Message> initialMessages = messageDao.getLast20Messages(senderRoom);
            Log.d("fetchMessages", "Initial messages fetched: " + initialMessages.size());
            if (!initialMessages.isEmpty()) {
                lastFetchedTime = initialMessages.get(initialMessages.size() - 1).getTimeStamp();
                runOnUiThread(() -> {
                    messages.clear();
                    messages.addAll(0, initialMessages);
                    messagesAdapter.notifyDataSetChanged();
                    for (Message message : messages) {
                        System.out.println(message.getMessage());
                    }
                    chatsBinding.chatRecyclerView.scrollToPosition(messages.size() - 1);
                });
                // Load remaining messages in the background
                loadRemainingMessages();
            } else {
                Log.e("fetchMessages", "Initial messages are null");
            }
        }).start();
    }

    private void loadRemainingMessages() {
        new Thread(() -> {
            Log.d("loadRemainingMessages", "Loading remaining messages for senderRoom: " + senderRoom);
            List<Message> remainingMessages = messageDao.getMessagesAfter(senderRoom, lastFetchedTime);
            Log.d("loadRemainingMessages", "Remaining messages fetched: " + remainingMessages.size());
            runOnUiThread(() -> {
                int currentSize = messages.size();
                messages.addAll(0, remainingMessages);
                messagesAdapter.notifyItemRangeInserted(currentSize, remainingMessages.size());
            });
        }).start();
    }

    public void checkIndexes() {
        AppDatabase db = DatabaseClient.getInstance(this).getAppDatabase();
        try (Cursor cursor = db.getOpenHelper().getReadableDatabase().query("PRAGMA index_list(messages)")) {
            while (cursor.moveToNext()) {
                String indexName = cursor.getString(cursor.getColumnIndex("name"));
                Log.d("Database Index", "Index found: " + indexName);
            }
        }catch (Exception e){
            Log.d("checkIndexes","indexes not found");
            e.printStackTrace();
        }
    }




    private void sendMessage(String messageText) {
        Date date = new Date();
        String randomKey = database.getReference().push().getKey();
        Message message = new Message(messageText, senderUid, date.getTime(), randomKey, Message.STATUS_SENT, false,senderRoom,receiverRoom);
        Log.d("sendMessage", "Message senderId: " + senderUid);
        saveMessageToDatabase(message);

        chatsBinding.messageBox.setText("");

        HashMap<String, Object> lastMessage = new HashMap<>();
        lastMessage.put("lastMessage", message.getMessage());
        lastMessage.put("lastMessageTime", date.getTime());
        lastMessage.put("isAudioMessage", false);
        updateLastMessage(lastMessage, senderRoom);
        updateLastMessage(lastMessage, receiverRoom);

        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .child(randomKey)
                .setValue(message)
                .addOnSuccessListener(unused -> {
                    database.getReference().child("chats")
                            .child(receiverRoom)
                            .child("messages")
                            .child(randomKey)
                            .setValue(message)
                            .addOnSuccessListener(unused1 -> {
                                sendNotification(message, receiverFcmToken, randomKey);
                            });
                });
    }

    private void sendMessage(String audioMessage, boolean isAudioMessage) {
        Date date = new Date();
        String randomKey = database.getReference().push().getKey();
        Message message = new Message(audioMessage, senderUid, date.getTime(), randomKey, Message.STATUS_SENT, isAudioMessage,senderRoom,receiverRoom);

        Log.d("sendMessage", "Audio Message senderId: " + senderUid);
//
//        database.getReference().child("chats")
//                .child(senderRoom)
//                .child("messages")
//                .addChildEventListener(new ChildEventListener() {
//                    @Override
//                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                        Message message = snapshot.getValue(Message.class);
//                        if (message != null && !messageKeys.contains(message.getKey())) {
//                            messageKeys.add(message.getKey());
//                            messages.add(message);
//                            messagesAdapter.notifyItemInserted(messages.size() - 1);
//                        }
//                    }
//
//                    @Override
//                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
//
//                    @Override
//                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
//
//                    @Override
//                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {}
//                });
        saveMessageToDatabase(message);
        sendAudioMessage(audioMessage, uriOnFirebase -> {
            message.setMessage(uriOnFirebase);
            chatsBinding.messageBox.setText("");

            HashMap<String, Object> lastMessage = new HashMap<>();
            lastMessage.put("lastMessage", message.getMessage());
            lastMessage.put("lastMessageTime", date.getTime());
            lastMessage.put("isAudioMessage", isAudioMessage);
            updateLastMessage(lastMessage, senderRoom);
            updateLastMessage(lastMessage, receiverRoom);

            database.getReference().child("chats")
                    .child(senderRoom)
                    .child("messages")
                    .child(randomKey)
                    .setValue(message)
                    .addOnSuccessListener(unused -> {
                        database.getReference().child("chats")
                                .child(receiverRoom)
                                .child("messages")
                                .child(randomKey)
                                .setValue(message)
                                .addOnSuccessListener(unused1 -> {
                                    sendNotification(message, receiverFcmToken, randomKey);
                                });
                    });
        });
    }

    private void saveMessageToDatabase(Message message) {
        new Thread(() -> {
            appDatabase.messageDao().insertMessage(message);
        }).start();
    }

    private void setUpVerticalMore() {
        PopupMenu popupMenu=new PopupMenu(this,chatsBinding.moreVertical);
        getMenuInflater().inflate(R.menu.menu_chat,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId()==R.id.view_contact){
                    Intent intent = new Intent(ChatsActivity.this, ProfileActivity.class);
                    intent.putExtra("imageUri", profile_image_uri);
                    intent.putExtra("uid", receiverUid);
                    startActivity(intent);
                } else if (item.getItemId()==R.id.block) {

                }else if (item.getItemId()==R.id.wallpaper) {
                    Intent intent=new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");

                }else if (item.getItemId()==R.id.clear_chat) {
                    Dialog dialog=new Dialog(ChatsActivity.this);
                    dialog.setContentView(R.layout.logout_confirmation_dialog);
                    Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(null);
                    TextView header=dialog.findViewById(R.id.header);
                    TextView confirm=dialog.findViewById(R.id.confirm);
                    TextView cancel=dialog.findViewById(R.id.cancel);
                    dialog.findViewById(R.id.account_name).setVisibility(View.GONE);
                    dialog.findViewById(R.id.note).setVisibility(View.VISIBLE);
                    header.setText("Do you want to clear this chat?");
                    confirm.setText("Clear chat");
                    dialog.findViewById(R.id.cancel).setOnClickListener(v -> dialog.dismiss());
                    confirm.setOnClickListener(v -> executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            appDatabase.messageDao().deleteAllMessages();
                            database.getReference().child("chats")
                                    .child(senderRoom)
                                    .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    messagesAdapter.notifyDataSetChanged();
                                                    Toast.makeText(ChatsActivity.this, "cleared chat successfully", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    });
                            dialog.dismiss();
                        }
                    }));
                    dialog.show();

                    return true;

                }else if (item.getItemId()==R.id.view_contact) {

                }
                popupMenu.dismiss();
                return false;
            }
        });
        popupMenu.show();
    }

    private void updateLastMessage(HashMap<String, Object> lastMessage, String room) {
        database.getReference().child("chats").child(room).updateChildren(lastMessage);
    }


    private void sendAudioMessage(String audioFilePath, OnUriReceivedListener listener) {
        if (audioFilePath != null) {
            File audioFile = new File(audioFilePath);
            if (audioFile.exists()) {
                Uri audioUri = Uri.fromFile(audioFile);
                if (audioUri != null) {

                    StorageReference reference = FirebaseStorage.getInstance().getReference().child("chat recording").child(recordingName);
                    reference.putFile(audioUri).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            reference.getDownloadUrl().addOnSuccessListener(uri -> {
                                if (uri != null) {
                                    String uriOnFirebase = uri.toString();
                                    if (listener != null) {
                                        listener.onUriReceived(uriOnFirebase);
                                    }
                                }
                            });
                        } else {
                            Log.d("sendAudioMessage", "upload failed: " + task.getException());
                        }
                    });
                } else {
                    Log.e("Chat", "Audio URI is null: " + audioFilePath);
                }
            } else {
                Log.d("sendAudioMethod", "Audio file is missing");
            }
        } else {
            Log.d("sendAudioMethod", "Audio path is null");
        }
    }


    // Define an interface to handle callback when URI is received
    public interface OnUriReceivedListener {
        void onUriReceived(String uri);
    }

    private void initMediaRecorder() {

    if (mediaRecorder != null) {
        mediaRecorder.release();
        mediaRecorder = null;
    }
    mediaRecorder = new MediaRecorder();
    ContentResolver resolver = getContentResolver();
    ContentValues contentValues = new ContentValues();
    long currentTimeMillis = System.currentTimeMillis();
    String fileCreatedTime = formatAudioCreatedTime(currentTimeMillis)[0] + "_" + formatAudioCreatedTime(currentTimeMillis)[1];
    String uniqueID = UUID.randomUUID().toString().substring(0, 8);
    recordingName="MyAud-" + fileCreatedTime + "_" + uniqueID + ".3gp";
    contentValues.put(MediaStore.Audio.Media.DISPLAY_NAME, recordingName );
    contentValues.put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp");
    contentValues.put(MediaStore.Audio.Media.RELATIVE_PATH, "Android/media/com.saif.mywhatsapp/MyWhatsApp/Media/ChatRecordings");

    Uri audioUri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues);
    Log.d("initMediaPlayer","audio uri: "+audioUri);

    if (audioUri != null) {
        audioFilePath = getPathFromUri(audioUri);
        Log.d("initMediaPlayer","audioFilePath: "+audioFilePath);
        try {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(resolver.openFileDescriptor(audioUri, "w").getFileDescriptor());
            mediaRecorder.prepare();
            mediaRecorder.start();
            Log.d("ChatsActivity", "initMediaRecorder: audioPath is :- " + audioFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    } else {
        Toast.makeText(this, "Unable to create audio file", Toast.LENGTH_SHORT).show();
    }
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
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MyWhatsAppPermissions.REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 &&  grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults.length>1 && grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults.length>2 && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, you can now start recording
                chatsBinding.recordButton.setListenForRecord(true);
                Toast.makeText(this, "RECORD BUTTON CLICKED", Toast.LENGTH_SHORT).show();
                Log.d("RecordButton", "RECORD BUTTON CLICKED");
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getHumanTimeText(long milliseconds) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent.hasExtra("userId")) {
            receiverUid = intent.getStringExtra("userId");
        }
        //fetchMessages();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("receiverUid", receiverUid);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        receiverUid = savedInstanceState.getString("receiverUid");
    }

    @Override
    protected void onStart() {
        super.onStart();
//        loadMessages();
        markMessagesAsSeen();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        loadMessages();
        markMessagesAsSeen();
    }

    @Override
    protected void onPause() {
        super.onPause();
        markMessagesAsSeen();
    }

    private void markMessagesAsSeen() {
        DatabaseReference chatReference = database.getReference().child("chats").child(senderRoom).child("messages");
        String senderUid = FirebaseAuth.getInstance().getUid();

        chatReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean shouldUpdate = false;
                Log.d("makrMessageAsseen","data changed");
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Message message = dataSnapshot.getValue(Message.class);
                    if (message != null) {
//                        Log.d("markMessagesAsSeen","message is "+message+" sender is "+message.getSenderId());
                        if (message.getSenderId()!=null && !message.getSenderId().equals(senderUid) && message.getStatus() == Message.STATUS_DELIVERED ) {
                            message.setStatus(Message.STATUS_READ);
                            dataSnapshot.getRef().setValue(message);

                            // Also update the status in the receiver's room
                            database.getReference().child("chats").child(receiverRoom).child("messages").child(message.getKey()).child("status").setValue(Message.STATUS_READ);
                            shouldUpdate = true;
                        }
                        // Update the local database with the message status from Firebase
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                if(messageDao.getMessageByKey(message.getKey())!=null)
                                    updateLocalDatabase(message);
                            }
                        });

                    }
                }
                if (shouldUpdate) {
                    messagesAdapter.notifyDataSetChanged();
                    chatsBinding.chatRecyclerView.scrollToPosition(messagesAdapter.getItemCount() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if needed
            }
        });
    }

    private void updateLocalDatabase(Message message) {
        new Thread(() -> {
            appDatabase.messageDao().setMessageStatus(message.getKey(),message.getStatus());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    Toast.makeText(ChatsActivity.this, "message status changed", Toast.LENGTH_SHORT).show();
                    messagesAdapter.notifyDataSetChanged();
                }
            });
        }).start();
    }



    private void sendNotification(Message message, String receiverFcmToken, String randomKey) {
//        String senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database.getReference().child("Users").child(senderUid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User senderUser = task.getResult().getValue(User.class);
                if (senderUser != null) {
                    try {
                        JSONObject messageObject = new JSONObject();
                        JSONObject dataObject = new JSONObject();

                        dataObject.put("title", senderUser.getName());
                        dataObject.put("body", message.getMessage());
                        dataObject.put("userId", senderUid);
                        dataObject.put("timeStamp", String.valueOf(message.getTimeStamp())); // Convert to String if necessary
                        dataObject.put("key", randomKey);
                        dataObject.put("status", "0"); // Ensure status is sent as a String
                        dataObject.put("isAudio", String.valueOf(message.isAudioMessage())); // Convert boolean to String
                        dataObject.put("senderRoom", senderRoom);
                        dataObject.put("receiverRoom", receiverRoom);
                        dataObject.put("Contact_name", senderUser.getName());
                        dataObject.put("chat_profile", senderUser.getProfileImage());
                        dataObject.put("number", senderUser.getPhoneNumber());
                        dataObject.put("receiverFcmToken", senderUser.getFcmToken());

                        messageObject.put("token", receiverFcmToken);
                        messageObject.put("data", dataObject);

                        JSONObject requestBody = new JSONObject();
                        requestBody.put("message", messageObject);

                        new SendNotificationTask().execute(requestBody);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    // AsyncTask to handle network operations
    private class SendNotificationTask extends AsyncTask<JSONObject, Void, Void> {
        @Override
        protected Void doInBackground(JSONObject... jsonObjects) {
            MediaType JSON = MediaType.get("application/json");
            OkHttpClient client = new OkHttpClient();

            try {
                String accessToken = AuthUtil.getAccessToken(ChatsActivity.this);
                String url = "https://fcm.googleapis.com/v1/projects/mywhatsapp-2d301/messages:send";
                RequestBody body = RequestBody.create(jsonObjects[0].toString(), JSON);
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .header("Authorization", "Bearer " + accessToken)
                        .build();

                Response response = client.newCall(request).execute();
                Log.d("SendNotificationTask", "Response: " + response.body().string());
            } catch (IOException e) {
                Log.e("SendNotificationTask", "Request failed: " + e.getMessage());
            }
            return null;
        }
    }

    private int setThemeForHomeScreen() {
        int nightModeFlags = this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int color;
        int color2;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                color = ContextCompat.getColor(this, R.color.primaryTextColor); // White for dark mode
                color2 = ContextCompat.getColor(this, R.color.secondaryTextColor); // White for dark mode
                int iconColor = ContextCompat.getColor(this, R.color.white); // White for dark mode icons
                int toolbarColor = ContextCompat.getColor(this, R.color.receive_message);
                chatsBinding.name.setTextColor(color);
                chatsBinding.status.setTextColor(color);
                chatsBinding.main.setBackgroundColor(color);
                chatsBinding.main.setBackgroundResource(R.drawable.chat_background);
                chatsBinding.bottomScrollBtn.setBackgroundColor(ContextCompat.getColor(this,R.color.backgroundColor));
                chatsBinding.toolbar.setBackgroundColor(toolbarColor);
                chatsBinding.bottomScrollBtn.setBackgroundResource(R.drawable.circle_bg);
                chatsBinding.cardView.setCardBackgroundColor(ContextCompat.getColor(this,R.color.night_color_background));
                chatsBinding.recordingCardview.setCardBackgroundColor(ContextCompat.getColor(this,R.color.send_message));
//                chatsBinding.bottomScrollBtn.setBackground(getDrawable(R.drawable.circle_bg) );
                chatsBinding.bottomScrollBtn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.white));
                chatsBinding.emoji.setColorFilter(iconColor);
                chatsBinding.attachment.setColorFilter(iconColor);
                chatsBinding.camera.setColorFilter(iconColor);
//                chatsBinding.recordView.setSlideToCancelTextColor(ContextCompat.getColor(this,R.color.Teal));
                chatsBinding.recordView.setCounterTimeColor(ContextCompat.getColor(this,R.color.white));
//                chatsBinding.recordView.setSlideToCancelArrowColor(ContextCompat.getColor(this,R.color.white));
                return 1;
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
            default:
                color = ContextCompat.getColor(this, R.color.white); // Black for light mode
                color2 = ContextCompat.getColor(this, R.color.secondaryTextColor); // Black for light mode
//                iconColor = ContextCompat.getColor(this, R.color.grey); // White for dark mode icons
                chatsBinding.name.setTextColor(color);
                chatsBinding.status.setTextColor(color);
                chatsBinding.main.setBackgroundResource(R.drawable.bg);
                chatsBinding.bottomScrollBtn.setBackgroundResource(R.drawable.circle_bg);
                chatsBinding.bottomScrollBtn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.white));
                return 2;
        }
    }
    private void checkLastMessageVisibility() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) chatsBinding.chatRecyclerView.getLayoutManager();
        int lastVisibleItemPosition=0;
        if(layoutManager!=null)
            lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();// give the last visible message on screen
        int lastMessagePosition = messagesAdapter.getItemCount() - 1;

        if (lastVisibleItemPosition == lastMessagePosition) {
            chatsBinding.bottomScrollBtn.setVisibility(View.GONE);
        } else {
            chatsBinding.bottomScrollBtn.setVisibility(View.VISIBLE);
            chatsBinding.bottomScrollBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chatsBinding.chatRecyclerView.scrollToPosition(lastMessagePosition);
                }
            });
        }
    }

    private void listenForUserStatus(String receiverUid) {
        receiverReference = database.getReference()
                .child("Users")
                .child(receiverUid);
        receiverReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.child("status").getValue(String.class);
                    if (snapshot.child("lastSeen").exists()) {
                        lastSeen = snapshot.child("lastSeen").getValue(Long.class);
                    } else {
                        lastSeen = Long.parseLong("1719049687353");
                    }
                    if (!receiverUid.equals(senderUid)) {
                        if ("typing...".equals(status)) {
                            chatsBinding.status.setText("typing...");
                        } else if ("online".equals(status)) {
                            chatsBinding.status.setText("online");
                        } else if ("offline".equals(status)) {
                            chatsBinding.status.setText("last seen " + formatLastSeen(lastSeen));
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if needed
            }
        });
    }
    private String formatLastSeen(long lastSeenTimestamp) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        String time = timeFormat.format(new Date(lastSeenTimestamp));
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
        String lastSeenDate = dateFormat.format(new Date(lastSeenTimestamp));
        String currentDate = dateFormat.format(new Date());
        if (lastSeenDate.equals(currentDate)) {
            return "Today at " + time;
        } else if (System.currentTimeMillis() - lastSeenTimestamp < 86400000) {
            return "Yesterday at " + time;
        } else {
            return "on " + lastSeenDate + " at " + time;
        }
    }
    private String[] formatAudioCreatedTime(long audioCreatedTime) {
        SimpleDateFormat formatTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String recordAt = formatTime.format(new Date(audioCreatedTime));
        SimpleDateFormat formatDate = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
        String recordOn = formatDate.format(new Date(audioCreatedTime));
        return new String[]{recordOn,recordAt};
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

            if(item.getItemId()==R.id.view_contact){
                Intent intent = new Intent(ChatsActivity.this, ProfileActivity.class);
                intent.putExtra("imageUri", profile_image_uri);
                intent.putExtra("uid", receiverUid);
                startActivity(intent);
            } else if (item.getItemId()==R.id.block) {

            }else if (item.getItemId()==R.id.wallpaper) {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");

            }else if (item.getItemId()==R.id.clear_chat) {
                Dialog dialog=new Dialog(this);
                dialog.setContentView(R.layout.logout_confirmation_dialog);
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(null);
                TextView header=dialog.findViewById(R.id.header);
                TextView confirm=dialog.findViewById(R.id.confirm);
                TextView cancel=dialog.findViewById(R.id.cancel);
                dialog.findViewById(R.id.account_name).setVisibility(View.GONE);
                dialog.findViewById(R.id.note).setVisibility(View.VISIBLE);
                header.setText("Do you want to clear this chat?");
                confirm.setText("Clear chat");
                dialog.findViewById(R.id.cancel).setOnClickListener(v -> dialog.dismiss());
                confirm.setOnClickListener(v -> executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        appDatabase.messageDao().deleteAllMessages();
                        database.getReference().child("chats")
                                .child(senderRoom)
                                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                messagesAdapter.notifyDataSetChanged();
                                                Toast.makeText(ChatsActivity.this, "cleared chat successfully", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                        dialog.dismiss();
                    }
                }));
                dialog.show();

                return true;

            }else if (item.getItemId()==R.id.view_contact) {

            }else if (item.getItemId()==R.id.view_contact) {

            }
        return super.onOptionsItemSelected(item);
    }
}

