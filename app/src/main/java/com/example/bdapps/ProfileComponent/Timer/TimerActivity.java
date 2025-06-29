package com.example.bdapps.ProfileComponent.Timer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bdapps.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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

public class TimerActivity extends AppCompatActivity {

    // UI Components
    private TextInputLayout tilGoalName;
    private TextInputEditText etGoalName;
    private TextView tvCurrentGoal;
    private TextView tvTimer;
    private TextView tvStatus;
    private MaterialButton btnStart;
    private MaterialButton btnStop;
    private MaterialButton btnReset;
    private MaterialCardView cardGoalInput;

    // Timer variables
    private Handler timerHandler;
    private Runnable timerRunnable;
    private long startTime = 0;
    private long elapsedTime = 0;
    private boolean isTimerRunning = false;
    private String currentGoalName = "";

    // Network variables
    private OkHttpClient client;
    private static final String BASE_URL = "https://dormitorybackend.duckdns.org/api/productivity/tracked-times/";
    private static final String START_TIMER_URL = BASE_URL + "start-timer/";
    private static final String END_TIMER_URL = BASE_URL + "end-timer/";
    private static final String ACTIVE_TIMER_STATUS_URL = BASE_URL + "active-timer-status/";
    private static final String TAG = "TimerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timer_activity_layout);

        initializeViews();
        setupClickListeners();
        initializeTimer();
        initializeNetwork();

        // Check for active timer when activity starts
        checkActiveTimer();
    }

    private void initializeViews() {
        tilGoalName = findViewById(R.id.tilGoalName);
        etGoalName = findViewById(R.id.etGoalName);
        tvCurrentGoal = findViewById(R.id.tvCurrentGoal);
        tvTimer = findViewById(R.id.tvTimer);
        tvStatus = findViewById(R.id.tvStatus);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnReset = findViewById(R.id.btnReset);
        cardGoalInput = findViewById(R.id.cardGoalInput);
    }

    private void setupClickListeners() {
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimer();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTimer();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });
    }

    private void initializeTimer() {
        timerHandler = new Handler(Looper.getMainLooper());

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isTimerRunning) {
                    elapsedTime = System.currentTimeMillis() - startTime;
                    updateTimerDisplay();
                    timerHandler.postDelayed(this, 1000); // Update every second
                }
            }
        };
    }

    private void initializeNetwork() {
        client = new OkHttpClient();
    }

    private String getAccessToken() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return prefs.getString("access_token", "");
    }

    private void checkActiveTimer() {
        String accessToken = getAccessToken();

        if (TextUtils.isEmpty(accessToken)) {
            Log.w(TAG, "No access token found, skipping active timer check");
            return;
        }

        Request request = new Request.Builder()
                .url(ACTIVE_TIMER_STATUS_URL)
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to check active timer status", e);
                runOnUiThread(() -> {
                    // Silently fail - user can still start new timer
                    tvStatus.setText("Ready to start");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        boolean isActive = jsonResponse.getBoolean("active");

                        if (isActive) {
                            String elapsedTimeStr = jsonResponse.getString("elapsed_time");
                            String goalName = jsonResponse.optString("goal", "Previous Goal");

                            runOnUiThread(() -> {
                                resumeTimerFromBackend(elapsedTimeStr, goalName);
                            });
                        } else {
                            runOnUiThread(() -> {
                                tvStatus.setText("Ready to start");
                            });
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing active timer response", e);
                        runOnUiThread(() -> {
                            tvStatus.setText("Ready to start");
                        });
                    }
                } else {
                    Log.e(TAG, "Active timer check failed: " + response.code());
                    runOnUiThread(() -> {
                        tvStatus.setText("Ready to start");
                    });
                }
                response.close();
            }
        });
    }

    private void resumeTimerFromBackend(String elapsedTimeStr, String goalName) {
        try {
            // Parse elapsed time string (format: "HH:MM:SS.ffffff" or "MM:SS.ffffff")
            long elapsedMillis = parseElapsedTime(elapsedTimeStr);

            // Set the current goal
            currentGoalName = goalName;
            tvCurrentGoal.setText(goalName);

            // Resume timer with elapsed time
            elapsedTime = elapsedMillis;
            startTime = System.currentTimeMillis() - elapsedTime;
            isTimerRunning = true;

            // Start the timer runnable
            timerHandler.post(timerRunnable);

            // Update UI
            updateUIForTimerState();
            tvStatus.setText("Timer resumed - " + goalName);

            Toast.makeText(this, "Timer resumed for: " + goalName, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Error resuming timer", e);
            Toast.makeText(this, "Error resuming timer", Toast.LENGTH_SHORT).show();
        }
    }

    private long parseElapsedTime(String timeStr) {
        // Parse time string like "00:00:25.131101" or "00:25.131101"
        // Remove microseconds part and focus on HH:MM:SS
        String[] parts = timeStr.split("\\.");
        String timePart = parts[0];

        String[] timeParts = timePart.split(":");
        long totalMillis = 0;

        if (timeParts.length == 3) {
            // Format: HH:MM:SS
            int hours = Integer.parseInt(timeParts[0]);
            int minutes = Integer.parseInt(timeParts[1]);
            int seconds = Integer.parseInt(timeParts[2]);

            totalMillis = (hours * 3600 + minutes * 60 + seconds) * 1000L;
        } else if (timeParts.length == 2) {
            // Format: MM:SS
            int minutes = Integer.parseInt(timeParts[0]);
            int seconds = Integer.parseInt(timeParts[1]);

            totalMillis = (minutes * 60 + seconds) * 1000L;
        }

        // Add microseconds if present
        if (parts.length > 1) {
            String microsPart = parts[1];
            if (microsPart.length() >= 3) {
                // Convert first 3 digits of microseconds to milliseconds
                int millis = Integer.parseInt(microsPart.substring(0, 3));
                totalMillis += millis;
            }
        }

        return totalMillis;
    }

    private void startTimer() {
        String goalName = etGoalName.getText().toString().trim();

        if (TextUtils.isEmpty(goalName)) {
            tilGoalName.setError("Please enter a goal name");
            etGoalName.requestFocus();
            return;
        }

        // Clear any previous error
        tilGoalName.setError(null);

        // Send start timer request to backend
        sendStartTimerRequest(goalName);
    }

    private void sendStartTimerRequest(String goalName) {
        String accessToken = getAccessToken();

        if (TextUtils.isEmpty(accessToken)) {
            Toast.makeText(this, "Authentication token not found. Please login again.", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            // Create JSON payload
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("goal", goalName);

            // Create request body
            RequestBody requestBody = RequestBody.create(
                    jsonObject.toString(),
                    MediaType.parse("application/json")
            );

            // Build request
            Request request = new Request.Builder()
                    .url(START_TIMER_URL)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build();

            // Execute request
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Start timer request failed", e);
                    runOnUiThread(() -> {
                        Toast.makeText(TimerActivity.this, "Failed to start timer. Check your connection.", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Start timer request successful");
                        runOnUiThread(() -> {
                            // Start local timer
                            startLocalTimer(goalName);
                        });
                    } else {
                        Log.e(TAG, "Start timer request failed: " + response.code());
                        runOnUiThread(() -> {
                            Toast.makeText(TimerActivity.this, "Failed to start timer. Server error.", Toast.LENGTH_SHORT).show();
                        });
                    }
                    response.close();
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON for start timer", e);
            Toast.makeText(this, "Error preparing request", Toast.LENGTH_SHORT).show();
        }
    }

    private void startLocalTimer(String goalName) {
        // Set the current goal
        currentGoalName = goalName;
        tvCurrentGoal.setText(goalName);

        // Start the timer
        if (!isTimerRunning) {
            startTime = System.currentTimeMillis() - elapsedTime;
            isTimerRunning = true;
            timerHandler.post(timerRunnable);
        }

        // Update UI state
        updateUIForTimerState();
        tvStatus.setText("Timer is running...");

        Toast.makeText(this, "Timer started for: " + goalName, Toast.LENGTH_SHORT).show();
    }

    private void stopTimer() {
        if (isTimerRunning) {
            // Send stop timer request to backend
            sendEndTimerRequest();
        }
    }

    private void sendEndTimerRequest() {
        String accessToken = getAccessToken();

        if (TextUtils.isEmpty(accessToken)) {
            Toast.makeText(this, "Authentication token not found. Please login again.", Toast.LENGTH_LONG).show();
            return;
        }

        // Build request (no body needed for end timer)
        Request request = new Request.Builder()
                .url(END_TIMER_URL)
                .addHeader("Authorization", "Bearer " + accessToken)
                .post(RequestBody.create("", MediaType.parse("application/json")))
                .build();

        // Execute request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "End timer request failed", e);
                runOnUiThread(() -> {
                    Toast.makeText(TimerActivity.this, "Failed to stop timer on server. Timer stopped locally.", Toast.LENGTH_SHORT).show();
                    // Stop local timer anyway
                    stopLocalTimer();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d(TAG, "End timer request successful");
                    runOnUiThread(() -> {
                        // Stop local timer
                        stopLocalTimer();
                    });
                } else {
                    Log.e(TAG, "End timer request failed: " + response.code());
                    runOnUiThread(() -> {
                        Toast.makeText(TimerActivity.this, "Failed to stop timer on server. Timer stopped locally.", Toast.LENGTH_SHORT).show();
                        // Stop local timer anyway
                        stopLocalTimer();
                    });
                }
                response.close();
            }
        });
    }

    private void stopLocalTimer() {
        isTimerRunning = false;
        timerHandler.removeCallbacks(timerRunnable);

        // Update UI state
        updateUIForTimerState();
        tvStatus.setText("Timer stopped");

        String goalName = currentGoalName.isEmpty() ? tvCurrentGoal.getText().toString() : currentGoalName;
        String timeSpent = tvTimer.getText().toString();

        Toast.makeText(this, "Great work! You focused on '" + goalName +
                "' for " + timeSpent, Toast.LENGTH_LONG).show();
    }

    private void resetTimer() {
        // Stop the timer if running
        isTimerRunning = false;
        timerHandler.removeCallbacks(timerRunnable);

        // Reset timer variables
        startTime = 0;
        elapsedTime = 0;
        currentGoalName = "";

        // Reset UI
        tvTimer.setText("00:00:00");
        tvCurrentGoal.setText("Ready to focus");
        tvStatus.setText("Press start to begin");
        etGoalName.setText("");

        // Update UI state
        updateUIForTimerState();

        Toast.makeText(this, "Timer reset", Toast.LENGTH_SHORT).show();
    }

    private void updateTimerDisplay() {
        long totalSeconds = elapsedTime / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        tvTimer.setText(timeString);
    }

    private void updateUIForTimerState() {
        if (isTimerRunning) {
            // Timer is running
            btnStart.setEnabled(false);
            btnStop.setEnabled(true);
            btnReset.setEnabled(false);
            cardGoalInput.setAlpha(0.5f);
            etGoalName.setEnabled(false);
        } else if (elapsedTime > 0) {
            // Timer is stopped but has elapsed time
            btnStart.setEnabled(true);
            btnStop.setEnabled(false);
            btnReset.setEnabled(true);
            cardGoalInput.setAlpha(0.5f);
            etGoalName.setEnabled(false);
        } else {
            // Timer is reset/initial state
            btnStart.setEnabled(true);
            btnStop.setEnabled(false);
            btnReset.setEnabled(false);
            cardGoalInput.setAlpha(1.0f);
            etGoalName.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Timer continues running in background
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check for active timer when returning to the app
        if (!isTimerRunning) {
            checkActiveTimer();
        } else {
            // Update display when app comes back to foreground
            updateTimerDisplay();
        }
    }
}