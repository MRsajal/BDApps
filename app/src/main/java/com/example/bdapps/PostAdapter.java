package com.example.bdapps;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Post> posts;

    public PostAdapter(List<Post> posts){
        this.posts=posts;
    }

    @NonNull
    @Override
    public PostAdapter.PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card,parent,false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.PostViewHolder holder, int position) {
        Post post=posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void addPost(Post post){
        posts.add(0,post);
        notifyItemInserted(0);
    }
    public static class PostViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUsername,tvPostTime,tvPostContent;
        private ImageButton btnLike,btnComment,btnShare;
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername=itemView.findViewById(R.id.tv_username);
            tvPostTime=itemView.findViewById(R.id.tv_post_time);
            tvPostContent=itemView.findViewById(R.id.tv_post_content);
            btnLike=itemView.findViewById(R.id.btn_like);
            btnComment=itemView.findViewById(R.id.btn_comment);
            btnShare=itemView.findViewById(R.id.btn_share);
            setupClickListerners();
        }
        public void bind(Post post){
            tvUsername.setText(post.getUsername());
            tvPostTime.setText(post.getTimeAgo());
            tvPostContent.setText(post.getContent());
        }

        private void setupClickListerners() {
            btnLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "Liked!", Toast.LENGTH_SHORT).show();
                }
            });
            btnComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "Comment clicked!", Toast.LENGTH_SHORT).show();
                }
            });
            btnShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "Share clicked!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
