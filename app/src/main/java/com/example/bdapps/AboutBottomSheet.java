package com.example.bdapps;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.bdapps.ProfileComponent.Education.Education;
import com.example.bdapps.ProfileComponent.Education.EducationAdapterEdit;
import com.example.bdapps.ProfileComponent.Education.EducationBottomSheet;
import com.example.bdapps.ProfileComponent.Education.EducationBottomSheetEdit;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.android.volley.toolbox.JsonArrayRequest;

import java.util.ArrayList;
import java.util.List;

public class AboutBottomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "AboutFragmentProfile";
    private static final String API_BASE_URL = "https://dormitorybackend.duckdns.org/api/auth/profile";

    private TextView tvInfoBio;
    private EditText tvInfoBioEdit;
    private ImageButton btnEditInfo;
    private ImageButton btnEditInfoSave;
    private ImageButton btnAddEducation;

    private RequestQueue requestQueue;
    private SharedPreferences prefs;

    private boolean isEditMode = false;
    private String originalAboutMe = "";
    private RecyclerView educationRecyclerView;
    private EducationAdapterEdit educationAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestQueue = Volley.newRequestQueue(requireContext());
        prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.about_bottom_sheet_layout, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupClickListeners();
//        setupEducationRecyclerView();
        loadProfileData();
    }

    private void initializeViews(View view) {
        tvInfoBio = view.findViewById(R.id.tvInfoBio);
        tvInfoBioEdit = view.findViewById(R.id.tvInfoBioEdit);
        btnEditInfo = view.findViewById(R.id.btnEditInfo);
        btnEditInfoSave = view.findViewById(R.id.btnEditInfoSave);
        btnAddEducation = view.findViewById(R.id.btnAddEducation);
        educationRecyclerView = view.findViewById(R.id.education_edit_recyclerView);
        setupEducationRecyclerView();

    }

    private void setupClickListeners() {
        btnEditInfo.setOnClickListener(v -> toggleEditMode(true));
        btnEditInfoSave.setOnClickListener(v -> saveProfileData());
        btnAddEducation.setOnClickListener(v -> navigateToAddEducation());
    }

    private void navigateToAddEducation() {
        dismiss();

        EducationBottomSheet addEducationBottomSheet = new EducationBottomSheet();

        addEducationBottomSheet.setOnEducationActionListener(new EducationBottomSheet.OnEducationActionListener() {
            @Override
            public void onEducationSaved() {
                loadProfileData();
            }

            @Override
            public void onEducationCancelled() {
                addEducationBottomSheet.dismiss();
            }
        });

        // Show the add education bottom sheet
        addEducationBottomSheet.show(getParentFragmentManager(), "AddEducationBottomSheet");
    }


    private void loadProfileData() {
        String accessToken = prefs.getString("access_token", "");

        if (accessToken.isEmpty()) {
            Toast.makeText(getContext(), "No access token found", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                API_BASE_URL,
                null,
                response -> {
                    try {
                        SharedPreferences.Editor editor = prefs.edit();

                        String name = response.optString("name", "").trim();
                        String profilePic = response.optString("profile_pic", "");
                        String address = response.optString("address", "");
                        String bio = response.optString("bio", "");
                        String personalWebsite = response.optString("personal_website", "");
                        String aboutMe = response.optString("about_me", "");
                        String gender = response.optString("gender", "M");

                        if (!name.isEmpty()) editor.putString("user_name", name);
                        editor.putString("profile_pic", profilePic);
                        editor.putString("address", address);
                        editor.putString("bio", bio);
                        editor.putString("personal_website", personalWebsite);
                        editor.putString("about_me", aboutMe);
                        editor.putString("gender", gender);
                        editor.apply();

                        originalAboutMe = aboutMe;

                        tvInfoBio.setText(aboutMe.isEmpty() ? "No information available" : aboutMe);
                        tvInfoBioEdit.setText(aboutMe);

                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing profile data", e);
                        Toast.makeText(getContext(), "Error loading profile data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Error loading profile", error);
                    String errorMessage = "Failed to load profile data";

                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        if (statusCode == 401) {
                            errorMessage = "Unauthorized access. Please login again.";
                        } else if (statusCode == 404) {
                            errorMessage = "Profile not found";
                        }
                    }

                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
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

    private void saveProfileData() {
        String accessToken = prefs.getString("access_token", "");
        String newAboutMe = tvInfoBioEdit.getText().toString();

        if (accessToken.isEmpty()) {
            Toast.makeText(getContext(), "No access token found", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject userData = new JSONObject();
        try {
            userData.put("about_me", newAboutMe);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON body", e);
            Toast.makeText(getContext(), "Error preparing data", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PATCH,
                API_BASE_URL,
                userData,
                response -> {
                    try {
                        String updatedAboutMe = response.optString("about_me", "");
                        tvInfoBio.setText(updatedAboutMe.isEmpty() ? "No information available" : updatedAboutMe);
                        originalAboutMe = updatedAboutMe;
                        toggleEditMode(false);
                        Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing save response", e);
                        Toast.makeText(getContext(), "Update completed but error processing response", Toast.LENGTH_SHORT).show();
                        toggleEditMode(false);
                    }
                },
                error -> {
                    Log.e(TAG, "Error saving profile", error);
                    String errorMessage = "Failed to save profile data";

                    if (error.networkResponse != null) {
                        try {
                            String responseBody = new String(error.networkResponse.data, "utf-8");
                            JSONObject errorResponse = new JSONObject(responseBody);
                            if (errorResponse.has("message")) {
                                errorMessage = errorResponse.getString("message");
                            }
                            Log.e(TAG, "Error response: " + responseBody);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error response", e);
                        }

                        int statusCode = error.networkResponse.statusCode;
                        if (statusCode == 401) {
                            errorMessage = "Unauthorized access. Please login again.";
                        } else if (statusCode == 400) {
                            errorMessage = "Invalid data provided: " + errorMessage;
                        }
                    }

                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
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

    private void toggleEditMode(boolean editMode) {
        isEditMode = editMode;

        if (editMode) {
            tvInfoBio.setVisibility(View.GONE);
            btnEditInfo.setVisibility(View.GONE);
            tvInfoBioEdit.setVisibility(View.VISIBLE);
            btnEditInfoSave.setVisibility(View.VISIBLE);
            tvInfoBioEdit.requestFocus();
        } else {
            tvInfoBio.setVisibility(View.VISIBLE);
            btnEditInfo.setVisibility(View.VISIBLE);
            tvInfoBioEdit.setVisibility(View.GONE);
            btnEditInfoSave.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }


    // 2. Initialize the RecyclerView in your initializeViews() method
//    private void initializeViews(View view) {
//        tvInfoBio = view.findViewById(R.id.tvInfoBio);
//        tvInfoBioEdit = view.findViewById(R.id.tvInfoBioEdit);
//        btnEditInfo = view.findViewById(R.id.btnEditInfo);
//        btnEditInfoSave = view.findViewById(R.id.btnEditInfoSave);
//        btnAddEducation = view.findViewById(R.id.btnAddEducation);
//
//        // Add this line
//        educationRecyclerView = view.findViewById(R.id.education_edit_recyclerView);
//        setupEducationRecyclerView();
//    }

    // 3. Setup RecyclerView method
    private void setupEducationRecyclerView() {
        educationAdapter = new EducationAdapterEdit(requireContext());
        educationRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        educationRecyclerView.setAdapter(educationAdapter);

        // Set click listeners
        educationAdapter.setOnEducationItemClickListener(new EducationAdapterEdit.OnEducationItemClickListener() {
            @Override
            public void onEditClick(Education education, int position) {
                // Handle edit button click
                openEditEducationBottomSheet(education, position);
            }

            @Override
            public void onItemClick(Education education, int position) {
                // Handle item click (optional)
                // You can show details or do nothing
            }
        });

        // Load education data
        loadEducationData();
    }

    // 4. Method to load education data from API
    private void loadEducationData() {
        String accessToken = prefs.getString("access_token", "");

        if (accessToken.isEmpty()) {
            Toast.makeText(getContext(), "No access token found", Toast.LENGTH_SHORT).show();
            return;
        }

        String educationUrl = "https://dormitorybackend.duckdns.org/api/auth/profile/education";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                educationUrl,
                null,
                response -> {
                    try {
                        List<Education> educationList = new ArrayList<>();

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject educationJson = response.getJSONObject(i);
                            Education education = parseEducationFromJson(educationJson);
                            educationList.add(education);
                        }

                        // Update adapter
                        educationAdapter.updateEducationList(educationList);

                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing education data", e);
                        Toast.makeText(getContext(), "Error loading education data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Error loading education data", error);
                    String errorMessage = "Failed to load education data";

                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        if (statusCode == 401) {
                            errorMessage = "Unauthorized access. Please login again.";
                        } else if (statusCode == 404) {
                            errorMessage = "No education data found";
                        }
                    }

                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
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

    // 5. Helper method to parse Education object from JSON
    private Education parseEducationFromJson(JSONObject jsonObject) throws JSONException {
        Education education = new Education();

        education.setId(jsonObject.optInt("id", 0));
        education.setMajor(jsonObject.optString("major", ""));
        education.setDegree(jsonObject.optString("degree", ""));
        education.setDegree_display(jsonObject.optString("degree_display", ""));
        education.setSeries(jsonObject.optString("series", ""));
        education.setStart_date(jsonObject.optString("start_date", ""));
        education.setEnd_date(jsonObject.optString("end_date", ""));
        education.setIs_current(jsonObject.optBoolean("is_current", false));
        education.setDescription(jsonObject.optString("description", ""));

        // Parse institution
        JSONObject institutionJson = jsonObject.optJSONObject("institution");
        if (institutionJson != null) {
            Education.Institution institution = new Education.Institution();
            institution.setId(institutionJson.optInt("id", 0));
            institution.setName(institutionJson.optString("name", ""));
            institution.setLocation(institutionJson.optString("location", ""));
            institution.setWebsite(institutionJson.optString("website", ""));
            institution.setStudents(institutionJson.optString("students", ""));
            education.setInstitution(institution);
        }

        return education;
    }

    // 6. Method to handle edit education (optional)
    // Updated method in AboutBottomSheet.java
    private void openEditEducationBottomSheet(Education education, int position) {
        dismiss(); // Dismiss current bottom sheet

        EducationBottomSheetEdit editEducationBottomSheetEdit = new EducationBottomSheetEdit();

        // Pass education data to the bottom sheet for editing
        Bundle args = new Bundle();
        args.putInt("education_id", education.getId());
        args.putInt("position", position);
        editEducationBottomSheetEdit.setArguments(args);

        editEducationBottomSheetEdit.setOnEducationActionListener(new EducationBottomSheetEdit.OnEducationActionListener() {
            @Override
            public void onEducationSavedEdit() {
                // Reload education data and show this bottom sheet again
                loadEducationData();
//                show(getParentFragmentManager(), "AboutBottomSheet");
            }

            @Override
            public void onEducationCancelledEdit() {
                // Show this bottom sheet again when cancelled
//                show(getParentFragmentManager(), "AboutBottomSheet");
            }

            @Override
            public void onEducationDeleteEdit() {
                // Reload education data and show this bottom sheet again
                loadEducationData();
//                show(getParentFragmentManager(), "AboutBottomSheet");
            }
        });

        editEducationBottomSheetEdit.show(getParentFragmentManager(), "EditEducationBottomSheetEdit");
    }
}