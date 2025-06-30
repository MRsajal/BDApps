package com.example.bdapps.SearchPost;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bdapps.Post;
import com.example.bdapps.R;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private Context context;
    private List<Post> postList;
    private OnPostClickListener onPostClickListener;
    public interface OnPostClickListener {
        void onPostClick(Post post);
    }

    public PostAdapter(Context context, List<Post> postList, OnPostClickListener listener) {
        this.context = context;
        this.postList = postList;
        this.onPostClickListener = listener;
    }
    @NonNull
    @Override
    public PostAdapter.PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.PostViewHolder holder, int position) {
        Post post = postList.get(position);

        holder.contentTextView.setText(post.getContent());
        holder.usernameTextView.setText(post.getUsername());
        holder.timeTextView.setText(post.getTimeAgo());

        // Set click listener for the entire item
        holder.itemView.setOnClickListener(v -> {
            if (onPostClickListener != null) {
                onPostClickListener.onPostClick(post);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        TextView contentTextView, usernameTextView, timeTextView;
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
        }
    }
}
