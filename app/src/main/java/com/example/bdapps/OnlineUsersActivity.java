package com.example.bdapps;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class OnlineUsersActivity extends AppCompatActivity {

    private static final String TAG = "OnlineUsersActivity";
    private static final String API_URL = "https://dormitorybackend.duckdns.org/api/productivity/live-users/";
    private static final int REFRESH_INTERVAL = 30000; // 30 seconds
    private static final int TIMER_INTERVAL = 1000; // 1 second

    private RecyclerView recyclerViewUsers;
    private OnlineUsersAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvOnlineCount;

    private List<OnlineUser> userList;
    private RequestQueue requestQueue;
    private Handler refreshHandler;
    private Handler timerHandler;
    private SharedPreferences prefs;

    // Date formatters for parsing server timestamps
    private SimpleDateFormat serverDateFormat;
    private SimpleDateFormat isoDateFormat;

    private Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            fetchOnlineUsers();
            refreshHandler.postDelayed(this, REFRESH_INTERVAL);
        }
    };

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            updateTimers();
            timerHandler.postDelayed(this, TIMER_INTERVAL);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.online_users_layout);

        // Initialize SharedPreferences
        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        // Initialize date formatters
        initDateFormatters();

        initViews();
        initData();
        setupRecyclerView();
        setupSwipeRefresh();

        // Start fetching data and updating timers
        fetchOnlineUsers();
        startPeriodicRefresh();
        startTimerUpdates();
    }

    private void initDateFormatters() {
        // Common server timestamp formats
        serverDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        serverDateFormat.setTimeZone(TimeZone.getDefault());

        isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        isoDateFormat.setTimeZone(TimeZone.getDefault());
    }

    private void initViews() {
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        tvOnlineCount = findViewById(R.id.tvOnlineCount);
    }

    private void initData() {
        userList = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(this);
        refreshHandler = new Handler(Looper.getMainLooper());
        timerHandler = new Handler(Looper.getMainLooper());
    }

    private void setupRecyclerView() {
        adapter = new OnlineUsersAdapter(userList);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUsers.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchOnlineUsers();
        });
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
    }

    private void fetchOnlineUsers() {
        // Retrieve access token
        String accessToken = prefs.getString("access_token", null);
        if (accessToken == null) {
            showError("Please log in to view online users");
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                API_URL,
                null,
                response -> {
                    try {
                        parseUsersResponse(response);
                        swipeRefreshLayout.setRefreshing(false);
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error", e);
                        showError("Error parsing data");
                        swipeRefreshLayout.setRefreshing(false);
                    }
                },
                error -> {
                    Log.e(TAG, "Network error", error);
                    if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                        showError("Authentication failed. Please log in again.");
                        // Optionally, clear the access token and redirect to login
                        prefs.edit().remove("access_token").apply();
                        // Redirect to login activity if needed
                        // Intent intent = new Intent(this, LoginActivity.class);
                        // startActivity(intent);
                    } else {
                        showError("Network error occurred");
                    }
                    swipeRefreshLayout.setRefreshing(false);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                return headers;
            }
        };

        request.setTag(TAG);
        requestQueue.add(request);
    }

    private void parseUsersResponse(JSONArray response) throws JSONException {
        List<OnlineUser> newUserList = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        for (int i = 0; i < response.length(); i++) {
            JSONObject userObj = response.getJSONObject(i);

            String username = userObj.optString("user", "Unknown");
            String domain = userObj.optString("domain", "Unknown");
            String goal = userObj.optString("goal", "No goal set");
            String totalStudiedTimeToday = userObj.optString("total_studied_time_today", "0");

            // Check if user already exists to preserve their session continuity
            OnlineUser existingUser = findExistingUser(username);
            OnlineUser user;

            if (existingUser != null) {
                // Update existing user data but preserve the session start time
                user = new OnlineUser(username, domain, goal, totalStudiedTimeToday, existingUser.getOnlineStartTime());
                // If the backend data has changed, update the base elapsed time
                user.setBaseElapsedTime(user.getBaseElapsedTime());
            } else {
                // New user - create with current time as session start
                user = new OnlineUser(username, domain, goal, totalStudiedTimeToday, currentTime);
            }

            newUserList.add(user);
        }

        userList.clear();
        userList.addAll(newUserList);

        runOnUiThread(() -> {
            adapter.notifyDataSetChanged();
            updateOnlineCount();
        });
    }

    private long parseOnlineStartTime(JSONObject userObj, long fallbackTime) {
        // Try different possible field names for online start time
        String[] possibleFields = {
                "online_since",
                "online_start_time",
                "session_start",
                "start_time",
                "created_at",
                "last_seen"
        };

        for (String field : possibleFields) {
            if (userObj.has(field)) {
                String timeString = userObj.optString(field, null);
                if (timeString != null && !timeString.isEmpty() && !timeString.equals("null")) {
                    long parsedTime = parseTimestamp(timeString);
                    if (parsedTime > 0) {
                        return parsedTime;
                    }
                }
            }
        }

        // If no timestamp found in server response, check if we have existing user data
        return fallbackTime;
    }

    private long parseTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return 0;
        }

        try {
            // Try parsing as milliseconds first
            if (timestamp.matches("\\d+")) {
                long millis = Long.parseLong(timestamp);
                // If it's in seconds, convert to milliseconds
                if (millis < 10000000000L) { // Less than year 2001 in milliseconds
                    millis *= 1000;
                }
                return millis;
            }

            // Try ISO format with 'T'
            if (timestamp.contains("T")) {
                // Handle various ISO formats
                String cleanTimestamp = timestamp.replace("Z", "").split("\\.")[0];
                Date date = isoDateFormat.parse(cleanTimestamp);
                return date.getTime();
            }

            // Try standard format
            Date date = serverDateFormat.parse(timestamp);
            return date.getTime();

        } catch (ParseException | NumberFormatException e) {
            Log.w(TAG, "Failed to parse timestamp: " + timestamp, e);
            return 0;
        }
    }

    private OnlineUser findExistingUser(String username) {
        for (OnlineUser user : userList) {
            if (user.getUser().equals(username)) {
                return user;
            }
        }
        return null;
    }

    private void updateOnlineCount() {
        int count = userList.size();
        String countText = count + (count == 1 ? " user online" : " users online");
        tvOnlineCount.setText(countText);
    }

    private void updateTimers() {
        // Timer updates are handled automatically in the OnlineUser class
        runOnUiThread(() -> adapter.notifyDataSetChanged());
    }

    private void startPeriodicRefresh() {
        refreshHandler.post(refreshRunnable);
    }

    private void startTimerUpdates() {
        timerHandler.post(timerRunnable);
    }

    private void showError(String message) {
        runOnUiThread(() ->
                Toast.makeText(OnlineUsersActivity.this, message, Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (refreshHandler != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
        if (timerHandler != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (refreshHandler != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
        if (timerHandler != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchOnlineUsers();
        startPeriodicRefresh();
        startTimerUpdates();
    }
}