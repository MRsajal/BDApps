package com.example.bdapps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    EditText searchEditText;
    Button searchActionButton;
    RecyclerView searchResultsRecyclerView;
    ProgressBar progressBar;
    TextView noResultsText;

    List<User> searchResults;
    private SearchResultAdapter adapter;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initViews();
        setupRecyclerView();
        setupSearch();

        requestQueue = Volley.newRequestQueue(this);
    }
    private void initViews() {
        searchEditText = findViewById(R.id.searchEditText);
        searchActionButton = findViewById(R.id.searchActionButton);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        noResultsText = findViewById(R.id.noResultsText);
    }

    private void setupRecyclerView() {
        searchResults = new ArrayList<>();
        adapter = new SearchResultAdapter(searchResults, this::onUserClick);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchResultsRecyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        searchActionButton.setOnClickListener(v -> performSearch());

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });
    }

    private void performSearch() {
        String username = searchEditText.getText().toString().trim();
        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
            return;
        }

        searchUser(username);
    }

    private void searchUser(String username) {
        showLoading(true);

        String url = "https://dormitorybackend.duckdns.org/api/auth/user/" + username;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    showLoading(false);
                    try {
                        User user = parseUserFromJson(response);
                        searchResults.clear();
                        searchResults.add(user);
                        adapter.notifyDataSetChanged();
                        showNoResults(false);
                    } catch (Exception e) {
                        showError("Error parsing user data");
                    }
                },
                error -> {
                    showLoading(false);
                    if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                        showNoResults(true);
                    } else {
                        showError("Network error occurred");
                    }
                }
        );

        requestQueue.add(request);
    }

    private User parseUserFromJson(JSONObject json) throws JSONException {
        User user = new User();
        user.setUserId(json.getString("id"));
        user.setUsername(json.getString("username"));
        user.setEmail(json.getString("email"));

        if (json.has("profile")) {
            JSONObject profile = json.getJSONObject("profile");
            UserProfile userProfile = new UserProfile();
            userProfile.setName(profile.optString("name", ""));
            userProfile.setProfilePic(profile.optString("profile_pic", ""));
            userProfile.setFollowersCount(profile.optString("followers_count", "0"));
            userProfile.setFollowingCount(profile.optString("following_count", "0"));
            userProfile.setIsFollowing(profile.optString("is_following", "false"));
            userProfile.setAddress(profile.optString("address", ""));
            userProfile.setBio(profile.optString("bio", ""));
            userProfile.setPersonalWebsite(profile.optString("personal_website", ""));
            userProfile.setAboutMe(profile.optString("about_me", ""));
            userProfile.setGender(profile.optString("gender", ""));
            userProfile.setGenderDisplay(profile.optString("gender_display", ""));

            user.setProfile(userProfile);
        }

        return user;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        searchResultsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showNoResults(boolean show) {
        noResultsText.setVisibility(show ? View.VISIBLE : View.GONE);
        searchResultsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        if (show) {
            searchResults.clear();
            adapter.notifyDataSetChanged();
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        showNoResults(true);
    }

    private void onUserClick(User user) {
        Intent intent = new Intent(this, UserProfileActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
    }
}