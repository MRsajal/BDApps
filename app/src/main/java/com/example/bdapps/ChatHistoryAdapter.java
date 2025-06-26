package com.example.bdapps;

import android.content.Context;
import android.content.Intent;
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

public class ChatHistoryAdapter extends RecyclerView.Adapter<ChatHistoryAdapter.ViewHolder> {

    private List<Conversation> conversations;
    private Context context;
    private SimpleDateFormat dateFormat;

    public ChatHistoryAdapter(List<Conversation> conversations,Context context){
        this.conversations=conversations;
        this.context=context;
        this.dateFormat=new SimpleDateFormat("MMM dd, yyyy HH:mm");
    }

    @NonNull
    @Override
    public ChatHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_chat_history,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatHistoryAdapter.ViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);

        holder.titleTextView.setText(conversation.getTitle());
        holder.previewTextView.setText(conversation.getPreviewText());

        // Format and display date
        String dateText = formatDate(conversation.getUpdatedAt());
        holder.dateTextView.setText(dateText);

        // Set click listener to open detailed chat view
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatHistoryActivity.class);
            intent.putExtra("conversation_id", conversation.getId());
            intent.putExtra("conversation_title", conversation.getTitle());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    private String formatDate(String dateString) {
        try {
            // Assuming the date comes in ISO format, adjust as needed
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return dateFormat.format(date);
        } catch (Exception e) {
            return dateString; // Return original string if parsing fails
        }
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView previewTextView;
        TextView dateTextView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.textViewTitle);
            previewTextView = itemView.findViewById(R.id.textViewPreview);
            dateTextView = itemView.findViewById(R.id.textViewDate);
        }
    }
}
