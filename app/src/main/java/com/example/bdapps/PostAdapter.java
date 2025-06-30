package com.example.bdapps;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Post> posts;
    private static final String TAG = "PostAdapter";

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
        private ImageView profile;
        private Post currentPost;
        private static Map<String, String> profileImageCache = new HashMap<>();
        private static Map<String, String> usersCache = new HashMap<>();
        private static boolean usersLoaded = false;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername=itemView.findViewById(R.id.tv_username);
            tvPostTime=itemView.findViewById(R.id.tv_post_time);
            profile=itemView.findViewById(R.id.profile_image); // Uncommented this line
            tvPostContent=itemView.findViewById(R.id.tv_post_content);
            btnLike=itemView.findViewById(R.id.btn_like);
            btnComment=itemView.findViewById(R.id.btn_comment);
            btnShare=itemView.findViewById(R.id.btn_share);
            setupClickListeners();
        }

        public void bind(Post post){
            this.currentPost = post;

            tvUsername.setText(post.getUsername());
            tvPostTime.setText(post.getTimeAgo());
            tvPostContent.setText(post.getContent());

            // Clear previous image to avoid recycling issues
            Glide.with(profile.getContext()).clear(profile);
            profile.setImageResource(R.drawable.img); // Temporary default while loading

            loadProfileImage(post.getUsername());
        }


        private void loadProfileImage(String username) {
            Log.d(TAG, "Loading profile image for username: " + username);

            // Check if we already have this user's profile image cached
            if (profileImageCache.containsKey(username)) {
                String cachedImageUrl = profileImageCache.get(username);
                if (cachedImageUrl != null && !cachedImageUrl.isEmpty()) {
                    loadImageIntoView(cachedImageUrl, profile);
                } else {
                    setDefaultProfileImage();
                }
                return;
            }

            // If users are not loaded yet, load all users first
            if (!usersLoaded) {
                loadAllUsers(username);
            } else {
                if (!usersCache.containsKey(username)) {
                    // Re-fetch all users in case it's missing
                    usersLoaded = false;
                    loadAllUsers(username);
                } else {
                    findUserProfileImage(username);
                }
            }
        }

        private void loadAllUsers(String targetUsername) {
            // Get token from SharedPreferences
            SharedPreferences prefs = itemView.getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            String token = prefs.getString("access_token", "");

            if (token.isEmpty()) {
                Log.e(TAG, "No access token found");
                setDefaultProfileImage();
                return;
            }

            RequestQueue queue = Volley.newRequestQueue(itemView.getContext());
            String url = "https://dormitorybackend.duckdns.org/api/auth/users";

            JsonArrayRequest usersRequest = new JsonArrayRequest(
                    Request.Method.GET, url, null,
                    response -> {
                        try {
                            Log.d(TAG, "Users API response length: " + response.length());
                            Log.d(TAG, "Full Users API response: " + response.toString());

                            // Clear previous cache
                            usersCache.clear();
                            profileImageCache.clear();

                            // Parse all users and cache their profile images
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject user = response.getJSONObject(i);

                                // Log all keys in the user object to see the structure
                                Log.d(TAG, "User " + i + " keys: " + user.keys().toString());
                                Log.d(TAG, "User " + i + " full data: " + user.toString());

                                String username = user.optString("username", "");
                                JSONObject profileObj = user.optJSONObject("profile");
                                String profilePic = "";

                                // Try alternative field names in case the API uses different naming
                                if (username.isEmpty()) {
                                    username = user.optString("user_name", "");
                                }
                                if (username.isEmpty()) {
                                    username = user.optString("name", "");
                                }

                                if (profileObj != null) {
                                    profilePic = profileObj.optString("profile_pic", "");
                                }

                                Log.d(TAG, "Parsed user - Username: '" + username + "', ProfilePic: '" + profilePic + "'");

                                if (!username.isEmpty()) {
                                    usersCache.put(username, profilePic);
                                    profileImageCache.put(username, profilePic);
                                    Log.d(TAG, "Cached user: " + username + " with image: " + profilePic);
                                }
                            }

                            Log.d(TAG, "Total users cached: " + usersCache.size());
                            Log.d(TAG, "Looking for target username: '" + targetUsername + "'");

                            usersLoaded = true;

                            // Now load the specific user's profile image
                            findUserProfileImage(targetUsername);

                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing users response", e);
                            e.printStackTrace();
                            profileImageCache.put(targetUsername, "");
                            setDefaultProfileImage();
                        }
                    },
                    error -> {
                        Log.e(TAG, "Users load error", error);
                        if (error.networkResponse != null) {
                            Log.e(TAG, "Status Code: " + error.networkResponse.statusCode);
                            try {
                                String responseBody = new String(error.networkResponse.data, "utf-8");
                                Log.e(TAG, "Error Response Data: " + responseBody);
                            } catch (Exception e) {
                                Log.e(TAG, "Could not parse error response", e);
                            }
                        }
                        profileImageCache.put(targetUsername, "");
                        setDefaultProfileImage();
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };

            usersRequest.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                    10000,
                    com.android.volley.DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            queue.add(usersRequest);
        }

        private void findUserProfileImage(String username) {
            String profileImageUrl = usersCache.get(username);

            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                loadImageIntoView(profileImageUrl, profile);
            } else {
                profileImageCache.put(username, "");
                setDefaultProfileImage();
            }
        }


        private void loadImageIntoView(String imageUrl, ImageView imageView) {
            Log.d(TAG, "Loading image with Glide: " + imageUrl);

            Glide.with(imageView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.img)
                    .error(R.drawable.img)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .circleCrop()
                    .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                            Log.e(TAG, "Glide failed to load image: " + imageUrl, e);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                            Log.d(TAG, "Glide successfully loaded image: " + imageUrl);
                            return false;
                        }
                    })
                    .into(imageView);
        }

        private void setDefaultProfileImage() {
            Log.d(TAG, "Setting default profile image");
            if (profile != null) {
                profile.setImageResource(R.drawable.img);
            }
        }

        private void setupClickListeners() {
            btnLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "Liked!", Toast.LENGTH_SHORT).show();
                }
            });

            btnComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Integer postId = getCurrentPostId();
                    Intent intent = new Intent(v.getContext(), CommentActivity.class);
                    intent.putExtra("POST_ID", postId);
                    v.getContext().startActivity(intent);
                }
            });

            btnShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "Share clicked!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private Integer getCurrentPostId() {
            if (currentPost != null) {
                return currentPost.getId();
            }
            return -1;
        }

        public static void clearProfileImageCache() {
            profileImageCache.clear();
            usersCache.clear();
            usersLoaded = false;
        }
    }
}