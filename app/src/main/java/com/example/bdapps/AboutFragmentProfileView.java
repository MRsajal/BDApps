package com.example.bdapps;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.bdapps.ProfileComponent.Education.Education;
import com.example.bdapps.ProfileComponent.Education.EducationAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AboutFragmentProfileView extends Fragment implements EducationAdapter.OnEducationItemClickListener {
    private Button editButton;
    private TextView aboutMeInfoTextView;
    private RecyclerView educationRecyclerView;
    private EducationAdapter educationAdapter;
    private RequestQueue requestQueue;

    private static final String TAG = "AboutFragmentProfile";
    private static final String API_URL = "https://dormitorybackend.duckdns.org/api/auth/profile";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_about_profile_view, container, false);
        int position = getArguments() != null ? getArguments().getInt("position") : -1;
        view.setTag("f" + position);

        // Initialize views
        initViews(view);

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(requireContext());

        // Setup RecyclerView
        setupRecyclerView();

        // Setup click listeners
        setupClickListeners();

        // Fetch profile data when fragment is created
        fetchProfileData();

        return view;
    }
    private void initViews(View view) {
        editButton = view.findViewById(R.id.editButton_about_profile);
        aboutMeInfoTextView = view.findViewById(R.id.about_me_info_profile);
        educationRecyclerView = view.findViewById(R.id.education_profile_recyclerView);
    }

    private void setupRecyclerView() {
        educationAdapter = new EducationAdapter();
        educationAdapter.setOnEducationItemClickListener(this);

        educationRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        educationRecyclerView.setAdapter(educationAdapter);

        // Optional: Add item decoration for spacing
        // educationRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    private void setupClickListeners() {
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AboutBottomSheet bottomSheet = new AboutBottomSheet();
                bottomSheet.show(getParentFragmentManager(), "AboutEditBottomSheet");
            }
        });
    }

    private void fetchProfileData() {
        // Get access token from SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("access_token", null);

        if (accessToken == null) {
            Log.e(TAG, "No access token found");
            Toast.makeText(getContext(), "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create JSON request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                API_URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "Profile response: " + response.toString());

                            // Extract about_me from response
                            parseAboutMeData(response);

                            // Extract education data from response
                            parseEducationData(response);

                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error: " + e.getMessage());
                            aboutMeInfoTextView.setText("Error loading information");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "API request failed: " + error.getMessage());
                        if (error.networkResponse != null) {
                            Log.e(TAG, "Status code: " + error.networkResponse.statusCode);
                            if (error.networkResponse.statusCode == 401) {
                                Toast.makeText(getContext(), "Session expired. Please login again", Toast.LENGTH_SHORT).show();
                            }
                        }
                        aboutMeInfoTextView.setText("Failed to load information");
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // Add request to queue
        requestQueue.add(jsonObjectRequest);
    }

    private void parseAboutMeData(JSONObject response) throws JSONException {
        if (response.has("about_me")) {
            String aboutMe = response.getString("about_me");

            // Set the about_me text to TextView
            if (aboutMe != null && !aboutMe.isEmpty() && !aboutMe.equals("null")) {
                aboutMeInfoTextView.setText(aboutMe);
            } else {
                aboutMeInfoTextView.setText("No information available");
            }
        } else {
            aboutMeInfoTextView.setText("No information available");
            Log.w(TAG, "about_me field not found in response");
        }
    }

    private void parseEducationData(JSONObject response) throws JSONException {
        if (response.has("educations")) {
            JSONArray educationsArray = response.getJSONArray("educations");
            List<Education> educationList = new ArrayList<>();

            for (int i = 0; i < educationsArray.length(); i++) {
                JSONObject educationJson = educationsArray.getJSONObject(i);
                Education education = parseEducationObject(educationJson);
                if (education != null) {
                    educationList.add(education);
                }
            }

            // Update adapter with new data
            educationAdapter.updateEducationList(educationList);

            Log.d(TAG, "Loaded " + educationList.size() + " education records");
        } else {
            Log.w(TAG, "educations field not found in response");
        }
    }

    private Education parseEducationObject(JSONObject educationJson) {
        try {
            Education education = new Education();

            // Parse basic education fields
            education.setId(educationJson.optInt("id", 0));
            education.setMajor(educationJson.optString("major", ""));
            education.setDegree(educationJson.optString("degree", ""));
            education.setDegree_display(educationJson.optString("degree_display", ""));
            education.setSeries(educationJson.optString("series", ""));
            education.setStart_date(educationJson.optString("start_date", ""));
            education.setEnd_date(educationJson.optString("end_date", ""));
            education.setIs_current(educationJson.optBoolean("is_current", false));
            education.setDescription(educationJson.optString("description", ""));

            // Parse institution object
            if (educationJson.has("institution")) {
                JSONObject institutionJson = educationJson.getJSONObject("institution");
                Education.Institution institution = new Education.Institution();

                institution.setId(institutionJson.optInt("id", 0));
                institution.setName(institutionJson.optString("name", ""));
                institution.setLocation(institutionJson.optString("location", ""));
                institution.setWebsite(institutionJson.optString("website", ""));
                institution.setStudents(institutionJson.optString("students", ""));
                education.setInstitution(institution);
            }

            return education;

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing education object: " + e.getMessage());
            return null;
        }
    }

    // Implementation of EducationAdapter.OnEducationItemClickListener
    @Override
    public void onEditClick(Education education, int position) {
        // Handle edit click - you can implement this based on your needs
        Toast.makeText(getContext(), "Edit education: " + education.getInstitution().getName(), Toast.LENGTH_SHORT).show();

        // You might want to open an edit dialog or navigate to an edit screen
        // Example:
        // EducationEditBottomSheet editSheet = new EducationEditBottomSheet();
        // Bundle args = new Bundle();
        // args.putInt("education_id", education.getId());
        // args.putInt("position", position);
        // editSheet.setArguments(args);
        // editSheet.show(getParentFragmentManager(), "EducationEditBottomSheet");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }
}