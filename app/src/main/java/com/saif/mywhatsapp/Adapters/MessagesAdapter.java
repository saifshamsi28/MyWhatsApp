package com.saif.mywhatsapp.Adapters;
////
////import android.content.Context;
////import android.content.res.Configuration;
////import android.view.LayoutInflater;
////import android.view.View;
////import android.view.ViewGroup;
////import android.view.ViewTreeObserver;
////import android.widget.LinearLayout;
////import androidx.annotation.NonNull;
////import androidx.core.content.ContextCompat;
////import androidx.recyclerview.widget.RecyclerView;
////import com.github.pgreze.reactions.ReactionPopup;
////import com.github.pgreze.reactions.ReactionsConfig;
////import com.github.pgreze.reactions.ReactionsConfigBuilder;
////import com.google.firebase.auth.FirebaseAuth;
////import com.saif.mywhatsapp.Models.Message;
////import com.saif.mywhatsapp.R;
////import com.saif.mywhatsapp.databinding.ItemReceiveBinding;
////import com.saif.mywhatsapp.databinding.ItemSendBinding;
////import com.saif.mywhatsapp.databinding.ReceiveAudioItemBinding;
////import com.saif.mywhatsapp.databinding.SendAudioItemBinding;
////
////import java.text.SimpleDateFormat;
////import java.util.ArrayList;
////import java.util.Date;
////import java.util.Locale;
////
////import me.jagar.chatvoiceplayerlibrary.VoicePlayerView;
////
////public class MessagesAdapter extends RecyclerView.Adapter{
////    private Context context;
////    private ArrayList<Message> messages;
////    private final int INT_ITEM_SENT=1;
////    private final int INT_ITEM_RECEIVE=2;
////    private final int INT_AUDIO_MESSAGE_SENT=3;
////    private final int INT_AUDIO_MESSAGE_RECEIVE=4;
////
////    public MessagesAdapter(Context context,ArrayList<Message> messages){
////        this.context=context;
////        this.messages=messages;
////    }
////    @NonNull
////    @Override
////    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
////        View view = null;
////        switch (viewType){
////            case INT_ITEM_SENT:
////                view= LayoutInflater.from(context).inflate(R.layout.item_send,parent,false);
////                return new SentViewHolder(view);
////            case INT_ITEM_RECEIVE:
////                view= LayoutInflater.from(context).inflate(R.layout.item_receive,parent,false);
////                return new ReceiverViewHolder(view);
////            case INT_AUDIO_MESSAGE_SENT:
////                view=LayoutInflater.from(context).inflate(R.layout.send_audio_item,parent,false);
////                return new SentAudioViewHolder(view);
////            case INT_AUDIO_MESSAGE_RECEIVE:
////                view=LayoutInflater.from(context).inflate(R.layout.receive_audio_item,parent,false);
////                return new ReceiverAudioViewHolder(view);
////            default:
////                return new SentViewHolder(view);
////        }
////    }
////
////    @Override
////    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
////        int []reactions=new int[]{
////                R.drawable.ic_fb_like,
////                R.drawable.ic_fb_love,
////                R.drawable.ic_fb_laugh,
////                R.drawable.ic_fb_wow,
////                R.drawable.ic_fb_sad,
////                R.drawable.ic_fb_angry
////        };
////        ReactionsConfig config = new ReactionsConfigBuilder(context)
////                .withReactions(reactions)
////                .build();
////
////        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {
////            return true; // true is closing popup, false is requesting a new selection
////        });
////        Message message=messages.get(position);
////        if(holder.getClass()==SentViewHolder.class){
////            SentViewHolder viewHolder = (SentViewHolder) holder;
//////            if(message.isAudioMessage()){
//////                viewHolder.sendAudioItemBinding.cardviewVoiceMe.setVisibility(View.VISIBLE);
//////                viewHolder.sendAudioItemBinding.senderProfile.setImageResource(R.drawable.avatar);
//////            }else {
////                viewHolder.sendBinding.message.setText(message.getMessage());
//////            }
////
////            Date date = new Date(message.getTimeStamp());
////            // to format time (12-hour format with AM/PM)
////            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
////            //to Format the date
////            String formattedTime = timeFormat.format(date);
////            // Setting the formatted timestamp(in 12hours AM/PM) to the timestamp-TextView
////            viewHolder.sendBinding.timestamp.setText(formattedTime);
////
////            setThemeForHomeScreen(holder, 1); // Updated call
////            // Set the message status
////            switch (message.getStatus()) {
////                case Message.STATUS_SENT:
////                    viewHolder.sendBinding.statusIcon.setImageResource(R.drawable.sent_tick);
////                    break;
////                case Message.STATUS_DELIVERED:
////                    viewHolder.sendBinding.statusIcon.setImageResource(R.drawable.delivered_tick);
////                    break;
////                case Message.STATUS_READ:
////                    viewHolder.sendBinding.statusIcon.setImageResource(R.drawable.seen_tick);
////                    break;
////            }
////            // Adjust layout
////            viewHolder.sendBinding.layoutChatSendContainer.post(() -> {
////                int messageWidth = viewHolder.sendBinding.message.getMeasuredWidth() + viewHolder.sendBinding.message.getPaddingLeft() + viewHolder.sendBinding.message.getPaddingRight();
////                int timestampWidth = viewHolder.sendBinding.timestamp.getMeasuredWidth() + viewHolder.sendBinding.timestamp.getPaddingLeft() + viewHolder.sendBinding.timestamp.getPaddingRight();
////                int combinedWidth = messageWidth + timestampWidth;
////
////                int maxWidth = viewHolder.sendBinding.message.getMaxWidth();
////                if (combinedWidth <= maxWidth) {
////                    viewHolder.sendBinding.layoutChatSendContainer.setOrientation(LinearLayout.HORIZONTAL);
////                } else {
////                    viewHolder.sendBinding.layoutChatSendContainer.setOrientation(LinearLayout.VERTICAL);
////                }
////            });
////
////            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
////            String messageDay = sdf.format(date);
////
////            if (position == 0) {
////                viewHolder.sendBinding.messageDate.setVisibility(View.VISIBLE);
////                viewHolder.sendBinding.messageDate.setText(messageDay);
////            } else {
////                Message previousMessage = messages.get(position - 1);
////                String previousMessageDay = sdf.format(new Date(previousMessage.getTimeStamp()));
////                if (!messageDay.equals(previousMessageDay)) {
////                    viewHolder.sendBinding.messageDate.setVisibility(View.VISIBLE);
////                    viewHolder.sendBinding.messageDate.setText(messageDay);
////                } else {
////                    viewHolder.sendBinding.messageDate.setVisibility(View.GONE);
////                }
////            }
////        } else {
////            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
////            viewHolder.receiveBinding.message.setText(message.getMessage());
////            setThemeForHomeScreen(holder,2);
////
////            Date date = new Date(message.getTimeStamp());
////            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
////            String formattedDate = dateFormat.format(date);
////            viewHolder.receiveBinding.timestamp.setText(formattedDate);
////
////            viewHolder.receiveBinding.layoutChatReceiveContainer.getViewTreeObserver().addOnGlobalLayoutListener(
////                    new ViewTreeObserver.OnGlobalLayoutListener() {
////                        @Override
////                        public void onGlobalLayout() {
////                            int messageWidth = viewHolder.receiveBinding.message.getWidth() + viewHolder.receiveBinding.message.getPaddingLeft() + viewHolder.receiveBinding.message.getPaddingRight();
////                            int timestampWidth = viewHolder.receiveBinding.timestamp.getWidth() + viewHolder.receiveBinding.timestamp.getPaddingLeft() + viewHolder.receiveBinding.timestamp.getPaddingRight();
////                            int combinedWidth = messageWidth + timestampWidth;
////
////                            int maxWidth = viewHolder.receiveBinding.message.getMaxWidth();
////
////                            if (combinedWidth <= maxWidth) {
////                                viewHolder.receiveBinding.layoutChatReceiveContainer.setOrientation(LinearLayout.HORIZONTAL);
////                            } else {
////                                viewHolder.receiveBinding.layoutChatReceiveContainer.setOrientation(LinearLayout.VERTICAL);
////                            }
////
////                            viewHolder.receiveBinding.layoutChatReceiveContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
////                        }
////                    });
////
////            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
////            String messageDay = sdf.format(date);
////
////            if (position == 0) {
////                viewHolder.receiveBinding.messageDate.setVisibility(View.VISIBLE);
////                viewHolder.receiveBinding.messageDate.setText(messageDay);
////            } else {
////                Message previousMessage = messages.get(position - 1);
////                String previousMessageDay = sdf.format(new Date(previousMessage.getTimeStamp()));
////                if (!messageDay.equals(previousMessageDay)) {
////                    viewHolder.receiveBinding.messageDate.setVisibility(View.VISIBLE);
////                    viewHolder.receiveBinding.messageDate.setText(messageDay);
////                } else {
////                    viewHolder.receiveBinding.messageDate.setVisibility(View.GONE);
////                }
////            }
////        }
////
////    }
////
////    @Override
////    public int getItemViewType(int position) {
////        Message message= messages.get(position);
////        if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(message.getSenderId())){
////            if(message.isAudioMessage()){
////                return INT_AUDIO_MESSAGE_SENT;
////            }
////            return INT_ITEM_SENT;
////        }
////        else {
////            if(message.isAudioMessage()){
////                return INT_AUDIO_MESSAGE_RECEIVE;
////            }
////            return INT_ITEM_RECEIVE;
////        }
////    }
////
////    @Override
////    public int getItemCount() {
////        return messages.size();
////    }
////
////    private void setThemeForHomeScreen(RecyclerView.ViewHolder viewHolder, int type) {
////        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
////        int color;
////        int receiveMessage;
////        int receiveMessageBackground;
////        int sendMessageBackground;
////        int color2;
////
////        switch (nightModeFlags) {
////            case Configuration.UI_MODE_NIGHT_YES:
////                color = ContextCompat.getColor(context, R.color.primaryTextColor); // White for dark mode
////                color2 = ContextCompat.getColor(context, R.color.secondaryTextColor); // White for dark mode
////                receiveMessage = ContextCompat.getColor(context, R.color.white); // White text for dark mode
////                receiveMessageBackground = ContextCompat.getColor(context, R.color.receive_message); // Black background for dark mode
////                sendMessageBackground = ContextCompat.getColor(context, R.color.send_message);
////                break;
////            case Configuration.UI_MODE_NIGHT_NO:
////            case Configuration.UI_MODE_NIGHT_UNDEFINED:
////            default:
////                color = ContextCompat.getColor(context, R.color.white); // Black for light mode
////                color2 = ContextCompat.getColor(context, R.color.secondaryTextColor); // Secondary text color
////                receiveMessage = ContextCompat.getColor(context, R.color.black); // Black text for light mode
////                receiveMessageBackground = ContextCompat.getColor(context, R.color.white); // White background for light mode
////                sendMessageBackground = ContextCompat.getColor(context, R.color.GreenishBlue);
////                break;
////        }
////
////        if (viewHolder instanceof SentViewHolder) {
////            SentViewHolder sentViewHolder = (SentViewHolder) viewHolder;
////            sentViewHolder.sendBinding.message.setTextColor(color);
////            sentViewHolder.sendBinding.messageDate.setTextColor(color2);
//////            sentViewHolder.sendBinding.statusIcon.setColorFilter(color);
////            sentViewHolder.sendBinding.cardviewMessageMe.setCardBackgroundColor(sendMessageBackground);
////        } else if (viewHolder instanceof ReceiverViewHolder) {
////            ReceiverViewHolder receiverViewHolder = (ReceiverViewHolder) viewHolder;
////            receiverViewHolder.receiveBinding.message.setTextColor(receiveMessage);
////            receiverViewHolder.receiveBinding.messageDate.setTextColor(color2);
////            receiverViewHolder.receiveBinding.timestamp.setTextColor(color2);
////            receiverViewHolder.receiveBinding.cardviewMessageOther.setCardBackgroundColor(receiveMessageBackground); // Updated to set background color
////        } else if (viewHolder instanceof SentAudioViewHolder) {
////            SentAudioViewHolder sentAudioViewHolder = (SentAudioViewHolder) viewHolder;
////            sentAudioViewHolder.sendAudioItemBinding.timestamp.setTextColor(color2);
////            sentAudioViewHolder.sendAudioItemBinding.seekBar.getProgressDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
////            sentAudioViewHolder.sendAudioItemBinding.seekBar.getThumb().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
////            sentAudioViewHolder.sendAudioItemBinding.playerLayout.setBackgroundColor(sendMessageBackground);
////        } else if (viewHolder instanceof ReceiverAudioViewHolder) {
////            ReceiverAudioViewHolder receiverAudioViewHolder = (ReceiverAudioViewHolder) viewHolder;
////            receiverAudioViewHolder.receiveAudioItemBinding.timestamp.setTextColor(color2);
////            receiverAudioViewHolder.receiveAudioItemBinding.seekBar.getProgressDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
////            receiverAudioViewHolder.receiveAudioItemBinding.seekBar.getThumb().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
////            receiverAudioViewHolder.receiveAudioItemBinding.playerLayout.setBackgroundColor(receiveMessageBackground);
////        }
////    }
////    public class SentViewHolder extends RecyclerView.ViewHolder{
////        ItemSendBinding sendBinding;
////
////        public SentViewHolder(@NonNull View itemView) {
////            super(itemView);
////            sendBinding=ItemSendBinding.bind(itemView);
////        }
////    }
////    public class ReceiverViewHolder extends RecyclerView.ViewHolder{
////
////        ItemReceiveBinding receiveBinding;
////        public ReceiverViewHolder(@NonNull View itemView) {
////            super(itemView);
////            receiveBinding=ItemReceiveBinding.bind(itemView);
////        }
////    }
////
////    public class SentAudioViewHolder extends RecyclerView.ViewHolder{
////        SendAudioItemBinding sendAudioItemBinding;
////
////        public SentAudioViewHolder(@NonNull View itemView) {
////            super(itemView);
////            sendAudioItemBinding=SendAudioItemBinding.bind(itemView);
////        }
////    }
////    public class ReceiverAudioViewHolder extends RecyclerView.ViewHolder{
////
////        ReceiveAudioItemBinding receiveAudioItemBinding;
////        public ReceiverAudioViewHolder(@NonNull View itemView) {
////            super(itemView);
////            receiveAudioItemBinding=ReceiveAudioItemBinding.bind(itemView);
////        }
////    }
////
////}
//
//import android.content.Context;
//import android.content.res.Configuration;
//import android.media.MediaPlayer;
//import android.os.Handler;
//import android.os.Looper;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.ViewTreeObserver;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.core.content.ContextCompat;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.bumptech.glide.Glide;
//import com.google.firebase.auth.FirebaseAuth;
//import com.saif.mywhatsapp.Database.AppDatabase;
//import com.saif.mywhatsapp.Database.DatabaseClient;
//import com.saif.mywhatsapp.Models.Message;
//import com.saif.mywhatsapp.Models.User;
//import com.saif.mywhatsapp.R;
//import com.saif.mywhatsapp.databinding.ItemReceiveBinding;
//import com.saif.mywhatsapp.databinding.ItemSendBinding;
//import com.saif.mywhatsapp.databinding.ReceiveAudioItemBinding;
//import com.saif.mywhatsapp.databinding.SendAudioItemBinding;
//
//import java.io.IOException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.Locale;
//import java.util.concurrent.Executor;
//import java.util.concurrent.Executors;
//
//public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//
//    private static final int INT_ITEM_SENT = 1;
//    private static final int INT_ITEM_RECEIVE = 2;
//    private static final int INT_AUDIO_MESSAGE_SENT = 3;
//    private static final int INT_AUDIO_MESSAGE_RECEIVE = 4;
//
//    private Context context;
//    private ArrayList<Message> messages;
//    private MediaPlayer mediaPlayer;
//    private AppDatabase appDatabase;
//    private final Executor executor= Executors.newSingleThreadExecutor();
//    private final Handler handler=new Handler(Looper.getMainLooper());
//
//    public MessagesAdapter(Context context, ArrayList<Message> messages) {
//        this.context = context;
//        this.messages = messages;
//        this.mediaPlayer = new MediaPlayer();
//        appDatabase= DatabaseClient.getInstance(context).getAppDatabase();
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//        Message message = messages.get(position);
//        String senderId = FirebaseAuth.getInstance().getUid();
//
//        // Log for debugging
//        Log.d("getItemViewType", "Message senderId: " + message.getSenderId() + ", Current user senderId: " + senderId);
//
//        if (message != null && message.getSenderId() != null) {
//            if (message.getSenderId().equals(senderId)) {
//                if (message.isAudioMessage()) {
//                    return INT_AUDIO_MESSAGE_SENT;
//                } else {
//                    return INT_ITEM_SENT;
//                }
//            } else {
//                if (message.isAudioMessage()) {
//                    return INT_AUDIO_MESSAGE_RECEIVE;
//                } else {
//                    return INT_ITEM_RECEIVE;
//                }
//            }
//        } else {
//            Toast.makeText(context, "Default layout receive is calling", Toast.LENGTH_SHORT).show();
//            // Default to receive if the message or senderId is null
//            return INT_ITEM_RECEIVE;
//        }
//    }
//
//
//
//    @NonNull
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        if (viewType == INT_ITEM_SENT) {
//            View view = LayoutInflater.from(context).inflate(R.layout.item_send, parent, false);
//            return new SentViewHolder(view);
//        } else if (viewType == INT_ITEM_RECEIVE) {
//            View view = LayoutInflater.from(context).inflate(R.layout.item_receive, parent, false);
//            return new ReceiverViewHolder(view);
//        } else if (viewType == INT_AUDIO_MESSAGE_SENT) {
//            View view = LayoutInflater.from(context).inflate(R.layout.send_audio_item, parent, false);
//            return new SentAudioViewHolder(view);
//        } else {
//            View view = LayoutInflater.from(context).inflate(R.layout.receive_audio_item, parent, false);
//            return new ReceiverAudioViewHolder(view);
//        }
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        Message message = messages.get(position);
//
//        if (holder instanceof SentViewHolder) {
//            SentViewHolder viewHolder = (SentViewHolder) holder;
//            viewHolder.sendBinding.message.setText(message.getMessage());
//
//            setSenderChatViews(viewHolder.sendBinding.message,
//                        message,
//                        viewHolder.sendBinding.messageDate,
//                        viewHolder.sendBinding.layoutChatSendContainer,
//                        viewHolder.sendBinding.timestamp,
//                        viewHolder.sendBinding.statusIcon
//                        ,position);
//
//            viewHolder.sendBinding.layoutChatSendContainer.post(() -> {
//                int messageWidth =viewHolder.sendBinding.message.getMeasuredWidth() + viewHolder.sendBinding.message.getPaddingLeft() + viewHolder.sendBinding.message.getPaddingRight();
//                int timestampWidth = viewHolder.sendBinding.timestamp.getMeasuredWidth() + viewHolder.sendBinding.timestamp.getPaddingLeft() + viewHolder.sendBinding.timestamp.getPaddingRight();
//                int combinedWidth = messageWidth + timestampWidth;
//
//                int maxWidth = viewHolder.sendBinding.message.getMaxWidth();
//                if (combinedWidth <= maxWidth) {
//                    viewHolder.sendBinding.layoutChatSendContainer.setOrientation(LinearLayout.HORIZONTAL);
//                } else {
//                    viewHolder.sendBinding.layoutChatSendContainer.setOrientation(LinearLayout.VERTICAL);
//                }
//            });
////            setThemeForHomeScreen(holder, 1);
//
//        } else if (holder instanceof ReceiverViewHolder) {
//            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
//            viewHolder.receiveBinding.message.setText(message.getMessage());
//            setReceiverChatViews(viewHolder.receiveBinding.message,
//                        message,
//                        viewHolder.receiveBinding.messageDate,
//                        viewHolder.receiveBinding.layoutChatReceiveContainer,
//                        viewHolder.receiveBinding.timestamp,
//                        position);
//
//            setThemeForHomeScreen(holder, 2);
//
//        } else if (holder instanceof SentAudioViewHolder) {
//            SentAudioViewHolder viewHolder = (SentAudioViewHolder) holder;
//            setupAudioPlayer(viewHolder.sendAudioItemBinding.playPause, message.getMessage());
//            executor.execute(() -> {
//                User user=appDatabase.userDao().getUserByUid(message.getSenderId());
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Glide.with(context).load(user.getProfileImage())
//                                .placeholder(R.drawable.avatar)
//                                .into(viewHolder.sendAudioItemBinding.senderProfile);
//                    }
//                });
//            });
//
//            setThemeForHomeScreen(holder, 1);
//
//        } else if (holder instanceof ReceiverAudioViewHolder) {
//            ReceiverAudioViewHolder viewHolder = (ReceiverAudioViewHolder) holder;
//            setupAudioPlayer(viewHolder.receiveAudioItemBinding.playPause, message.getMessage());
//
//            executor.execute(() -> {
//                User user=appDatabase.userDao().getUserByUid(message.getSenderId());
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Glide.with(context).load(user.getProfileImage())
//                                .placeholder(R.drawable.avatar)
//                                .into(viewHolder.receiveAudioItemBinding.senderProfile);
//                    }
//                });
//            });
//            setThemeForHomeScreen(holder, 2);
//        }
//    }
//
//    private void setSenderChatViews(TextView messageTextView, Message message, TextView messageDate, LinearLayout layoutChatSendContainer, TextView timestamp, ImageView statusIcon,int position) {
//        Date date = new Date(message.getTimeStamp());
//            // to format time (12-hour format with AM/PM)
//            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
//            //to Format the date
//            String formattedTime = timeFormat.format(date);
//            // Setting the formatted timestamp(in 12hours AM/PM) to the timestamp-TextView
//            timestamp.setText(formattedTime);
//
//            // Set the message status
//            switch (message.getStatus()) {
//                case Message.STATUS_SENT:
//                    statusIcon.setImageResource(R.drawable.sent_tick);
//                    break;
//                case Message.STATUS_DELIVERED:
//                    statusIcon.setImageResource(R.drawable.delivered_tick);
//                    break;
//                case Message.STATUS_READ:
//                    statusIcon.setImageResource(R.drawable.seen_tick);
//                    break;
//            }
//            // Adjust layout
//
//            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
//            String messageDay = sdf.format(date);
//
//            if (position == 0) {
//                messageDate.setVisibility(View.VISIBLE);
//                messageDate.setText(messageDay);
//            } else {
//                Message previousMessage = messages.get(position - 1);
//                String previousMessageDay = sdf.format(new Date(previousMessage.getTimeStamp()));
//                if (!messageDay.equals(previousMessageDay)) {
//                    messageDate.setVisibility(View.VISIBLE);
//                    messageDate.setText(messageDay);
//                } else {
//                    messageDate.setVisibility(View.GONE);
//                }
//            }
//    }
//
//    private void setReceiverChatViews(TextView messageTextView, Message message, TextView messageDate, LinearLayout layoutChatReceiveContainer, TextView timestamp, int position){
//        Date date = new Date(message.getTimeStamp());
//            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
//            String formattedDate = dateFormat.format(date);
//            timestamp.setText(formattedDate);
//
//            if(!message.isAudioMessage()) {
//                layoutChatReceiveContainer.getViewTreeObserver().addOnGlobalLayoutListener(
//                        new ViewTreeObserver.OnGlobalLayoutListener() {
//                            @Override
//                            public void onGlobalLayout() {
//                                int messageWidth = messageTextView.getWidth() + messageTextView.getPaddingLeft() + messageTextView.getPaddingRight();
//                                int timestampWidth = timestamp.getWidth() + timestamp.getPaddingLeft() + timestamp.getPaddingRight();
//                                int combinedWidth = messageWidth + timestampWidth;
//
//                                int maxWidth = messageTextView.getMaxWidth();
//
//                                if (combinedWidth <= maxWidth) {
//                                    layoutChatReceiveContainer.setOrientation(LinearLayout.HORIZONTAL);
//                                } else {
//                                    layoutChatReceiveContainer.setOrientation(LinearLayout.VERTICAL);
//                                }
//
//                                layoutChatReceiveContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                            }
//                        });
//            }else {
//
//            }
//
//            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
//            String messageDay = sdf.format(date);
//
//            if (position == 0) {
//                messageDate.setVisibility(View.VISIBLE);
//                messageDate.setText(messageDay);
//            } else {
//                Message previousMessage = messages.get(position - 1);
//                String previousMessageDay = sdf.format(new Date(previousMessage.getTimeStamp()));
//                if (!messageDay.equals(previousMessageDay)) {
//                    messageDate.setVisibility(View.VISIBLE);
//                    messageDate.setText(messageDay);
//                } else {
//                    messageDate.setVisibility(View.GONE);
//                }
//            }
//        }
//    private void setupAudioPlayer(View playButton, String audioUrl) {
//        try{
//            mediaPlayer.setDataSource(audioUrl);
//            mediaPlayer.prepare();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        playButton.setOnClickListener(v -> {
//            if (mediaPlayer.isPlaying()) {
//                mediaPlayer.pause();
////                mediaPlayer.reset();
//            } else {
//                mediaPlayer.start();
//            }
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return messages.size();
//    }
//
//
//    public static class SentViewHolder extends RecyclerView.ViewHolder {
//        ItemSendBinding sendBinding;
//
//        public SentViewHolder(@NonNull View itemView) {
//            super(itemView);
//            sendBinding = ItemSendBinding.bind(itemView);
//        }
//    }
//
//    public static class ReceiverViewHolder extends RecyclerView.ViewHolder {
//        ItemReceiveBinding receiveBinding;
//
//        public ReceiverViewHolder(@NonNull View itemView) {
//            super(itemView);
//            receiveBinding = ItemReceiveBinding.bind(itemView);
//        }
//    }
//
//    public static class SentAudioViewHolder extends RecyclerView.ViewHolder {
//        SendAudioItemBinding sendAudioItemBinding;
//
//        public SentAudioViewHolder(@NonNull View itemView) {
//            super(itemView);
//            sendAudioItemBinding = SendAudioItemBinding.bind(itemView);
//        }
//    }
//
//    public static class ReceiverAudioViewHolder extends RecyclerView.ViewHolder {
//        ReceiveAudioItemBinding receiveAudioItemBinding;
//
//        public ReceiverAudioViewHolder(@NonNull View itemView) {
//            super(itemView);
//            receiveAudioItemBinding = ReceiveAudioItemBinding.bind(itemView);
//        }
//    }
//}
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.saif.mywhatsapp.Database.AppDatabase;
import com.saif.mywhatsapp.Database.DatabaseClient;
import com.saif.mywhatsapp.Models.Message;
import com.saif.mywhatsapp.Models.User;
import com.saif.mywhatsapp.R;
import com.saif.mywhatsapp.databinding.ItemReceiveBinding;
import com.saif.mywhatsapp.databinding.ItemSendBinding;
import com.saif.mywhatsapp.databinding.ReceiveAudioItemBinding;
import com.saif.mywhatsapp.databinding.SendAudioItemBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import linc.com.amplituda.Amplituda;
import rm.com.audiowave.AudioWaveView;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int INT_ITEM_SENT = 1;
    private static final int INT_ITEM_RECEIVE = 2;
    private static final int INT_AUDIO_MESSAGE_SENT = 3;
    private static final int INT_AUDIO_MESSAGE_RECEIVE = 4;

    private final Context context;
    private ArrayList<Message> messages;
    private MediaPlayer mediaPlayer;
    private MediaPlayer currentMediaPlayer;
    private final AppDatabase appDatabase;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateSeekBarRunnable;
    private ImageView currentPlayButton;
    private TextView currentPlayedTime;
    private SeekBar currentSeekBar;
    private Map<Integer, MediaPlayer> mediaPlayerMap = new HashMap<>();
    private Map<String, byte[]> audioWaveformCache = new HashMap<>();
    private Amplituda amplituda;

    public MessagesAdapter(Context context, ArrayList<Message> messages) {
        this.context = context;
        this.messages = messages;
        this.mediaPlayer = new MediaPlayer();
        appDatabase = DatabaseClient.getInstance(context).getAppDatabase();
        amplituda = new Amplituda(context);
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        String senderId = FirebaseAuth.getInstance().getUid();

        // Log for debugging
//        Log.d("getItemViewType", "Message senderId: " + message.getSenderId() + ", Current user senderId: " + senderId);

        if (message != null && message.getSenderId() != null) {
            if (message.getSenderId().equals(senderId)) {
                if (message.isAudioMessage()) {
                    return INT_AUDIO_MESSAGE_SENT;
                } else {
                    return INT_ITEM_SENT;
                }
            } else {
                if (message.isAudioMessage()) {
                    return INT_AUDIO_MESSAGE_RECEIVE;
                } else {
                    return INT_ITEM_RECEIVE;
                }
            }
        } else {
            Toast.makeText(context, "Default layout receive is calling", Toast.LENGTH_SHORT).show();
            // Default to receive if the message or senderId is null
            return INT_ITEM_RECEIVE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == INT_ITEM_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_send, parent, false);
            return new SentViewHolder(view);
        } else if (viewType == INT_ITEM_RECEIVE) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_receive, parent, false);
            return new ReceiverViewHolder(view);
        } else if (viewType == INT_AUDIO_MESSAGE_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.send_audio_item, parent, false);
            return new SentAudioViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.receive_audio_item, parent, false);
            return new ReceiverAudioViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        if (holder instanceof SentViewHolder) {
            SentViewHolder viewHolder = (SentViewHolder) holder;
            viewHolder.sendBinding.message.setText(message.getMessage());

            setSenderChatViews(message, viewHolder.sendBinding.messageDateCardview, viewHolder.sendBinding.messageDate,
                    viewHolder.sendBinding.timestamp,
                    viewHolder.sendBinding.statusIcon, position);

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    updateLayoutOrientation(viewHolder.sendBinding.layoutChatSendContainer,
                            viewHolder.sendBinding.message, viewHolder.sendBinding.timestamp);
                }
            });

            setThemeForHomeScreen(viewHolder);

        } else if (holder instanceof ReceiverViewHolder) {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            viewHolder.receiveBinding.message.setText(message.getMessage());

            setReceiverChatViews(viewHolder.receiveBinding.message, message, viewHolder.receiveBinding.messageDateCardview,viewHolder.receiveBinding.messageDate,
                    viewHolder.receiveBinding.layoutChatReceiveContainer, viewHolder.receiveBinding.timestamp, position);
            setThemeForHomeScreen(viewHolder);

        } else if (holder instanceof SentAudioViewHolder) {
            SentAudioViewHolder viewHolder = (SentAudioViewHolder) holder;
            Date date = new Date(message.getTimeStamp());
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            String formattedTime = timeFormat.format(date);
            viewHolder.sendAudioItemBinding.timestamp.setText(formattedTime);
            setupAudioPlayer(viewHolder.sendAudioItemBinding, message,position);

            executor.execute(() -> {
                User user = appDatabase.userDao().getUserByUid(message.getSenderId());
                handler.post(() -> Glide.with(context).load(user.getProfileImage())
                        .placeholder(R.drawable.avatar)
                        .into(viewHolder.sendAudioItemBinding.senderProfile));
            });
            setThemeForHomeScreen(viewHolder);

        } else if (holder instanceof ReceiverAudioViewHolder) {
            ReceiverAudioViewHolder viewHolder = (ReceiverAudioViewHolder) holder;
            Date date = new Date(message.getTimeStamp());
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            String formattedTime = timeFormat.format(date);
            viewHolder.receiveAudioItemBinding.timestamp.setText(formattedTime);
            setupAudioPlayer(viewHolder.receiveAudioItemBinding, message,position);
            setThemeForHomeScreen(viewHolder);

            executor.execute(() -> {
                User user = appDatabase.userDao().getUserByUid(message.getSenderId());
                handler.post(() -> Glide.with(context).load(user.getProfileImage())
                        .placeholder(R.drawable.avatar)
                        .into(viewHolder.receiveAudioItemBinding.senderProfile));
            });
        }
    }

    private void updateLayoutOrientation(LinearLayout container, TextView message, TextView timestamp) {
        int messageWidth = message.getMeasuredWidth() + message.getPaddingLeft() + message.getPaddingRight();
        int timestampWidth = timestamp.getMeasuredWidth() + timestamp.getPaddingLeft() + timestamp.getPaddingRight();
        int combinedWidth = messageWidth + timestampWidth;
        int maxWidth = message.getMaxWidth();

        container.setOrientation(combinedWidth <= maxWidth ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
    }

    private void setSenderChatViews(Message message, CardView messageDateCardview, TextView messageDate,
                                    TextView timestamp,
                                    ImageView statusIcon, int position) {
        Date date = new Date(message.getTimeStamp());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String formattedTime = timeFormat.format(date);
        if(formattedTime.startsWith("0")){
            formattedTime=formattedTime.substring(1);
        }
        timestamp.setText(formattedTime);

        switch (message.getStatus()) {
            case Message.STATUS_SENT:
                statusIcon.setImageResource(R.drawable.sent_tick);
                break;
            case Message.STATUS_DELIVERED:
                statusIcon.setImageResource(R.drawable.delivered_tick);
                break;
            case Message.STATUS_READ:
                statusIcon.setImageResource(R.drawable.seen_tick);
                break;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        String messageDay = sdf.format(date);

        if (position == 0) {
            messageDateCardview.setVisibility(View.VISIBLE);
            messageDate.setText(messageDay);
        } else {
            Message previousMessage = messages.get(position - 1);
            String previousMessageDay = sdf.format(new Date(previousMessage.getTimeStamp()));
            if (!messageDay.equals(previousMessageDay)) {
                messageDateCardview.setVisibility(View.VISIBLE);
                messageDate.setText(messageDay);
            } else {
                messageDateCardview.setVisibility(View.GONE);
            }
        }
    }

    private void setReceiverChatViews(TextView messageTextView, Message message, CardView messageDateCardview, TextView messageDate,
                                      LinearLayout layoutChatReceiveContainer, TextView timestamp,
                                      int position) {
        Date date = new Date(message.getTimeStamp());
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String formattedTime = dateFormat.format(date);
        if(formattedTime.startsWith("0")){
            formattedTime = formattedTime.substring(1);
        }
        timestamp.setText(formattedTime);

        layoutChatReceiveContainer.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int messageWidth = messageTextView.getWidth() + messageTextView.getPaddingLeft() + messageTextView.getPaddingRight();
                        int timestampWidth = timestamp.getWidth() + timestamp.getPaddingLeft() + timestamp.getPaddingRight();
                        int combinedWidth = messageWidth + timestampWidth;

                        int maxWidth = messageTextView.getMaxWidth();
                        if (combinedWidth <= maxWidth) {
                            layoutChatReceiveContainer.setOrientation(LinearLayout.HORIZONTAL);
                        } else {
                            layoutChatReceiveContainer.setOrientation(LinearLayout.VERTICAL);
                        }
                        layoutChatReceiveContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        String messageDay = sdf.format(date);

        if (position == 0) {
            messageDateCardview.setVisibility(View.VISIBLE);
            messageDate.setText(messageDay);
        } else {
            Message previousMessage = messages.get(position - 1);
            String previousMessageDay = sdf.format(new Date(previousMessage.getTimeStamp()));
            if (!messageDay.equals(previousMessageDay)) {
                messageDateCardview.setVisibility(View.VISIBLE);
                messageDate.setText(messageDay);
            } else {
                messageDateCardview.setVisibility(View.GONE);
            }
        }
    }

//
    private void setupAudioPlayer(SendAudioItemBinding binding, Message message, int position) {
        switch (message.getStatus()) {
            case Message.STATUS_SENT:
                binding.statusIcon.setImageResource(R.drawable.sent_tick);
                break;
            case Message.STATUS_DELIVERED:
                binding.statusIcon.setImageResource(R.drawable.delivered_tick);
                break;
            case Message.STATUS_READ:
                binding.statusIcon.setImageResource(R.drawable.seen_tick);
                break;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        String messageDay = sdf.format(new Date(message.getTimeStamp()));

        if (position == 0) {
            binding.messageDateCardview.setVisibility(View.VISIBLE);
            binding.voiceMessageDate.setText(messageDay);
        } else {
            Message previousMessage = messages.get(position - 1);
            String previousMessageDay = sdf.format(new Date(previousMessage.getTimeStamp()));
            if (!messageDay.equals(previousMessageDay)) {
                binding.messageDateCardview.setVisibility(View.VISIBLE);
                binding.voiceMessageDate.setText(messageDay);
            } else {
                binding.messageDateCardview.setVisibility(View.GONE);
            }
        }
        //setting voice items
        MediaPlayer mediaPlayer = getMediaPlayerForPosition(position, message.getMessage());
        binding.totalTime.setText(formatTime(mediaPlayer.getDuration()));

        binding.playPause.setOnClickListener(view -> {
            if (currentMediaPlayer != null && currentMediaPlayer != mediaPlayer && currentMediaPlayer.isPlaying()) {
                currentMediaPlayer.pause();
                currentPlayButton.setImageResource(R.drawable.play);
            }

            if (currentMediaPlayer == mediaPlayer) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    binding.playPause.setImageResource(R.drawable.play);
                } else {
                    mediaPlayer.start();
                    binding.playPause.setImageResource(R.drawable.pause);
                    updateSeekBar(binding.seekBar, binding.playedTime);
                }
            } else {
                stopCurrentPlayback(); // Ensure previous playback is stopped
                currentMediaPlayer = mediaPlayer;
                currentMediaPlayer.start();
                currentPlayButton = binding.playPause;
                currentSeekBar = binding.seekBar;
                currentPlayedTime = binding.playedTime;
                binding.playPause.setImageResource(R.drawable.pause);
                currentSeekBar.setMax(currentMediaPlayer.getDuration());
                updateSeekBar(currentSeekBar, currentPlayedTime);
            }
        });

        mediaPlayer.setOnCompletionListener(mp -> {
            binding.seekBar.setProgress(0);
            binding.playedTime.setText(formatTime(0));
            binding.playPause.setImageResource(R.drawable.play);
            handler.removeCallbacks(updateSeekBarRunnable);
            stopCurrentPlayback();
        });

        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (seekBar!=null && currentMediaPlayer!=null && fromUser) {
                    currentMediaPlayer.seekTo(progress);
                    seekBar.setProgress(progress);
                    currentPlayedTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

//    private void setupAudioPlayer(SendAudioItemBinding binding, Message message, int position) {
//        MediaPlayer mediaPlayer = getMediaPlayerForPosition(position, message.getMessage());
//        binding.totalTime.setText(formatTime(mediaPlayer.getDuration()));
//
//        binding.playPause.setOnClickListener(view -> {
//            if (currentMediaPlayer != null && currentMediaPlayer != mediaPlayer && currentMediaPlayer.isPlaying()) {
//                currentMediaPlayer.pause();
//                currentPlayButton.setImageResource(R.drawable.play);
//            }
//
//            if (currentMediaPlayer == mediaPlayer) {
//                if (mediaPlayer.isPlaying()) {
//                    mediaPlayer.pause();
//                    binding.playPause.setImageResource(R.drawable.play);
//                } else {
//                    mediaPlayer.start();
//                    binding.playPause.setImageResource(R.drawable.pause);
//                    updateAudioWaveView(binding.audioWave, mediaPlayer);
//                }
//            } else {
//                stopCurrentPlayback(); // Ensure previous playback is stopped
//                currentMediaPlayer = mediaPlayer;
//                currentMediaPlayer.start();
//                currentPlayButton = binding.playPause;
//                currentPlayedTime = binding.playedTime;
//                binding.playPause.setImageResource(R.drawable.pause);
//                updateAudioWaveView(binding.audioWave, currentMediaPlayer);
//            }
//        });
//
//        mediaPlayer.setOnCompletionListener(mp -> {
//            binding.audioWave.setProgress(0);
//            binding.playedTime.setText(formatTime(0));
//            binding.playPause.setImageResource(R.drawable.play);
//            handler.removeCallbacks(updateSeekBarRunnable);
//            stopCurrentPlayback();
//        });
//    }

    private void updateAudioWaveView(AudioWaveView audioWaveView, MediaPlayer mediaPlayer) {
        handler.removeCallbacks(updateSeekBarRunnable);  // Clear any previous callbacks
        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentMediaPlayer != null) {
                    int progress = (int) ((float) currentMediaPlayer.getCurrentPosition() / currentMediaPlayer.getDuration() * 100);
                    audioWaveView.setProgress(progress);
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(updateSeekBarRunnable);
    }

    private void stopCurrentPlayback() {
        if (currentMediaPlayer != null && currentMediaPlayer.isPlaying()) {
            currentMediaPlayer.pause();
            currentPlayButton.setImageResource(R.drawable.play);
            handler.removeCallbacks(updateSeekBarRunnable);  // Stop updating the audio wave view
        }
    }
    private MediaPlayer getMediaPlayerForPosition(int position, String audioPath) {
        if (mediaPlayerMap.containsKey(position)) {
            return mediaPlayerMap.get(position);
        }
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioPath);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayerMap.put(position, mediaPlayer);
        return mediaPlayer;
    }

    private void updateSeekBar(SeekBar seekBar, TextView playedTime) {
        handler.removeCallbacks(updateSeekBarRunnable);  // Clear any previous callbacks
        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentMediaPlayer != null) {
                    seekBar.setProgress(currentMediaPlayer.getCurrentPosition());
                    playedTime.setText(formatTime(currentMediaPlayer.getCurrentPosition()));
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(updateSeekBarRunnable);
    }

    private String formatTime(int milliseconds) {
        int seconds = milliseconds / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private void setupAudioPlayer(ReceiveAudioItemBinding binding, Message message, int position) {

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        String messageDay = sdf.format(new Date(message.getTimeStamp()));

        if (position == 0) {

            binding.messageDateCardview.setVisibility(View.VISIBLE);
            binding.messageDate.setText(messageDay);
        } else {
            Message previousMessage = messages.get(position - 1);
            String previousMessageDay = sdf.format(new Date(previousMessage.getTimeStamp()));
            if (!messageDay.equals(previousMessageDay)) {
                binding.messageDateCardview.setVisibility(View.VISIBLE);
                binding.messageDate.setText(messageDay);
            } else {
                binding.messageDateCardview.setVisibility(View.GONE);
            }
        }

        MediaPlayer mediaPlayer = getMediaPlayerForPosition(position, message.getMessage());
        binding.totalTime.setText(formatTime(mediaPlayer.getDuration()));

        binding.playPause.setOnClickListener(view -> {
            if (currentMediaPlayer != null && currentMediaPlayer != mediaPlayer && currentMediaPlayer.isPlaying()) {
                currentMediaPlayer.pause();
                currentPlayButton.setImageResource(R.drawable.play);
            }

            if (currentMediaPlayer == mediaPlayer) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    binding.playPause.setImageResource(R.drawable.play);
                } else {
                    mediaPlayer.start();
                    binding.playPause.setImageResource(R.drawable.pause);
                    updateSeekBar(binding.seekBar, binding.playedTime);
                }
            } else {
                stopCurrentPlayback(); // Ensure previous playback is stopped
                currentMediaPlayer = mediaPlayer;
                currentMediaPlayer.start();
                currentPlayButton = binding.playPause;
                currentSeekBar = binding.seekBar;
                currentPlayedTime = binding.playedTime;
                binding.playPause.setImageResource(R.drawable.pause);
                currentSeekBar.setMax(currentMediaPlayer.getDuration());
                updateSeekBar(currentSeekBar, currentPlayedTime);
            }
        });

        mediaPlayer.setOnCompletionListener(mp -> {
            binding.seekBar.setProgress(0);
            binding.playedTime.setText(formatTime(0));
            binding.playPause.setImageResource(R.drawable.play);
            handler.removeCallbacks(updateSeekBarRunnable);
            stopCurrentPlayback();
        });

        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    currentMediaPlayer.seekTo(progress);
                    currentPlayedTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
    private byte[] extractAmplitudes(String audioFilePath) {
        int chunkCount = getChunkCount(audioFilePath);
        MediaExtractor extractor = new MediaExtractor();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            extractor.setDataSource(audioFilePath);
            extractor.selectTrack(0);
            ByteBuffer inputBuffer = ByteBuffer.allocate(4096);
            while (extractor.readSampleData(inputBuffer, 0) >= 0) {
                byte[] buffer = new byte[inputBuffer.remaining()];
                inputBuffer.get(buffer);
                outputStream.write(buffer);
                inputBuffer.clear();
                extractor.advance();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            extractor.release();
        }

        byte[] pcmData = outputStream.toByteArray();
        return calculateAmplitudes(pcmData, chunkCount);
    }

    private byte[] calculateAmplitudes(byte[] pcmData, int chunkCount) {
        int chunkSize = pcmData.length / chunkCount;
        byte[] amplitudes = new byte[chunkCount];

        for (int i = 0; i < chunkCount; i++) {
            int start = i * chunkSize;
            int end = Math.min(start + chunkSize, pcmData.length);
            double sum = 0;

            for (int j = start; j < end; j += 2) {
                int sample = (pcmData[j] & 0xFF) | (pcmData[j + 1] << 8);
                sum += Math.pow(sample, 2);
            }

            double rms = Math.sqrt(sum / (chunkSize / 2));

            // Normalize the RMS value to the range of 0 to 127 for visualization
            double normalizedRMS = rms / 32768 * 127; // Assuming 16-bit PCM data
            amplitudes[i] = (byte) Math.min(127, Math.max(2, normalizedRMS));
        }

        return amplitudes;
    }

    private int getChunkCount(String audioFilePath) {
        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(audioFilePath);
            MediaFormat format = extractor.getTrackFormat(0);
            long durationUs = format.getLong(MediaFormat.KEY_DURATION); // Duration in microseconds
            int chunkCount = (int) (durationUs / 50000); // 50ms per chunk
            return chunkCount;
        } catch (IOException e) {
            e.printStackTrace();
            return 100; // Default chunk count if an error occurs
        } finally {
            extractor.release();
        }
    }




    private void setThemeForHomeScreen(@NonNull RecyclerView.ViewHolder viewHolder) {

        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int color;
        int receiveMessage;
        int receiveMessageBackground;
        int sendMessageBackground;
        int color2;

        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                color = ContextCompat.getColor(context, R.color.primaryTextColor); // White for dark mode
                color2 = ContextCompat.getColor(context, R.color.secondaryTextColor); // White for dark mode
                receiveMessage = ContextCompat.getColor(context, R.color.white); // White text for dark mode
                receiveMessageBackground = ContextCompat.getColor(context, R.color.receive_message); // Black background for dark mode
                sendMessageBackground = ContextCompat.getColor(context, R.color.send_message);
                break;
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
            default:
                color = ContextCompat.getColor(context, R.color.white); // Black for light mode
                color2 = ContextCompat.getColor(context, R.color.secondaryTextColor); // Secondary text color
                receiveMessage = ContextCompat.getColor(context, R.color.black); // Black text for light mode
                receiveMessageBackground = ContextCompat.getColor(context, R.color.white); // White background for light mode
                sendMessageBackground = ContextCompat.getColor(context, R.color.GreenishBlue);
                break;
        }

        if (viewHolder instanceof SentViewHolder) {
            SentViewHolder sentViewHolder = (SentViewHolder) viewHolder;
            sentViewHolder.sendBinding.message.setTextColor(color);
            sentViewHolder.sendBinding.messageDate.setTextColor(color2);
            sentViewHolder.sendBinding.cardviewMessageMe.setCardBackgroundColor(sendMessageBackground);
        } else if (viewHolder instanceof ReceiverViewHolder) {
            ReceiverViewHolder receiverViewHolder = (ReceiverViewHolder) viewHolder;
            receiverViewHolder.receiveBinding.message.setTextColor(receiveMessage);
            receiverViewHolder.receiveBinding.messageDate.setTextColor(color2);
            receiverViewHolder.receiveBinding.timestamp.setTextColor(color2);
            receiverViewHolder.receiveBinding.cardviewMessageOther.setCardBackgroundColor(receiveMessageBackground); // Updated to set background color
        } else if (viewHolder instanceof SentAudioViewHolder) {
            SentAudioViewHolder sentAudioViewHolder = (SentAudioViewHolder) viewHolder;
//            sentAudioViewHolder.sendAudioItemBinding.seekBar.getProgressDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
//            sentAudioViewHolder.sendAudioItemBinding.seekBar.getThumb().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
            sentAudioViewHolder.sendAudioItemBinding.cardviewVoiceMe.setCardBackgroundColor(sendMessageBackground);
        } else if (viewHolder instanceof ReceiverAudioViewHolder) {
            ReceiverAudioViewHolder receiverAudioViewHolder = (ReceiverAudioViewHolder) viewHolder;
            receiverAudioViewHolder.receiveAudioItemBinding.timestamp.setTextColor(color2);
            receiverAudioViewHolder.receiveAudioItemBinding.seekBar.getProgressDrawable().setColorFilter(color2, PorterDuff.Mode.SRC_IN);
            receiverAudioViewHolder.receiveAudioItemBinding.seekBar.getThumb().setColorFilter(color2, PorterDuff.Mode.SRC_IN);
            receiverAudioViewHolder.receiveAudioItemBinding.cardviewMessageOther.setCardBackgroundColor(receiveMessageBackground);
        }
    }

    public static class SentViewHolder extends RecyclerView.ViewHolder {
        ItemSendBinding sendBinding;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            sendBinding = ItemSendBinding.bind(itemView);
        }
    }

    public static class ReceiverViewHolder extends RecyclerView.ViewHolder {
        ItemReceiveBinding receiveBinding;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            receiveBinding = ItemReceiveBinding.bind(itemView);
        }
    }

    public static class SentAudioViewHolder extends RecyclerView.ViewHolder {
        SendAudioItemBinding sendAudioItemBinding;

        public SentAudioViewHolder(@NonNull View itemView) {
            super(itemView);
            sendAudioItemBinding = SendAudioItemBinding.bind(itemView);
        }
    }

    public static class ReceiverAudioViewHolder extends RecyclerView.ViewHolder {
        ReceiveAudioItemBinding receiveAudioItemBinding;

        public ReceiverAudioViewHolder(@NonNull View itemView) {
            super(itemView);
            receiveAudioItemBinding = ReceiveAudioItemBinding.bind(itemView);
        }
    }
}
