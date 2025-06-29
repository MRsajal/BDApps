package com.example.bdapps;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.AsyncTask;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        private ImageView profile;
        private Post currentPost;
        private static Map<String, String> profileImageCache = new HashMap<>();

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername=itemView.findViewById(R.id.tv_username);
            tvPostTime=itemView.findViewById(R.id.tv_post_time);
            profile=itemView.findViewById(R.id.profile_image);
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
            this.currentPost=post;
            loadProfileImage(post.getUsername());
        }
        private void loadProfileImage(String username) {
            // Check cache first
            if (profileImageCache.containsKey(username)) {
                String cachedImageUrl = profileImageCache.get(username);
                if (cachedImageUrl != null && !cachedImageUrl.isEmpty()) {
                    loadImageIntoView(cachedImageUrl, profile);
                } else {
                    setDefaultProfileImage();
                }
                return;
            }
            RequestQueue queue = Volley.newRequestQueue(itemView.getContext());
            String url = "https://dormitorybackend.duckdns.org/api/auth/profile?username=" + username;

            JsonObjectRequest profileRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String profileImageUrl = response.optString("profile_image", "");

                                // Cache the result
                                profileImageCache.put(username, profileImageUrl);

                                // Load image if URL exists
                                if (!profileImageUrl.isEmpty()) {
                                    loadImageIntoView(profileImageUrl, profile);
                                } else {
                                    setDefaultProfileImage();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e("PROFILE_ERROR", "Error parsing profile response for user: " + username, e);
                                profileImageCache.put(username, ""); // Cache empty result
                                setDefaultProfileImage();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            Log.e("PROFILE_NETWORK_ERROR", "Error loading profile for user: " + username, error);
                            profileImageCache.put(username, ""); // Cache empty result
                            setDefaultProfileImage();
                        }
                    });

            queue.add(profileRequest);
        }
        private void loadImageIntoView(String imageUrl, ImageView imageView) {
            Glide.with(imageView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.img)
                    .error(R.drawable.img)
                    .circleCrop()
                    .into(imageView);
        }

        private void setDefaultProfileImage() {
            // Set a default profile image
            profile.setImageResource(R.drawable.img); // Make sure you have this drawable
        }
        private static class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
            private WeakReference<ImageView> imageViewRef;

            LoadImageTask(ImageView imageView) {
                imageViewRef = new WeakReference<>(imageView);
            }

            @Override
            protected Bitmap doInBackground(String... urls) {
                String imageUrl = urls[0];
                try {
                    URL url = new URL(imageUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    return BitmapFactory.decodeStream(input);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                ImageView imageView = imageViewRef.get();
                if (imageView != null && bitmap != null) {
                    // Create circular bitmap for profile image
                    Bitmap circularBitmap = getCircularBitmap(bitmap);
                    imageView.setImageBitmap(circularBitmap);
                }
            }

            private Bitmap getCircularBitmap(Bitmap bitmap) {
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                int size = Math.min(width, height);

                Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(output);

                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setFilterBitmap(true);
                paint.setDither(true);

                canvas.drawARGB(0, 0, 0, 0);
                canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);

                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                canvas.drawBitmap(bitmap, (size - width) / 2f, (size - height) / 2f, paint);

                return output;
            }
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
                    Integer postId=getCurrentPostId();
                    Intent intent=new Intent(v.getContext(),CommentActivity.class);
                    intent.putExtra("POST_ID",postId);
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
                return currentPost.getId(); // make sure your Post class has getId()
            }
            return -1; // or any default value if currentPost is null
        }

        public static void clearProfileImageCache() {
            profileImageCache.clear();
        }
    }
}
