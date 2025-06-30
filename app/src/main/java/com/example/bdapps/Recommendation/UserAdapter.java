package com.example.bdapps.Recommendation;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.bdapps.R;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> users;
    private Context context;

    public UserAdapter(List<User> users, Context context) {
        this.users = users;
        this.context = context;
    }
    @NonNull
    @Override
    public UserAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        private ImageView profileImageView;
        private TextView nameTextView;
        private TextView usernameTextView;
        private TextView bioTextView;
        private TextView jobTextView;
        private TextView educationTextView;
        private TextView followersTextView;
        private Button followButton;
        private Button websiteButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profile_image);
            nameTextView = itemView.findViewById(R.id.name_text);
            usernameTextView = itemView.findViewById(R.id.username_text);
            bioTextView = itemView.findViewById(R.id.bio_text);
            jobTextView = itemView.findViewById(R.id.job_text);
            educationTextView = itemView.findViewById(R.id.education_text);
            followersTextView = itemView.findViewById(R.id.followers_text);
            followButton = itemView.findViewById(R.id.follow_button);
            websiteButton = itemView.findViewById(R.id.website_button);
        }

        public void bind(User user) {
            Profile profile = user.getProfile();

            // Set profile image
            if (profile != null && !TextUtils.isEmpty(profile.getProfilePic())) {
                Glide.with(context)
                        .load(profile.getProfilePic())
                        .transform(new CircleCrop())
                        .placeholder(R.drawable.ic_person_placeholder)
                        .into(profileImageView);
            } else {
                profileImageView.setImageResource(R.drawable.ic_person_placeholder);
            }

            // Set name and username
            if (profile != null && !TextUtils.isEmpty(profile.getName())) {
                nameTextView.setText(profile.getName());
                nameTextView.setVisibility(View.VISIBLE);
            } else {
                nameTextView.setVisibility(View.GONE);
            }

            usernameTextView.setText("@" + user.getUsername());

            // Set bio
            if (profile != null && !TextUtils.isEmpty(profile.getBio())) {
                bioTextView.setText(profile.getBio());
                bioTextView.setVisibility(View.VISIBLE);
            } else {
                bioTextView.setVisibility(View.GONE);
            }

            // Set job info
            if (profile != null && !TextUtils.isEmpty(profile.getCurrentJob())) {
                String jobText = profile.getCurrentJob();
                if (!TextUtils.isEmpty(profile.getCurrentCompany())) {
                    jobText += " at " + profile.getCurrentCompany();
                }
                jobTextView.setText(jobText);
                jobTextView.setVisibility(View.VISIBLE);
            } else {
                jobTextView.setVisibility(View.GONE);
            }

            // Set education
            if (profile != null && !TextUtils.isEmpty(profile.getEducation())) {
                String eduText = profile.getEducation();
                if (!TextUtils.isEmpty(profile.getDegree())) {
                    eduText = profile.getDegree() + " • " + eduText;
                }
                educationTextView.setText(eduText);
                educationTextView.setVisibility(View.VISIBLE);
            } else {
                educationTextView.setVisibility(View.GONE);
            }

            // Set followers count
            if (profile != null && !TextUtils.isEmpty(profile.getFollowersCount())) {
                followersTextView.setText(profile.getFollowersCount() + " followers");
                followersTextView.setVisibility(View.VISIBLE);
            } else {
                followersTextView.setVisibility(View.GONE);
            }

            // Follow button
            followButton.setText(user.isFollowing() ? "Following" : "Follow");
            followButton.setOnClickListener(v -> {
                user.setFollowing(!user.isFollowing());
                followButton.setText(user.isFollowing() ? "Following" : "Follow");
            });

            // Website button
            if (profile != null && !TextUtils.isEmpty(profile.getPersonalWebsite())) {
                websiteButton.setVisibility(View.VISIBLE);
                websiteButton.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(profile.getPersonalWebsite()));
                    context.startActivity(intent);
                });
            } else {
                websiteButton.setVisibility(View.GONE);
            }
        }
    }

}
