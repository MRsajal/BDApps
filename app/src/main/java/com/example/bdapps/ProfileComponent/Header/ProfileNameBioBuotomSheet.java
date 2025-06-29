package com.example.bdapps.ProfileComponent.Header;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.bdapps.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileNameBioBuotomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "ProfileNameBioSheet";
    private static final String BASE_URL = "https://dormitorybackend.duckdns.org/api/auth/profile";

    private TextInputEditText usersNameProfile;
    private TextInputEditText userBioProfile;
    private MaterialButton btnSaveProfile;
    private MaterialButton btnCancelProfile;

    private OkHttpClient client;
    private String accessToken;

    // Current profile data
    private String currentName = "";
    private String currentBio = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.profile_name_bio_buotom_sheet_layout, container, false);

        initializeViews(view);
        setupOkHttp();
        getAccessToken();
        loadProfileData();
        setupClickListeners();

        return view;
    }

    private void initializeViews(View view) {
        usersNameProfile = view.findViewById(R.id.users_name_profile);
        userBioProfile = view.findViewById(R.id.user_bio_profile);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnCancelProfile = view.findViewById(R.id.btnCancelProfile);
    }

    private void setupOkHttp() {
        client = new OkHttpClient();
    }

    private void getAccessToken() {
        SharedPreferences prefs = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");

        if (accessToken.isEmpty()) {
            Toast.makeText(getContext(), "Access token not found. Please login again.", Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }

    private void loadProfileData() {
        if (accessToken.isEmpty()) return;

        Request request = new Request.Builder()
                .url(BASE_URL)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to load profile data", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Failed to load profile data", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        currentName = jsonObject.optString("name", "");
                        currentBio = jsonObject.optString("bio", "");

                        // Update UI on main thread
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                usersNameProfile.setText(currentName);
                                userBioProfile.setText(currentBio);
                            });
                        }

                    } catch (JSONException e) {
                        Log.e(TAG, "Failed to parse profile data", e);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Failed to parse profile data", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                } else {
                    Log.e(TAG, "Failed to load profile data. Response code: " + response.code());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Failed to load profile data. Please try again.", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }
        });
    }

    private void setupClickListeners() {
        btnCancelProfile.setOnClickListener(v -> dismiss());

        btnSaveProfile.setOnClickListener(v -> {
            String newName = usersNameProfile.getText().toString().trim();
            String newBio = userBioProfile.getText().toString().trim();

            // Validate input
            if (newName.isEmpty()) {
                usersNameProfile.setError("Name cannot be empty");
                return;
            }

            // Check if data has changed
            if (newName.equals(currentName) && newBio.equals(currentBio)) {
                Toast.makeText(getContext(), "No changes detected", Toast.LENGTH_SHORT).show();
                dismiss();
                return;
            }

            updateProfile(newName, newBio);
        });
    }

    private void updateProfile(String name, String bio) {
        if (accessToken.isEmpty()) {
            Toast.makeText(getContext(), "Access token not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable save button to prevent multiple requests
        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setText("Saving...");

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("name", name);
            jsonBody.put("bio", bio);

            RequestBody body = RequestBody.create(
                    jsonBody.toString(),
                    MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(BASE_URL)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Type", "application/json")
                    .patch(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Failed to update profile", e);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            resetSaveButton();
                            Toast.makeText(getContext(), "Failed to update profile. Please check your connection.", Toast.LENGTH_SHORT).show();
                        });
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            resetSaveButton();

                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();

                                // Update current values
                                currentName = name;
                                currentBio = bio;

                                // Dismiss the bottom sheet
                                dismiss();
                            } else {
                                Log.e(TAG, "Failed to update profile. Response code: " + response.code());
                                Toast.makeText(getContext(), "Failed to update profile. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "Failed to create JSON body", e);
            resetSaveButton();
            Toast.makeText(getContext(), "Failed to prepare data", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetSaveButton() {
        if (btnSaveProfile != null) {
            btnSaveProfile.setEnabled(true);
            btnSaveProfile.setText("Save");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up OkHttp client if needed
        if (client != null) {
            client.dispatcher().executorService().shutdown();
        }
    }
}