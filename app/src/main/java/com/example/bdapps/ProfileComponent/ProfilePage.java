package com.example.bdapps.ProfileComponent;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.bdapps.ProfileViewTabsAdapter;
import com.example.bdapps.R;
import com.example.bdapps.utils.ImageUtil;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ProfilePage extends AppCompatActivity {
    private static final String TAG = "ProfilePage";
    private static final String API_URL = "https://dormitorybackend.duckdns.org/api/auth/profile";
    private static final int PERMISSION_REQUEST_CODE = 100;

    private ShapeableImageView profileImageView;
    private TextView nameTextView, bioTextView, followersCountTextView;
    private ImageButton changeProfilePicBtn;

    private RequestQueue requestQueue;
    private String accessToken, username;

    private ActivityResultLauncher<Intent> galleryLauncher, cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new ProfileViewTabsAdapter(this));
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "About" : position == 1 ? "Posts" : "Activity");
        }).attach();
        tabLayout.post(() -> viewPager.setCurrentItem(1, false));

        requestQueue = Volley.newRequestQueue(this);
        initializeActivityLaunchers();
        getStoredCredentials();
        initViews();
        setupClickListeners();

        if (accessToken != null && !accessToken.isEmpty()) {
            fetchProfileData();
        } else {
            showError("No access token found. Please login again.");
        }
    }

    private void initializeActivityLaunchers() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) handleSelectedImage(selectedImageUri);
                    }
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        if (imageBitmap != null) handleCapturedImage(imageBitmap);
                    }
                }
        );
    }

    private void getStoredCredentials() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");
        username = prefs.getString("username", "");
        Log.d(TAG, "Access token retrieved: " + (accessToken != null && !accessToken.isEmpty() ? "Present" : "Empty"));
    }

    private void initViews() {
        profileImageView = findViewById(R.id.profile_image);
        nameTextView = findViewById(R.id.nameTextViewProfile);
        bioTextView = findViewById(R.id.usernameTextViewProfile);
        followersCountTextView = findViewById(R.id.followerCount);
        changeProfilePicBtn = findViewById(R.id.changeProfilePicBtn);
    }

    private void setupClickListeners() {
        changeProfilePicBtn.setOnClickListener(v -> showImagePickerDialog());
    }

    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Profile Picture");
        String[] options = {"Take Photo", "Choose from Gallery", "Remove Photo"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) openCamera();
            else if (which == 1) openGallery();
            else removeProfilePicture();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
        } else {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(cameraIntent);
        }
    }

    private void openGallery() {
        String permission = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU ?
                Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_REQUEST_CODE);
        } else {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryIntent.setType("image/*");
            galleryLauncher.launch(galleryIntent);
        }
    }

    private void handleSelectedImage(Uri imageUri) {
        try (InputStream inputStream = getContentResolver().openInputStream(imageUri)) {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap != null) {
                Bitmap resized = resizeBitmap(bitmap, 500, 500);
                uploadProfilePicture(resized);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading image: " + e.getMessage());
            showError("Error loading image");
        }
    }

    private void handleCapturedImage(Bitmap bitmap) {
        Bitmap resized = resizeBitmap(bitmap, 500, 500);
        uploadProfilePicture(resized);
    }

    private Bitmap resizeBitmap(Bitmap original, int maxWidth, int maxHeight) {
        int width = original.getWidth();
        int height = original.getHeight();
        if (width <= maxWidth && height <= maxHeight) return original;
        float ratio = (float) width / height;
        if (ratio > 1) {
            width = maxWidth;
            height = (int) (width / ratio);
        } else {
            height = maxHeight;
            width = (int) (height * ratio);
        }
        return Bitmap.createScaledBitmap(original, width, height, true);
    }

    public void uploadProfilePicture(Bitmap bitmap) {
        String base64Image = ImageUtil.bitmapToBase64(bitmap, Bitmap.CompressFormat.JPEG, 90);
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("profile_pic_base64", base64Image);
        } catch (JSONException e) {
            showError("Image data creation failed.");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PATCH,
                API_URL,
                requestBody,
                response -> {
                    showSuccess("Profile picture updated.");
                    fetchProfileData();
                },
                error -> {
                    Log.e(TAG, "Upload failed: " + error.toString());
                    if (error.networkResponse != null) {
                        String body = new String(error.networkResponse.data);
                        Log.e(TAG, "Error body: " + body);
                    }
                    showError("Failed to update profile picture.");
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void removeProfilePicture() {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("profile_pic_base64", "");
        } catch (JSONException e) {
            showError("Failed to prepare remove request.");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PATCH,
                API_URL,
                jsonBody,
                response -> {
                    showSuccess("Profile picture removed.");
                    fetchProfileData();
                },
                error -> {
                    showError("Failed to remove profile picture.");
                    Log.e(TAG, "Remove error: " + error.toString());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void fetchProfileData() {
        Log.d(TAG, "Fetching profile data...");
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                API_URL,
                null,
                response -> {
                    Log.d(TAG, "Profile response: " + response.toString());
                    String name = response.optString("name", "Unknown");
                    String profilePic = response.optString("profile_pic", "");
                    String followers = response.optString("followers_count", "0");
                    String bio = response.optString("bio", "");

                    Log.d(TAG, "Profile pic Base64: " + profilePic);
                    updateUI(name, profilePic, followers, bio);
                },
                error -> {
                    Log.e(TAG, "Error fetching profile: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Error status code: " + error.networkResponse.statusCode);
                        String body = new String(error.networkResponse.data);
                        Log.e(TAG, "Error response body: " + body);
                    }
                    showError("Failed to load profile data");
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void updateUI(String name, String profilePic, String followers, String bio) {
        Log.d(TAG, "Updating UI with profile pic: " + profilePic);

        nameTextView.setText(name);
        bioTextView.setText(bio);
        followersCountTextView.setText(followers);

        if (profilePic != null && !profilePic.isEmpty()) {
            Log.d(TAG, "Loading profile image as Base64");
            loadBase64Image(profilePic);
        } else {
            Log.d(TAG, "No profile picture, using default avatar");
            profileImageView.setImageResource(R.drawable.avatar);
        }
    }

    private void loadBase64Image(String base64Image) {
        try {
            // Remove data:image prefix if present
            String cleanBase64 = base64Image;
            if (base64Image.startsWith("data:image")) {
                cleanBase64 = base64Image.substring(base64Image.indexOf(",") + 1);
            }

            byte[] decodedString = Base64.decode(cleanBase64, Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            if (decodedBitmap != null) {
                Glide.with(this)
                        .load(decodedBitmap)
                        .transform(new CircleCrop())
                        .placeholder(R.drawable.avatar)
                        .error(R.drawable.avatar)
                        .into(profileImageView);
                Log.d(TAG, "Base64 image loaded successfully");
            } else {
                Log.e(TAG, "Failed to decode base64 image");
                profileImageView.setImageResource(R.drawable.avatar);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading base64 image: " + e.getMessage());
            profileImageView.setImageResource(R.drawable.avatar);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showSuccess("Permission granted. Please try again.");
        } else {
            showError("Permission denied.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) requestQueue.cancelAll(TAG);
    }
}