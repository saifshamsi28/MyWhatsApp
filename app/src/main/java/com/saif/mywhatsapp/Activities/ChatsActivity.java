package com.saif.mywhatsapp.Activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.saif.mywhatsapp.Adapters.MessagesAdapter;
import com.saif.mywhatsapp.AppDatabase;
import com.saif.mywhatsapp.AuthUtil;
import com.saif.mywhatsapp.DatabaseClient;
import com.saif.mywhatsapp.Models.Message;
import com.saif.mywhatsapp.Models.User;
import com.saif.mywhatsapp.R;
import com.saif.mywhatsapp.databinding.ActivityChatsBinding;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatsActivity extends AppCompatActivity {

    ActivityChatsBinding chatsBinding;
    Toolbar toolbar;
    TextView chatName;
    private View rootLayout;
    ImageView chatImage, backButton;
    MessagesAdapter messagesAdapter;
    ArrayList<Message> messages;
    String senderRoom, receiverRoom,receiverName;
    FirebaseDatabase database;
    static ImageView contactImage;
    private User user;
    private boolean isOnline = false;
    private AppDatabase appDatabase;
    private long lastSeen;
    String receiverUid,receiverFcmToken;
    private final Executor executor = Executors.newSingleThreadExecutor();

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
        rootLayout = findViewById(R.id.main);
        database = FirebaseDatabase.getInstance();
        appDatabase = DatabaseClient.getInstance(this).getAppDatabase();

        chatsBinding.status.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        chatsBinding.status.setSingleLine(true);
        chatsBinding.status.setMarqueeRepeatLimit(0);
        chatsBinding.status.setSelected(true);


        receiverName = getIntent().getStringExtra("Contact_name");
        chatsBinding.name.setText(receiverName);
        String contactNumber = getIntent().getStringExtra("number");
        String profile_image_uri = getIntent().getStringExtra("chat_profile");
        receiverUid = getIntent().getStringExtra("userId");
        receiverFcmToken = getIntent().getStringExtra("receiverFcmToken");

        // to get the user's status and last seen updates
        listenForUserStatus(receiverUid);

        String senderUid = FirebaseAuth.getInstance().getUid();
        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;
        contactImage = Glide.with(getBaseContext())
                .load(profile_image_uri)
                .placeholder(R.drawable.avatar)
                .into(chatsBinding.profile).getView();

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
                chatsBinding.chatRecyclerView.setTranslationZ(-5f);
            } else {
                // Keyboard is closed
                chatsBinding.cardView.setTranslationY(0);
                chatsBinding.sendBtn.setTranslationY(0);
                chatsBinding.chatRecyclerView.setTranslationY(0);
                chatsBinding.chatRecyclerView.setTranslationZ(-5f);
                chatsBinding.chatRecyclerView.scrollToPosition(messagesAdapter.getItemCount() - 1);
            }
            return WindowInsetsCompat.CONSUMED;
        });

        chatsBinding.chatRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                checkLastMessageVisibility();
            }
        });

        chatsBinding.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatsActivity.this, ProfileActivity.class);
//                intent.putExtra("name",receiverName);
//                intent.putExtra("number",contactNumber);
                intent.putExtra("imageUri", profile_image_uri.toString());
                intent.putExtra("uid", receiverUid);
                startActivity(intent);
            }
        });

        messages = new ArrayList<>();
        messagesAdapter = new MessagesAdapter(this, messages);
        chatsBinding.chatRecyclerView.setAdapter(messagesAdapter);

        database.getReference()
                .child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Message message = dataSnapshot.getValue(Message.class);
                            messages.add(message);
                        }
                        messagesAdapter.notifyDataSetChanged();
                        chatsBinding.chatRecyclerView.scrollToPosition(messagesAdapter.getItemCount() - 1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        chatsBinding.messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            //to dynamically change the height of message box based on message length
            public void afterTextChanged(Editable s) {
                if (chatsBinding.messageBox.getText().toString().isEmpty()) {
                    chatsBinding.camera.setVisibility(View.VISIBLE);
                } else {
                    chatsBinding.camera.setVisibility(View.GONE);
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
            chatsBinding.camera.setVisibility(View.VISIBLE);
            String messageText = chatsBinding.messageBox.getText().toString().trim();
            if (!messageText.isEmpty()) {
                Date date = new Date();
                Message message = new Message(messageText, senderUid, date.getTime());
                chatsBinding.messageBox.setText("");

                String randomKey = database.getReference().push().getKey();
                HashMap<String, Object> lastMessage = new HashMap<>();
                lastMessage.put("lastMessage", message.getMessage());
                lastMessage.put("lastMessageTime", date.getTime());
                database.getReference().child("chats").child(senderRoom).updateChildren(lastMessage);
                database.getReference().child("chats").child(receiverRoom).updateChildren(lastMessage);

                database.getReference().child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message)
                        .addOnSuccessListener(unused -> database.getReference().child("chats")
                                .child(receiverRoom)
                                .child("messages")
                                .child(randomKey)
                                .setValue(message)
                                .addOnSuccessListener(unused1 -> {
                                    sendNotification(message);
                                    // Toast.makeText(ChatsActivity.this, "message sent successfully", Toast.LENGTH_SHORT).show();
                                }));
                chatsBinding.chatRecyclerView.scrollToPosition(messagesAdapter.getItemCount());
            }
        });

        if (setThemeForHomeScreen() == 2) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.GreenishBlue));
            chatsBinding.toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.GreenishBlue));
        } else {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
            chatsBinding.toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.black));
        }
    }

    // Method to send notification
    // Method to send notification
    private void sendNotification(Message message) {
        String senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database.getReference().child("Users")
                .child(senderUid)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        User senderUser = task.getResult().getValue(User.class);
                        if (senderUser != null) {
                            try {
                                JSONObject jsonObject = new JSONObject();
                                JSONObject messageObject = new JSONObject();
                                JSONObject notificationObject = new JSONObject();
                                JSONObject dataObject = new JSONObject();

                                notificationObject.put("title", senderUser.getName());
                                notificationObject.put("body", message.getMessage());

                                dataObject.put("userId", senderUid);
                                dataObject.put("Contact_name", senderUser.getName());
                                dataObject.put("chat_profile", senderUser.getProfileImage());
                                dataObject.put("number", senderUser.getPhoneNumber());
                                dataObject.put("receiverFcmToken", senderUser.getFcmToken());

                                messageObject.put("token", receiverFcmToken);
                                messageObject.put("notification", notificationObject);
                                messageObject.put("data", dataObject);

                                jsonObject.put("message", messageObject);

                                new SendNotificationTask().execute(jsonObject);
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
                chatsBinding.name.setTextColor(color);
                chatsBinding.status.setTextColor(color);
                chatsBinding.main.setBackgroundColor(color);
                chatsBinding.main.setBackgroundResource(R.drawable.chat_background);
                chatsBinding.bottomScrollBtn.setBackgroundColor(ContextCompat.getColor(this,R.color.backgroundColor));

                chatsBinding.bottomScrollBtn.setBackgroundResource(R.drawable.circle_bg);
//                chatsBinding.bottomScrollBtn.setBackground(getDrawable(R.drawable.circle_bg) );
                chatsBinding.bottomScrollBtn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.white));
                return 1;
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
            default:
                color = ContextCompat.getColor(this, R.color.white); // Black for light mode
                color2 = ContextCompat.getColor(this, R.color.secondaryTextColor); // Black for light mode
                chatsBinding.name.setTextColor(color);
                chatsBinding.status.setTextColor(color);
                chatsBinding.main.setBackgroundResource(R.drawable.bg);
                chatsBinding.bottomScrollBtn.setBackgroundResource(R.drawable.circle_bg);
//                chatsBinding.bottomScrollBtn.setBackground(getDrawable(R.drawable.circle_bg) );
                chatsBinding.bottomScrollBtn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.white));
                return 2;
        }
    }

    private void checkLastMessageVisibility() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) chatsBinding.chatRecyclerView.getLayoutManager();
        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();// give the last visible message on screen
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
        database.getReference()
                .child("Users")
                .child(receiverUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String status = snapshot.child("status").getValue(String.class);
                            if(snapshot.child("lastSeen").exists()) {
                                lastSeen = snapshot.child("lastSeen").getValue(Long.class);
                            }else {
                                lastSeen=Long.parseLong("1719049687353");
                            }
                            if(!receiverUid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                if ("online".equals(status)) {
                                    chatsBinding.status.setText("online");
                                } else {
                                    chatsBinding.status.setText("last seen " + formatLastSeen(lastSeen));
                                }
                            }else {
                                chatsBinding.status.setText("Message yourself");
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
}

