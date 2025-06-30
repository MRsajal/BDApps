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
import com.google.android.material.textfield.TextInputEditText;

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

    // Name related views
    private TextView tvUserName;
    private EditText usersNameProfile;
    private ImageButton btnEditName;
    private ImageButton btnEditNameSave;

    // Bio related views
    private TextView tvBio;
    private EditText userBioProfile;
    private ImageButton btnEditBio;
    private ImageButton btnEditBioSave;

    // Location related views
    private TextView tvEditLocation;
    private EditText userLocationEdit;
    private ImageButton btnEditLocation;
    private ImageButton btnEditLocationSave;

    // Info related views
    private TextView tvInfoBio;
    private EditText tvInfoBioEdit;
    private ImageButton btnEditInfo;
    private ImageButton btnEditInfoSave;
    private ImageButton btnAddEducation;

    private RequestQueue requestQueue;
    private SharedPreferences prefs;

    private boolean isEditModeInfo = false;
    private boolean isEditModeName = false;
    private boolean isEditModeBio = false;
    private boolean isEditModeLocation = false;
    private String originalAboutMe = "";
    private String originalName = "";
    private String originalBio = "";
    private String originalLocation = "";

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
        loadProfileData();
    }

    private void initializeViews(View view) {
        // Name views
        tvUserName = view.findViewById(R.id.tvUserName);
        usersNameProfile = view.findViewById(R.id.users_name_profile);
        btnEditName = view.findViewById(R.id.btnEditName);
        btnEditNameSave = view.findViewById(R.id.btnEditNameSave);

        // Bio views
        tvBio = view.findViewById(R.id.tvBio);
        userBioProfile = view.findViewById(R.id.user_bio_profile);
        btnEditBio = view.findViewById(R.id.btnEditBio);
        btnEditBioSave = view.findViewById(R.id.btnEditBioSave);

        // Location views
        tvEditLocation = view.findViewById(R.id.tvEditLocation);
        userLocationEdit = view.findViewById(R.id.user_location_edit);
        btnEditLocation = view.findViewById(R.id.btnEditLocation);
        btnEditLocationSave = view.findViewById(R.id.btnEditLocationSave);

        // Info views
        tvInfoBio = view.findViewById(R.id.tvInfoBio);
        tvInfoBioEdit = view.findViewById(R.id.tvInfoBioEdit);
        btnEditInfo = view.findViewById(R.id.btnEditInfo);
        btnEditInfoSave = view.findViewById(R.id.btnEditInfoSave);
        btnAddEducation = view.findViewById(R.id.btnAddEducation);

        // Education RecyclerView
        educationRecyclerView = view.findViewById(R.id.education_edit_recyclerView);
        setupEducationRecyclerView();
    }

    private void setupClickListeners() {
        // Name edit listeners
        btnEditName.setOnClickListener(v -> toggleNameEditMode(true));
        btnEditNameSave.setOnClickListener(v -> saveNameData());

        // Bio edit listeners
        btnEditBio.setOnClickListener(v -> toggleBioEditMode(true));
        btnEditBioSave.setOnClickListener(v -> saveBioData());

        // Location edit listeners
        btnEditLocation.setOnClickListener(v -> toggleLocationEditMode(true));
        btnEditLocationSave.setOnClickListener(v -> saveLocationData());

        // Info edit listeners
        btnEditInfo.setOnClickListener(v -> toggleInfoEditMode(true));
        btnEditInfoSave.setOnClickListener(v -> saveInfoData());

        // Education listener
        btnAddEducation.setOnClickListener(v -> navigateToAddEducation());
    }

    private void toggleNameEditMode(boolean editMode) {
        isEditModeName = editMode;

        if (editMode) {
            tvUserName.setVisibility(View.GONE);
            btnEditName.setVisibility(View.GONE);
            usersNameProfile.setVisibility(View.VISIBLE);
            btnEditNameSave.setVisibility(View.VISIBLE);
            usersNameProfile.setText(originalName);
            usersNameProfile.requestFocus();
        } else {
            tvUserName.setVisibility(View.VISIBLE);
            btnEditName.setVisibility(View.VISIBLE);
            usersNameProfile.setVisibility(View.GONE);
            btnEditNameSave.setVisibility(View.GONE);
        }
    }

    private void toggleBioEditMode(boolean editMode) {
        isEditModeBio = editMode;

        if (editMode) {
            tvBio.setVisibility(View.GONE);
            btnEditBio.setVisibility(View.GONE);
            userBioProfile.setVisibility(View.VISIBLE);
            btnEditBioSave.setVisibility(View.VISIBLE);
            userBioProfile.setText(originalBio);
            userBioProfile.requestFocus();
        } else {
            tvBio.setVisibility(View.VISIBLE);
            btnEditBio.setVisibility(View.VISIBLE);
            userBioProfile.setVisibility(View.GONE);
            btnEditBioSave.setVisibility(View.GONE);
        }
    }

    private void toggleLocationEditMode(boolean editMode) {
        isEditModeLocation = editMode;

        if (editMode) {
            tvEditLocation.setVisibility(View.GONE);
            btnEditLocation.setVisibility(View.GONE);
            userLocationEdit.setVisibility(View.VISIBLE);
            btnEditLocationSave.setVisibility(View.VISIBLE);
            userLocationEdit.setText(originalLocation);
            userLocationEdit.requestFocus();
        } else {
            tvEditLocation.setVisibility(View.VISIBLE);
            btnEditLocation.setVisibility(View.VISIBLE);
            userLocationEdit.setVisibility(View.GONE);
            btnEditLocationSave.setVisibility(View.GONE);
        }
    }

    private void toggleInfoEditMode(boolean editMode) {
        isEditModeInfo = editMode;

        if (editMode) {
            tvInfoBio.setVisibility(View.GONE);
            btnEditInfo.setVisibility(View.GONE);
            tvInfoBioEdit.setVisibility(View.VISIBLE);
            btnEditInfoSave.setVisibility(View.VISIBLE);
            tvInfoBioEdit.setText(originalAboutMe);
            tvInfoBioEdit.requestFocus();
        } else {
            tvInfoBio.setVisibility(View.VISIBLE);
            btnEditInfo.setVisibility(View.VISIBLE);
            tvInfoBioEdit.setVisibility(View.GONE);
            btnEditInfoSave.setVisibility(View.GONE);
        }
    }

    private void saveNameData() {
        String accessToken = prefs.getString("access_token", "");
        String newName = usersNameProfile.getText().toString().trim();

        if (accessToken.isEmpty()) {
            Toast.makeText(getContext(), "No access token found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newName.isEmpty()) {
            Toast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject userData = new JSONObject();
        try {
            userData.put("name", newName);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON body", e);
            Toast.makeText(getContext(), "Error preparing data", Toast.LENGTH_SHORT).show();
            return;
        }

        updateProfile(userData, "name", newName, () -> {
            tvUserName.setText(newName);
            originalName = newName;
            toggleNameEditMode(false);
            // Update SharedPreferences
            prefs.edit().putString("user_name", newName).apply();
        });
    }

    private void saveBioData() {
        String accessToken = prefs.getString("access_token", "");
        String newBio = userBioProfile.getText().toString().trim();

        if (accessToken.isEmpty()) {
            Toast.makeText(getContext(), "No access token found", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject userData = new JSONObject();
        try {
            userData.put("bio", newBio);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON body", e);
            Toast.makeText(getContext(), "Error preparing data", Toast.LENGTH_SHORT).show();
            return;
        }

        updateProfile(userData, "bio", newBio, () -> {
            tvBio.setText(newBio.isEmpty() ? "No bio available" : newBio);
            originalBio = newBio;
            toggleBioEditMode(false);
            // Update SharedPreferences
            prefs.edit().putString("bio", newBio).apply();
        });
    }

    private void saveLocationData() {
        String accessToken = prefs.getString("access_token", "");
        String newLocation = userLocationEdit.getText().toString().trim();

        if (accessToken.isEmpty()) {
            Toast.makeText(getContext(), "No access token found", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject userData = new JSONObject();
        try {
            userData.put("address", newLocation);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON body", e);
            Toast.makeText(getContext(), "Error preparing data", Toast.LENGTH_SHORT).show();
            return;
        }

        updateProfile(userData, "address", newLocation, () -> {
            tvEditLocation.setText(newLocation.isEmpty() ? "No location available" : newLocation);
            originalLocation = newLocation;
            toggleLocationEditMode(false);
            // Update SharedPreferences
            prefs.edit().putString("address", newLocation).apply();
        });
    }

    private void saveInfoData() {
        String accessToken = prefs.getString("access_token", "");
        String newAboutMe = tvInfoBioEdit.getText().toString().trim();

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

        updateProfile(userData, "about_me", newAboutMe, () -> {
            tvInfoBio.setText(newAboutMe.isEmpty() ? "No information available" : newAboutMe);
            originalAboutMe = newAboutMe;
            toggleInfoEditMode(false);
            // Update SharedPreferences
            prefs.edit().putString("about_me", newAboutMe).apply();
        });
    }

    private void updateProfile(JSONObject userData, String fieldName, String newValue, Runnable onSuccess) {
        String accessToken = prefs.getString("access_token", "");

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PATCH,
                API_BASE_URL,
                userData,
                response -> {
                    try {
                        String updatedValue = response.optString(fieldName, "");
                        onSuccess.run();
                        Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing save response", e);
                        Toast.makeText(getContext(), "Update completed but error processing response", Toast.LENGTH_SHORT).show();
                        onSuccess.run();
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

                        // Store original values
                        originalName = name;
                        originalBio = bio;
                        originalLocation = address;
                        originalAboutMe = aboutMe;

                        // Update UI
                        tvUserName.setText(name.isEmpty() ? "No name available" : name);
                        tvBio.setText(bio.isEmpty() ? "No bio available" : bio);
                        tvEditLocation.setText(address.isEmpty() ? "No location available" : address);
                        tvInfoBio.setText(aboutMe.isEmpty() ? "No information available" : aboutMe);

                        // Set edit text values
                        usersNameProfile.setText(name);
                        userBioProfile.setText(bio);
                        userLocationEdit.setText(address);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }

    // Education RecyclerView setup and methods (keeping existing code)
    private void setupEducationRecyclerView() {
        educationAdapter = new EducationAdapterEdit(requireContext());
        educationRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        educationRecyclerView.setAdapter(educationAdapter);

        educationAdapter.setOnEducationItemClickListener(new EducationAdapterEdit.OnEducationItemClickListener() {
            @Override
            public void onEditClick(Education education, int position) {
                openEditEducationBottomSheet(education, position);
            }

            @Override
            public void onItemClick(Education education, int position) {
                // Handle item click (optional)
            }
        });

        loadEducationData();
    }

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

    private void openEditEducationBottomSheet(Education education, int position) {
        dismiss();

        EducationBottomSheetEdit editEducationBottomSheetEdit = new EducationBottomSheetEdit();

        Bundle args = new Bundle();
        args.putInt("education_id", education.getId());
        args.putInt("position", position);
        editEducationBottomSheetEdit.setArguments(args);

        editEducationBottomSheetEdit.setOnEducationActionListener(new EducationBottomSheetEdit.OnEducationActionListener() {
            @Override
            public void onEducationSavedEdit() {
                loadEducationData();
            }

            @Override
            public void onEducationCancelledEdit() {
                // Handle cancellation if needed
            }

            @Override
            public void onEducationDeleteEdit() {
                loadEducationData();
            }
        });

        editEducationBottomSheetEdit.show(getParentFragmentManager(), "EditEducationBottomSheetEdit");
    }
}