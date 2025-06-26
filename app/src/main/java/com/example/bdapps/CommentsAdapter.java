package com.example.bdapps;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {
    private List<Comment> comments;
    public CommentsAdapter(List<Comment> comments){
        this.comments=comments;
    }
    @NonNull
    @Override
    public CommentsAdapter.CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsAdapter.CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder{
        private TextView textViewAuthor;
        private TextView textViewContent;
        private TextView textViewDate;
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewAuthor=itemView.findViewById(R.id.textViewAuthor);
            textViewContent=itemView.findViewById(R.id.textViewContent);
            textViewDate=itemView.findViewById(R.id.textViewDate);
        }
        public void bind(Comment comment){
            textViewAuthor.setText(comment.getAuthor());
            textViewContent.setText(comment.getContent());
            textViewDate.setText(comment.getCreatedAt());
        }
    }
}
