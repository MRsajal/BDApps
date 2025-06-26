package com.example.bdapps;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {
    private List<User> users;
    private OnUserClickListener listener;
    public interface OnUserClickListener{
        void onUserClick(User user);
    }
    public SearchResultAdapter(List<User> users,OnUserClickListener listener){
        this.users=users;
        this.listener=listener;
    }

    @NonNull
    @Override
    public SearchResultAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchResultAdapter.ViewHolder holder, int position) {
        User user=users.get(position);
        holder.bind(user,listener);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
    static class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView profileImageView;
        private TextView nameTextView;
        private TextView usernameTextView;
        private TextView followersTextView;
        public ViewHolder(View itemView){
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            followersTextView = itemView.findViewById(R.id.followersTextView);
        }
        public void bind(User user, OnUserClickListener listener) {
            UserProfile profile = user.getProfile();

            nameTextView.setText(profile.getName().isEmpty() ? "No name" : profile.getName());
            usernameTextView.setText("@" + user.getUsername());
            followersTextView.setText(profile.getFollowersCount() + " followers");

            // Set profile image placeholder
            profileImageView.setImageResource(R.drawable.ic_person_placeholder);

            itemView.setOnClickListener(v -> listener.onUserClick(user));
        }
    }
}
