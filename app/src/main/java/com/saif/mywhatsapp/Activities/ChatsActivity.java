package com.saif.mywhatsapp.Activities;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.saif.mywhatsapp.Adapters.MessagesAdapter;
import com.saif.mywhatsapp.Models.Message;
import com.saif.mywhatsapp.R;
import com.saif.mywhatsapp.databinding.ActivityChatsBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
public class ChatsActivity extends AppCompatActivity {

    ActivityChatsBinding chatsBinding;
    Toolbar toolbar;
    TextView chatName;
    private View rootLayout;
    ImageView chatImage, backButton;
    MessagesAdapter messagesAdapter;
    ArrayList<Message> messages;
    String senderRoom, receiverRoom;
    FirebaseDatabase database;
    static ImageView contactImage;

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
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.GreenishBlue));

        chatsBinding.toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.GreenishBlue));

        rootLayout = findViewById(R.id.main);
        database = FirebaseDatabase.getInstance();

        String receiverName = getIntent().getStringExtra("Contact_name");
        chatsBinding.name.setText(receiverName);
        String contactNumber=getIntent().getStringExtra("number");
        String profile_image_uri = getIntent().getStringExtra("chat_profile");
        String receiverUid = getIntent().getStringExtra("uid");
        String senderUid = FirebaseAuth.getInstance().getUid();
        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        contactImage=Glide.with(getBaseContext())
                .load(profile_image_uri)
                .placeholder(R.drawable.avatar)
                .into(chatsBinding.profile).getView();

        chatsBinding.backBtn.setOnClickListener(v -> finish());
        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootLayout.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootLayout.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;

                if (keypadHeight > screenHeight * 0.15) {
                    // Keyboard is opened
                    chatsBinding.cardView.setTranslationY(-keypadHeight * 0.88f);
                    chatsBinding.sendBtn.setTranslationY(-keypadHeight * 0.88f);
                    chatsBinding.chatRecyclerView.setTranslationY(-keypadHeight * 0.88f);
                    chatsBinding.chatRecyclerView.setTranslationZ(-1f);
                } else {
                    // Keyboard is closed
                    chatsBinding.cardView.setTranslationY(0);
                    chatsBinding.sendBtn.setTranslationY(0);
                    chatsBinding.chatRecyclerView.setTranslationY(0);
                }
            }
        });
        chatsBinding.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ChatsActivity.this, ProfileActivity.class);
                intent.putExtra("name",receiverName);
                intent.putExtra("number",contactNumber);
                intent.putExtra("imageUri",profile_image_uri.toString());
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
                if(chatsBinding.messageBox.getText().toString().length()==0){
                    chatsBinding.camera.setVisibility(View.VISIBLE);
                }else {
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
                                    // Toast.makeText(ChatsActivity.this, "message sent successfully", Toast.LENGTH_SHORT).show();
                                }));
                chatsBinding.chatRecyclerView.scrollToPosition(messagesAdapter.getItemCount()-1);
            }
        });

        // Adjust layout when keyboard appears
//        chatsBinding.main.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
//            Rect r = new Rect();
//            chatsBinding.main.getWindowVisibleDisplayFrame(r);
//            int screenHeight = chatsBinding.main.getRootView().getHeight();
//            int keypadHeight = screenHeight - r.bottom;
//
//            if (keypadHeight > screenHeight * 0.15) {
//                chatsBinding.chatRecyclerView.scrollToPosition(messagesAdapter.getItemCount() - 1);
//            }
//        });
    }
}
