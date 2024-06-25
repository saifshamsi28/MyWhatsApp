package com.saif.mywhatsapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.saif.mywhatsapp.Models.Message;
import com.saif.mywhatsapp.R;
import com.saif.mywhatsapp.databinding.ItemReceiveBinding;
import com.saif.mywhatsapp.databinding.ItemSendBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MessagesAdapter extends RecyclerView.Adapter{
    private Context context;
    private ArrayList<Message> messages;
    private final int INT_ITEM_SENT=1;
    private final int INT_ITEM_RECEIVE=2;


    public MessagesAdapter(Context context,ArrayList<Message> messages){
        this.context=context;
        this.messages=messages;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==INT_ITEM_SENT){
            View view= LayoutInflater.from(context).inflate(R.layout.item_send,parent,false);
            return new SentViewHolder(view);
        }
        else {
            View view= LayoutInflater.from(context).inflate(R.layout.item_receive,parent,false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int []reactions=new int[]{
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry
        };
        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();

        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {
            return true; // true is closing popup, false is requesting a new selection
        });
        Message message=messages.get(position);

//        if(holder.getClass()==SentViewHolder.class){
//            SentViewHolder viewHolder = (SentViewHolder) holder;
//            viewHolder.sendBinding.message.setText(message.getMessage());
//            Date date = new Date(message.getTimeStamp());
//            // to format time (12-hour format with AM/PM)
//            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
//            //to Format the date
//            String formattedTime = timeFormat.format(date);
//            // Setting the formatted timestamp(in 12hours AM/PM) to the timestamp-TextView
//            viewHolder.sendBinding.timestamp.setText(formattedTime);
//            viewHolder.sendBinding.layoutChatSendContainer.post(() -> {
//                // combined width of message and timestamp
//                int messageWidth = viewHolder.sendBinding.message.getMeasuredWidth() + viewHolder.sendBinding.message.getPaddingLeft() + viewHolder.sendBinding.message.getPaddingRight();
//                int timestampWidth = viewHolder.sendBinding.timestamp.getMeasuredWidth() + viewHolder.sendBinding.timestamp.getPaddingLeft() + viewHolder.sendBinding.timestamp.getPaddingRight();
//                int combinedWidth = messageWidth + timestampWidth;
//
//                // Calculate the available width (excluding padding/margin if necessary)
//                int maxWidth = viewHolder.sendBinding.message.getMaxWidth();
//                // Adjust orientation based on the combined width
//                if (combinedWidth <= maxWidth) {
//                    viewHolder.sendBinding.layoutChatSendContainer.setOrientation(LinearLayout.HORIZONTAL);
//                } else {
////                viewHolder.sendBinding.message.setPaddingRelative(24, 16,16,8);
//                    viewHolder.sendBinding.layoutChatSendContainer.setOrientation(LinearLayout.VERTICAL);
//                }
//            });
//
//            // Format the date for the header(for every new day)
//            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
//            String messageDay = sdf.format(date); //date of the current message
//
//            if (position == 0) {
//                // Showing the date for the first message
//                viewHolder.sendBinding.messageDate.setVisibility(View.VISIBLE);
//                viewHolder.sendBinding.messageDate.setText(messageDay);
//            } else {
//                Message previousMessage = messages.get(position - 1);//to check whether the current and previous
//                                                                    // message sent on same day or different day
//                String previousMessageDay = sdf.format(new Date(previousMessage.getTimeStamp())); //previous message date
//                if (!messageDay.equals(previousMessageDay)) {
//                    // Show the date if current message date is different from the previous message's date
//                    viewHolder.sendBinding.messageDate.setVisibility(View.VISIBLE);
//                    viewHolder.sendBinding.messageDate.setText(messageDay);
//                } else {
//                    // Hide the date if it's the same as the previous message's date(only show the date once per day)
//                    viewHolder.sendBinding.messageDate.setVisibility(View.GONE);
//                }
//            }
//        }else {
//            ReceiverViewHolder viewHolder=(ReceiverViewHolder) holder;
//            viewHolder.receiveBinding.message.setText(message.getMessage());
//
//            Date date = new Date(message.getTimeStamp());
//
//            // to format time (12-hour format with AM/PM)
//            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
//
//            // Format the date
//            String formattedDate = dateFormat.format(date);
//
//            // Set the formatted time to the timestamp-TextView
//            viewHolder.receiveBinding.timestamp.setText(formattedDate);
//            viewHolder.receiveBinding.layoutChatReceiveContainer.getViewTreeObserver().addOnGlobalLayoutListener(
//                    new ViewTreeObserver.OnGlobalLayoutListener() {
//                        @Override
//                        public void onGlobalLayout() {
//                            int messageWidth = viewHolder.receiveBinding.message.getWidth() + viewHolder.receiveBinding.message.getPaddingLeft() + viewHolder.receiveBinding.message.getPaddingRight();
//                            int timestampWidth = viewHolder.receiveBinding.timestamp.getWidth() + viewHolder.receiveBinding.timestamp.getPaddingLeft() + viewHolder.receiveBinding.timestamp.getPaddingRight();
//                            int combinedWidth = messageWidth + timestampWidth;
//
//                            // Calculate the available width (excluding padding/margin if necessary)
//                            int maxWidth = viewHolder.receiveBinding.message.getMaxWidth();
//
//                            // Adjust orientation based on the combined width
//                            if (combinedWidth <= maxWidth) {
//                                viewHolder.receiveBinding.layoutChatReceiveContainer.setOrientation(LinearLayout.HORIZONTAL);
//                            } else {
//                                viewHolder.receiveBinding.layoutChatReceiveContainer.setOrientation(LinearLayout.VERTICAL);
//                            }
//
//                            // Remove the listener to avoid repeated calls
//                            viewHolder.receiveBinding.layoutChatReceiveContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                        }
//                    });
//
//            //to format the date for message date header (for every new day)
//            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
//            String messageDay = sdf.format(date);
//
//            if (position == 0) {
//                // Show the date for the first message
//                viewHolder.receiveBinding.messageDate.setVisibility(View.VISIBLE);
//                viewHolder.receiveBinding.messageDate.setText(messageDay);
//            } else {
//                //to check whether the current and previous message sent on same day or different day
//                Message previousMessage = messages.get(position - 1);
//                String previousMessageDay = sdf.format(new Date(previousMessage.getTimeStamp()));
//                if (!messageDay.equals(previousMessageDay)) {
//                    // Show the date if current message's date is different from the previous message's date
//                    viewHolder.receiveBinding.messageDate.setVisibility(View.VISIBLE);
//                    viewHolder.receiveBinding.messageDate.setText(messageDay);
//                } else {
//                    // Hide the date if it's the same as the previous message's date
//                    viewHolder.receiveBinding.messageDate.setVisibility(View.GONE);
//                }
//            }
//        }
        if(holder.getClass()==SentViewHolder.class){
            SentViewHolder viewHolder = (SentViewHolder) holder;
            viewHolder.sendBinding.message.setText(message.getMessage());
            Date date = new Date(message.getTimeStamp());
            // to format time (12-hour format with AM/PM)
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            //to Format the date
            String formattedTime = timeFormat.format(date);
            // Setting the formatted timestamp(in 12hours AM/PM) to the timestamp-TextView
            viewHolder.sendBinding.timestamp.setText(formattedTime);

            // Set the message status
            switch (message.getStatus()) {
                case Message.STATUS_SENT:
                    viewHolder.sendBinding.statusIcon.setImageResource(R.drawable.sent_tick);
                    break;
                case Message.STATUS_DELIVERED:
                    viewHolder.sendBinding.statusIcon.setImageResource(R.drawable.delivered_tick);
                    break;
                case Message.STATUS_READ:
                    viewHolder.sendBinding.statusIcon.setImageResource(R.drawable.seen_tick);
                    break;
            }
            // Adjust layout
            viewHolder.sendBinding.layoutChatSendContainer.post(() -> {
                int messageWidth = viewHolder.sendBinding.message.getMeasuredWidth() + viewHolder.sendBinding.message.getPaddingLeft() + viewHolder.sendBinding.message.getPaddingRight();
                int timestampWidth = viewHolder.sendBinding.timestamp.getMeasuredWidth() + viewHolder.sendBinding.timestamp.getPaddingLeft() + viewHolder.sendBinding.timestamp.getPaddingRight();
                int combinedWidth = messageWidth + timestampWidth;

                int maxWidth = viewHolder.sendBinding.message.getMaxWidth();
                if (combinedWidth <= maxWidth) {
                    viewHolder.sendBinding.layoutChatSendContainer.setOrientation(LinearLayout.HORIZONTAL);
                } else {
                    viewHolder.sendBinding.layoutChatSendContainer.setOrientation(LinearLayout.VERTICAL);
                }
            });

            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            String messageDay = sdf.format(date);

            if (position == 0) {
                viewHolder.sendBinding.messageDate.setVisibility(View.VISIBLE);
                viewHolder.sendBinding.messageDate.setText(messageDay);
            } else {
                Message previousMessage = messages.get(position - 1);
                String previousMessageDay = sdf.format(new Date(previousMessage.getTimeStamp()));
                if (!messageDay.equals(previousMessageDay)) {
                    viewHolder.sendBinding.messageDate.setVisibility(View.VISIBLE);
                    viewHolder.sendBinding.messageDate.setText(messageDay);
                } else {
                    viewHolder.sendBinding.messageDate.setVisibility(View.GONE);
                }
            }
        } else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            viewHolder.receiveBinding.message.setText(message.getMessage());

            Date date = new Date(message.getTimeStamp());
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            String formattedDate = dateFormat.format(date);
            viewHolder.receiveBinding.timestamp.setText(formattedDate);

            viewHolder.receiveBinding.layoutChatReceiveContainer.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            int messageWidth = viewHolder.receiveBinding.message.getWidth() + viewHolder.receiveBinding.message.getPaddingLeft() + viewHolder.receiveBinding.message.getPaddingRight();
                            int timestampWidth = viewHolder.receiveBinding.timestamp.getWidth() + viewHolder.receiveBinding.timestamp.getPaddingLeft() + viewHolder.receiveBinding.timestamp.getPaddingRight();
                            int combinedWidth = messageWidth + timestampWidth;

                            int maxWidth = viewHolder.receiveBinding.message.getMaxWidth();

                            if (combinedWidth <= maxWidth) {
                                viewHolder.receiveBinding.layoutChatReceiveContainer.setOrientation(LinearLayout.HORIZONTAL);
                            } else {
                                viewHolder.receiveBinding.layoutChatReceiveContainer.setOrientation(LinearLayout.VERTICAL);
                            }

                            viewHolder.receiveBinding.layoutChatReceiveContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    });

            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            String messageDay = sdf.format(date);

            if (position == 0) {
                viewHolder.receiveBinding.messageDate.setVisibility(View.VISIBLE);
                viewHolder.receiveBinding.messageDate.setText(messageDay);
            } else {
                Message previousMessage = messages.get(position - 1);
                String previousMessageDay = sdf.format(new Date(previousMessage.getTimeStamp()));
                if (!messageDay.equals(previousMessageDay)) {
                    viewHolder.receiveBinding.messageDate.setVisibility(View.VISIBLE);
                    viewHolder.receiveBinding.messageDate.setText(messageDay);
                } else {
                    viewHolder.receiveBinding.messageDate.setVisibility(View.GONE);
                }
            }
        }

    }

    @Override
    public int getItemViewType(int position) {
        Message message= messages.get(position);
        if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(message.getSenderId())){
            return INT_ITEM_SENT;
        }
        else {
            return INT_ITEM_RECEIVE;
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class SentViewHolder extends RecyclerView.ViewHolder{
        ItemSendBinding sendBinding;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            sendBinding=ItemSendBinding.bind(itemView);
        }
    }
    public class ReceiverViewHolder extends RecyclerView.ViewHolder{

        ItemReceiveBinding receiveBinding;
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            receiveBinding=ItemReceiveBinding.bind(itemView);
        }
    }
}
