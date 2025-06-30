package com.example.bdapps.Recommendation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.bdapps.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Recommendation extends AppCompatActivity {
    private static final String API_URL = "https://dormitorybackend.duckdns.org/api/auth/recommendations/peers";
    private static final String TAG = "RecommendedUsers";

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private List<User> userList;
    private RequestQueue requestQueue;
    private String accessToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendation);
        SharedPreferences prefs=getSharedPreferences("MyPrefs",MODE_PRIVATE);
        accessToken=prefs.getString("access_token","");
        initViews();
        setupRecyclerView();
        fetchRecommendedUsers();
    }
    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view_users);
        progressBar = findViewById(R.id.progress_bar);
        emptyTextView = findViewById(R.id.empty_text_view);

        userList = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(this);
    }
    private void setupRecyclerView() {
        userAdapter = new UserAdapter(userList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(userAdapter);
    }
    private void fetchRecommendedUsers() {
        showLoading(true);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                API_URL,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        showLoading(false);
                        parseUsersData(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showLoading(false);
                        showError("Failed to load recommendations: " + error.getMessage());
                        Log.e(TAG, "API Error: ", error);
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                return headers;
            }
        };

        requestQueue.add(jsonArrayRequest);
    }

    private void parseUsersData(JSONArray jsonArray) {
        userList.clear();

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject userObject = jsonArray.getJSONObject(i);
                User user = parseUser(userObject);
                if (user != null) {
                    userList.add(user);
                }
            }

            if (userList.isEmpty()) {
                showEmptyState();
            } else {
                userAdapter.notifyDataSetChanged();
            }

        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error: ", e);
            showError("Error parsing user data");
        }
    }

    private User parseUser(JSONObject userObject) {
        try {
            User user = new User();
            user.setId(userObject.optInt("id"));
            user.setUsername(userObject.optString("username"));
            user.setEmail(userObject.optString("email"));

            JSONObject profileObject = userObject.optJSONObject("profile");
            if (profileObject != null) {
                Profile profile = parseProfile(profileObject);
                user.setProfile(profile);
            }

            return user;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing user: ", e);
            return null;
        }
    }

    private Profile parseProfile(JSONObject profileObject) {
        Profile profile = new Profile();
        profile.setName(profileObject.optString("name"));
        profile.setProfilePic(profileObject.optString("profile_pic"));
        profile.setFollowersCount(profileObject.optString("followers_count"));
        profile.setFollowingCount(profileObject.optString("following_count"));
        profile.setBio(profileObject.optString("bio"));
        profile.setAddress(profileObject.optString("address"));
        profile.setPersonalWebsite(profileObject.optString("personal_website"));
        profile.setGender(profileObject.optString("gender"));

        // Parse work experiences
        JSONArray workExperiences = profileObject.optJSONArray("work_experiences");
        if (workExperiences != null && workExperiences.length() > 0) {
            try {
                JSONObject firstWork = workExperiences.getJSONObject(0);
                profile.setCurrentJob(firstWork.optString("title"));
                JSONObject organization = firstWork.optJSONObject("organization");
                if (organization != null) {
                    profile.setCurrentCompany(organization.optString("name"));
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing work experience: ", e);
            }
        }

        // Parse education
        JSONArray educations = profileObject.optJSONArray("educations");
        if (educations != null && educations.length() > 0) {
            try {
                JSONObject firstEducation = educations.getJSONObject(0);
                JSONObject institution = firstEducation.optJSONObject("institution");
                if (institution != null) {
                    profile.setEducation(institution.optString("name"));
                }
                profile.setDegree(firstEducation.optString("degree"));
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing education: ", e);
            }
        }

        return profile;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        emptyTextView.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.VISIBLE);
        emptyTextView.setText("No recommended users found");
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        showEmptyState();
        emptyTextView.setText("Error loading recommendations\nTap to retry");
        emptyTextView.setOnClickListener(v -> fetchRecommendedUsers());
    }
}