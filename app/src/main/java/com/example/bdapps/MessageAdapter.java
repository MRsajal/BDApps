package com.example.bdapps;

import android.content.Context;
import android.os.Message;
import android.print.PageRange;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_USER=1;
    private static final int VIEW_TYPE_BOT=2;
    private List<PreviousMessage> messages;
    private Context context;
    private SimpleDateFormat timeFormat;
    public MessageAdapter(List<PreviousMessage> messages,Context context){
        this.messages=messages;
        this.context=context;
        this.timeFormat=new SimpleDateFormat("HH:mm", Locale.getDefault());
    }
    @Override
    public int getItemViewType(int position){
        PreviousMessage message=messages.get(position);
        return message.isFromUser()?VIEW_TYPE_USER:VIEW_TYPE_BOT;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_user_history, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_bot_history, parent, false);
            return new BotMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PreviousMessage message = messages.get(position);

        if (holder instanceof UserMessageViewHolder) {
            UserMessageViewHolder userHolder = (UserMessageViewHolder) holder;
            userHolder.messageText.setText(message.getContent());
            userHolder.timeText.setText(formatTime(message.getTimestamp()));
        } else if (holder instanceof BotMessageViewHolder) {
            BotMessageViewHolder botHolder = (BotMessageViewHolder) holder;
            botHolder.messageText.setText(message.getContent());
            botHolder.timeText.setText(formatTime(message.getTimestamp()));
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
    private String formatTime(String timestamp) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(timestamp);
            return timeFormat.format(date);
        } catch (Exception e) {
            return "";
        }
    }
    public static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timeText;

        public UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageTextView);
            timeText = itemView.findViewById(R.id.tv_post_time);
        }
    }
    public static class BotMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timeText;

        public BotMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageTextView);
            timeText = itemView.findViewById(R.id.tv_post_time);
        }
    }
}
