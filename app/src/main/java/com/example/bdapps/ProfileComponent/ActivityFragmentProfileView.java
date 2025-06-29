package com.example.bdapps.ProfileComponent;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.bdapps.ProfileComponent.Timer.TimerActivity;
import com.example.bdapps.ProfileComponent.Timer.TimerManager;
import com.example.bdapps.R;

public class ActivityFragmentProfileView extends Fragment implements TimerManager.TimerCallback {

    private TextView tvTimerActivity;
    private TextView tvStatusActivity;
    private TextView tvCurrentGoalActivity;
    private TimerManager timerManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activity_profile_view, container, false);

        // Initialize views
        tvTimerActivity = view.findViewById(R.id.tvTimerActivity);
        tvStatusActivity = view.findViewById(R.id.tvStatusActivity);
        tvCurrentGoalActivity = view.findViewById(R.id.tvCurrentGoalActivity);
        ImageButton btnTimerExpand = view.findViewById(R.id.btntTimerExpand);

        // Initialize timer manager with context
        timerManager = new TimerManager(getContext(), this);

        // Set initial state
        tvTimerActivity.setText("00:00:00");
        tvStatusActivity.setText("Checking timer status...");

        // Set an OnClickListener for expand button
        btnTimerExpand.setOnClickListener(v -> {
            // Navigate to Timer activity
            Intent intent = new Intent(getActivity(), TimerActivity.class);
            startActivity(intent);
        });

        // Check timer status when fragment is created
        checkTimerStatus();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check timer status when fragment resumes
        checkTimerStatus();
        // Start periodic checking every 30 seconds to keep timer in sync
        if (timerManager != null) {
            timerManager.startPeriodicCheck();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop periodic checking when fragment is paused to save resources
        // But keep the timer running for display updates
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up timer manager
        if (timerManager != null) {
            timerManager.destroy();
        }
    }

    private void checkTimerStatus() {
        if (timerManager != null) {
            timerManager.checkTimerStatus();
        }
    }

    // TimerManager.TimerCallback implementation
    @Override
    public void onTimerUpdate(String formattedTime) {
        if (tvTimerActivity != null) {
            tvTimerActivity.setText(formattedTime);
        }
    }

    @Override
    public void onTimerStatusChanged(boolean isActive) {
        if (tvStatusActivity != null && tvCurrentGoalActivity != null) {
            if (isActive) {
                tvStatusActivity.setText("Timer running");
                tvCurrentGoalActivity.setText("Focus session active");
            } else {
                tvStatusActivity.setText("Press expand to start");
                tvCurrentGoalActivity.setText("Ready to focus");
            }
        }
    }

    @Override
    public void onError(String error) {
        if (tvStatusActivity != null) {
            tvStatusActivity.setText("Connection error");
        }

        // Show toast for error (optional)
        if (getContext() != null) {
            Toast.makeText(getContext(), "Timer error: " + error, Toast.LENGTH_SHORT).show();
        }
    }
}