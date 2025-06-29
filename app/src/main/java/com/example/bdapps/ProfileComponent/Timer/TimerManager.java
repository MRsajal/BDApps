package com.example.bdapps.ProfileComponent.Timer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TimerManager {
    private static final String TAG = "TimerManager";
    private static final String API_URL = "https://dormitorybackend.duckdns.org/api/productivity/tracked-times/active-timer-status/";
    private static final String PREFS_NAME = "MyPrefs";
    private static final String ACCESS_TOKEN_KEY = "access_token";

    private Handler mainHandler;
    private ExecutorService executor;
    private Runnable timerRunnable;
    private boolean isRunning = false;
    private long startTime = 0;
    private long elapsedTime = 0;
    private Context context;

    private String goalName;

    public interface TimerCallback {
        void onTimerUpdate(String formattedTime);
        void onTimerStatusChanged(boolean isActive);
        void onError(String error);
    }

    private TimerCallback callback;

    public TimerManager(Context context, TimerCallback callback) {
        this.context = context;
        this.callback = callback;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void checkTimerStatus() {
        executor.execute(() -> {
            try {
                // Get access token from SharedPreferences
                SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                String accessToken = prefs.getString(ACCESS_TOKEN_KEY, "");

                if (accessToken.isEmpty()) {
                    mainHandler.post(() -> callback.onError("No access token found. Please login again."));
                    return;
                }

                URL url = new URL(API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                // Add Authorization header with Bearer token
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);
                connection.setRequestProperty("Content-Type", "application/json");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    parseTimerResponse(response.toString());
                } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    mainHandler.post(() -> callback.onError("Authentication failed. Please login again."));
                } else {
                    mainHandler.post(() -> callback.onError("Failed to fetch timer status: " + responseCode));
                }

                connection.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error checking timer status", e);
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }

    private void parseTimerResponse(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            boolean isActive = jsonObject.getBoolean("active");
            String elapsedTimeStr = jsonObject.getString("elapsed_time");

            mainHandler.post(() -> {
                callback.onTimerStatusChanged(isActive);

                if (isActive) {
                    long parsedElapsedTime = parseElapsedTime(elapsedTimeStr);
                    startTimerWithElapsedTime(parsedElapsedTime);
                } else {
                    stopTimer();
                    callback.onTimerUpdate("00:00:00");
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error parsing timer response", e);
            mainHandler.post(() -> callback.onError("Error parsing response: " + e.getMessage()));
        }
    }

    private long parseElapsedTime(String timeString) {
        try {
            // Parse format "00:02:17.392000" to milliseconds
            String[] parts = timeString.split("\\.");
            String timePart = parts[0]; // "00:02:17"
            String microsecondsPart = parts.length > 1 ? parts[1] : "0";

            String[] timeParts = timePart.split(":");
            int hours = Integer.parseInt(timeParts[0]);
            int minutes = Integer.parseInt(timeParts[1]);
            int seconds = Integer.parseInt(timeParts[2]);

            // Convert microseconds to milliseconds (first 3 digits)
            int milliseconds = 0;
            if (microsecondsPart.length() >= 3) {
                milliseconds = Integer.parseInt(microsecondsPart.substring(0, 3));
            }

            return (hours * 3600L + minutes * 60L + seconds) * 1000L + milliseconds;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing elapsed time: " + timeString, e);
            return 0;
        }
    }

    public void startTimerWithElapsedTime(long elapsedMs) {
        stopTimer(); // Stop any existing timer

        this.elapsedTime = elapsedMs;
        this.startTime = System.currentTimeMillis() - elapsedMs;
        this.isRunning = true;

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    long currentTime = System.currentTimeMillis();
                    long totalElapsed = currentTime - startTime;

                    String formattedTime = formatTime(totalElapsed);
                    callback.onTimerUpdate(formattedTime);

                    mainHandler.postDelayed(this, 1000); // Update every second
                }
            }
        };

        mainHandler.post(timerRunnable);
    }

    public void stopTimer() {
        isRunning = false;
        if (timerRunnable != null) {
            mainHandler.removeCallbacks(timerRunnable);
        }
    }

    private String formatTime(long milliseconds) {
        long totalSeconds = milliseconds / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public void startPeriodicCheck() {
        startPeriodicCheck(30000); // Check every 30 seconds by default
    }

    public void startPeriodicCheck(long intervalMs) {
        Runnable periodicCheck = new Runnable() {
            @Override
            public void run() {
                checkTimerStatus();
                mainHandler.postDelayed(this, intervalMs);
            }
        };
        mainHandler.post(periodicCheck);
    }

    public void destroy() {
        stopTimer();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    public boolean isTimerRunning() {
        return isRunning;
    }
    public String getGoalName(){
        return goalName;
    }
}