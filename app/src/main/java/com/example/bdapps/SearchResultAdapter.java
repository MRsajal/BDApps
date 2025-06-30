package com.example.bdapps;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {
    private List<User> userList;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public SearchResultAdapter(List<User> userList, OnUserClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView profileImageView;
        private TextView nameTextView;
        private TextView usernameTextView;
        private TextView emailTextView;
        private CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views with null checks
            profileImageView = itemView.findViewById(R.id.iv_profile_image);
            nameTextView = itemView.findViewById(R.id.tv_name);
            usernameTextView = itemView.findViewById(R.id.tv_username);
            emailTextView = itemView.findViewById(R.id.tv_email);
            cardView = itemView.findViewById(R.id.card_view);

            // Add click listener
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onUserClick(userList.get(getAdapterPosition()));
                }
            });
        }

        public void bind(User user) {
            // Null checks for all operations
            if (user == null) return;

            // Set username
            if (usernameTextView != null) {
                usernameTextView.setText("@" + user.getUsername());
            }

            // Set email
            if (emailTextView != null) {
                emailTextView.setText(user.getEmail());
            }

            // Set name from profile
            if (nameTextView != null) {
                String displayName = "Unknown User";
                if (user.getProfile() != null && !user.getProfile().getName().isEmpty()) {
                    displayName = user.getProfile().getName();
                } else if (user.getUsername() != null && !user.getUsername().isEmpty()) {
                    displayName = user.getUsername();
                }
                nameTextView.setText(displayName);
            }

            // Load profile image
            if (profileImageView != null) {
                String profilePicUrl = "";
                if (user.getProfile() != null) {
                    profilePicUrl = user.getProfile().getProfilePic();
                }

                if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(profilePicUrl)
                            .placeholder(R.drawable.ic_person_placeholder)
                            .error(R.drawable.ic_person_placeholder)
                            .circleCrop()
                            .into(profileImageView);
                } else {
                    profileImageView.setImageResource(R.drawable.ic_person_placeholder);
                }
            }
        }
    }
}