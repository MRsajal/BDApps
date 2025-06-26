package com.example.bdapps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Request;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class ProfileImageHandler {
    private final AppCompatActivity activity;
    private final ShapeableImageView imageView;
    private final Context context;
    private final String apiBaseUrl = "https://dormitorybackend.duckdns.org/api/auth/profile";
    private final OkHttpClient client = new OkHttpClient();
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    public ProfileImageHandler(AppCompatActivity activity,ShapeableImageView imageView){
        this.activity=activity;
        this.imageView=imageView;
        this.context=activity;
        initializedActivityLaunchers();
        setupImageView();
        loadProfileImage();
    }

    public void loadProfileImage() {
        Request request = new Request.Builder()
                .url(apiBaseUrl)
                .addHeader("Authorization", "Bearer " + getAuthToken())
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                activity.runOnUiThread(() -> {
                    loadDefaultImage();
                    Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.body() != null) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        String profilePicUrl = jsonResponse.optString("profile_pic", "");

                        activity.runOnUiThread(() -> {
                            if (!profilePicUrl.isEmpty() && !profilePicUrl.equals("null")) {
                                loadImageFromUrl(profilePicUrl);
                            } else {
                                loadDefaultImage();
                            }
                        });
                    } catch (Exception e) {
                        activity.runOnUiThread(() -> loadDefaultImage());
                    }
                } else {
                    activity.runOnUiThread(() -> loadDefaultImage());
                }
            }
        });
    }

    private void loadImageFromUrl(String imageUrl) {
        Glide.with(context)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .into(imageView);
    }
    private void loadDefaultImage() {
        Glide.with(context)
                .load(R.drawable.avatar)
                .into(imageView);
    }
    private void setupImageView() {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageChangeDialog();
            }
        });
    }

    private void showImageChangeDialog() {
        String[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        new AlertDialog.Builder(context)
                .setTitle("Change Profile Picture")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            openCamera();
                            break;
                        case 1:
                            openGallery();
                            break;
                        case 2:
                            dialog.dismiss();
                            break;
                    }
                })
                .show();
    }
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(activity.getPackageManager()) != null) {
            cameraLauncher.launch(cameraIntent);
        } else {
            Toast.makeText(context, "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(galleryIntent);
    }

    private void initializedActivityLaunchers() {
        imagePickerLauncher=activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result->{
            if(result.getResultCode()== Activity.RESULT_OK && result.getData()!=null){
                Uri selectedImageUri=result.getData().getData();
                if(selectedImageUri!=null){
                    uploadProfileImage(selectedImageUri);
                }
            }
                });
        cameraLauncher=activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result->{
                    if(result.getResultCode()==Activity.RESULT_OK && result.getData()!=null){
                        Bitmap imageBitmap=(Bitmap) result.getData().getExtras().get("data");
                        if(imageBitmap!=null){
                            Uri uri=saveImageToCache(imageBitmap);
                            uploadProfileImage(uri);
                        }
                    }
                }
        );
    }

    private Uri saveImageToCache(Bitmap bitmap) {
        try {
            File file = new File(context.getCacheDir(), "camera_image_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.close();
            return Uri.fromFile(file);
        } catch (IOException e) {
            return null;
        }
    }

    private void uploadProfileImage(Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            File file = new File(context.getCacheDir(), "profile_image_" + System.currentTimeMillis() + ".jpg");

            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                            "profile_pic",
                            file.getName(),
                            RequestBody.create(file, MediaType.parse("image/*"))
                    )
                    .build();

            Request request = new Request.Builder()
                    .url(apiBaseUrl)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer " + getAuthToken())
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    activity.runOnUiThread(() ->
                            Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    activity.runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(context, "Profile image updated successfully", Toast.LENGTH_SHORT).show();
                            loadProfileImage();
                        } else {
                            Toast.makeText(context, "Failed to update profile image", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

        } catch (Exception e) {
            Toast.makeText(context, "Error processing image", Toast.LENGTH_SHORT).show();
        }
    }

    private String getAuthToken() {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return prefs.getString("access_token", "");
    }
}
