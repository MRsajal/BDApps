package com.example.bdapps;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;

import java.util.List;
import java.util.Random;

public class OnlineUsersAdapter extends RecyclerView.Adapter<OnlineUsersAdapter.UserViewHolder> {

    private List<OnlineUser> userList;
    private Random random = new Random();

    // Array of colors for user avatars
    private final int[] avatarColors = {
            Color.parseColor("#FF5722"), // Deep Orange
            Color.parseColor("#E91E63"), // Pink
            Color.parseColor("#9C27B0"), // Purple
            Color.parseColor("#673AB7"), // Deep Purple
            Color.parseColor("#3F51B5"), // Indigo
            Color.parseColor("#2196F3"), // Blue
            Color.parseColor("#03A9F4"), // Light Blue
            Color.parseColor("#00BCD4"), // Cyan
            Color.parseColor("#009688"), // Teal
            Color.parseColor("#4CAF50"), // Green
            Color.parseColor("#8BC34A"), // Light Green
            Color.parseColor("#FFC107"), // Amber
            Color.parseColor("#FF9800"), // Orange
            Color.parseColor("#795548"), // Brown
            Color.parseColor("#607D8B")  // Blue Grey
    };

    public OnlineUsersAdapter(List<OnlineUser> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_online_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        OnlineUser user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView ivUserAvatar;
        private TextView tvUsername;
        private TextView tvDomain;
        private TextView tvGoal;
        private TextView tvStudyTimeToday;
        private TextView tvOnlineTimer;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvDomain = itemView.findViewById(R.id.tvDomain);
            tvGoal = itemView.findViewById(R.id.tvGoal);
            tvStudyTimeToday = itemView.findViewById(R.id.tvStudyTimeToday);
            tvOnlineTimer = itemView.findViewById(R.id.tvOnlineTimer);
        }

        public void bind(OnlineUser user) {
            // Set username
            tvUsername.setText(user.getUser());

            // Set domain
            String domain = user.getDomain();
            if (domain == null || domain.isEmpty() || domain.equals("null")) {
                tvDomain.setText("General");
            } else {
                tvDomain.setText(domain);
            }

            // Set goal
            String goal = user.getGoal();
            if (goal == null || goal.isEmpty() || goal.equals("null")) {
                tvGoal.setText("No goal set");
            } else {
                tvGoal.setText(goal);
            }

            // Set study time today
            tvStudyTimeToday.setText(user.getFormattedStudyTimeToday());

            // Set online timer
            tvOnlineTimer.setText(user.getFormattedOnlineTime());

            // Set avatar color based on username
            setAvatarColor(user.getUser());
        }

        private void setAvatarColor(String username) {
            // Generate consistent color based on username
            int colorIndex = Math.abs(username.hashCode()) % avatarColors.length;
            int color = avatarColors[colorIndex];

            // Create a simple colored background for the avatar
            ivUserAvatar.setColorFilter(color);
            ivUserAvatar.setBorderColor(color);

            // You can also set the first letter of username as text overlay
            // This would require creating a custom drawable or using a library
        }
    }

    public void updateUsers(List<OnlineUser> newUserList) {
        this.userList.clear();
        this.userList.addAll(newUserList);
        notifyDataSetChanged();
    }

    public void addUser(OnlineUser user) {
        userList.add(user);
        notifyItemInserted(userList.size() - 1);
    }

    public void removeUser(int position) {
        if (position >= 0 && position < userList.size()) {
            userList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void removeUser(OnlineUser user) {
        int position = userList.indexOf(user);
        if (position != -1) {
            removeUser(position);
        }
    }

    public OnlineUser getUser(int position) {
        if (position >= 0 && position < userList.size()) {
            return userList.get(position);
        }
        return null;
    }

    public List<OnlineUser> getAllUsers() {
        return userList;
    }
}