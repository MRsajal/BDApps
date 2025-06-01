package com.example.bdapps;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private static final int VIEW_TYPE_USER=1;
    private static final int VIEW_TYPE_BOT=2;

    private List<ChatMessage> messageList;
    public ChatAdapter(List<ChatMessage> messageList){
        this.messageList=messageList;
    }

    @Override
    public int getItemViewType(int position) {
        if(messageList.get(position).isUser()){
            return VIEW_TYPE_USER;
        }
        else{
            return VIEW_TYPE_BOT;
        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_USER) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_user, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_box_bot, parent, false);
        }
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ChatViewHolder holder, int position) {
        ChatMessage message=messageList.get(position);
        holder.messageTextView.setText(message.getMessage());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView=itemView.findViewById(R.id.messageTextView);
        }
    }
}
