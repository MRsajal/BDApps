package com.example.bdapps.ProfileComponent.Education;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.bdapps.AboutBottomSheet;
import com.example.bdapps.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EducationBottomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "AddEducationBottomSheet";
    private static final String API_BASE_URL = "https://dormitorybackend.duckdns.org/api/auth/profile/education";

    // Views
    private TextInputEditText etInstitutionId;
    private TextInputEditText etInstitutionName;
    private TextInputEditText etLocation;
    private TextInputEditText etWebsite;
    private TextInputEditText etMajor;
    private TextInputEditText actvDegree;
    private TextInputEditText etSeries;
    private TextInputEditText etStartDate;
    private TextInputEditText etEndDate;
    private TextInputEditText etDescription;
    private SwitchMaterial switchCurrent;
    private TextInputLayout tilEndDate;
    private MaterialButton btnCancel;
    private MaterialButton btnSave;

    // Data
    private RequestQueue requestQueue;
    private SharedPreferences prefs;
    private Calendar startDateCalendar = Calendar.getInstance();
    private Calendar endDateCalendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // Callback interface
    public interface OnEducationActionListener {
        void onEducationSaved();
        void onEducationCancelled();
    }

    private OnEducationActionListener listener;

    public void setOnEducationActionListener(OnEducationActionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestQueue = Volley.newRequestQueue(requireContext());
        prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.education_bottom_sheet_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupDatePickers();
        setupClickListeners();
        setupCurrentStudySwitch();
    }

    private void initializeViews(View view) {
        etInstitutionId = view.findViewById(R.id.etInstitutionId);
        etInstitutionName = view.findViewById(R.id.etInstitutionName);
        etLocation = view.findViewById(R.id.etLocation);
        etWebsite = view.findViewById(R.id.etWebsite);
        etMajor = view.findViewById(R.id.etMajor);
        actvDegree = view.findViewById(R.id.actvDegree);
        etSeries = view.findViewById(R.id.etSeries);
        etStartDate = view.findViewById(R.id.etStartDate);
        etEndDate = view.findViewById(R.id.etEndDate);
        etDescription = view.findViewById(R.id.etDescription);
        switchCurrent = view.findViewById(R.id.switchCurrent);
        tilEndDate = view.findViewById(R.id.tilEndDate);
        btnCancel = view.findViewById(R.id.btnCancelEducation);
        btnSave = view.findViewById(R.id.btnSaveEducation);
    }

    private void setupDatePickers() {
        etStartDate.setOnClickListener(v -> showDatePicker(true));
        etEndDate.setOnClickListener(v -> showDatePicker(false));
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = isStartDate ? startDateCalendar : endDateCalendar;

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    String formattedDate = dateFormat.format(calendar.getTime());
                    if (isStartDate) {
                        etStartDate.setText(formattedDate);
                    } else {
                        etEndDate.setText(formattedDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void setupCurrentStudySwitch() {
        switchCurrent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                tilEndDate.setVisibility(View.GONE);
                etEndDate.setText("");
            } else {
                tilEndDate.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEducationCancelled();
            }
            AboutBottomSheet aboutBottomSheet = new AboutBottomSheet();
            aboutBottomSheet.show(getParentFragmentManager(), "AboutBottomSheet");
        });

        btnSave.setOnClickListener(v -> {
            saveEducation();
        });
    }

    private void saveEducation() {
        // Validate required fields

        if (!validateFields()) {
            return;
        }

        String accessToken = prefs.getString("access_token", "");
        if (accessToken.isEmpty()) {
            Toast.makeText(getContext(), "No access token found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create JSON object with education data in the required format
        JSONObject educationData = new JSONObject();
        try {
            // Institution ID (default to 0 as in your form)
//            educationData.put("institution_id", 0);

            // Institution data as nested object
            JSONObject institutionData = new JSONObject();
            institutionData.put("name", etInstitutionName.getText().toString().trim());
            institutionData.put("location", etLocation.getText().toString().trim());
            institutionData.put("website", etWebsite.getText().toString().trim());
            educationData.put("institution_data", institutionData);

            // Other education fields
            educationData.put("major", etMajor.getText().toString().trim());
            educationData.put("degree", actvDegree.getText().toString().trim());
            educationData.put("series", etSeries.getText().toString().trim());
            educationData.put("start_date", etStartDate.getText().toString().trim());

            // End date (only if not currently studying and end date is provided)
            if (!switchCurrent.isChecked() && !etEndDate.getText().toString().trim().isEmpty()) {
                educationData.put("end_date", etEndDate.getText().toString().trim());
            } else if (switchCurrent.isChecked()) {
                // If currently studying, you might want to set end_date to null or omit it
                // Based on your backend requirements, adjust this part
                educationData.put("end_date", JSONObject.NULL);
            }

            educationData.put("is_current", switchCurrent.isChecked());
            educationData.put("description", etDescription.getText().toString().trim());

        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON body", e);
            Toast.makeText(getContext(), "Error preparing data", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Sending education data: " + educationData.toString());


        // Make API request
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                API_BASE_URL,
                educationData,
                response -> {
                    Log.d(TAG, "Education saved successfully: " + response.toString());
                    Toast.makeText(getContext(), "Education added successfully", Toast.LENGTH_SHORT).show();

                    if (listener != null) {
                        listener.onEducationSaved();
                    }
                    dismiss();

                },
                error -> {
                    Log.e(TAG, "Error saving education", error);
                    String errorMessage = "Failed to save education data";

                    if (error.networkResponse != null) {
                        try {
                            String responseBody = new String(error.networkResponse.data, "utf-8");
                            JSONObject errorResponse = new JSONObject(responseBody);
                            if (errorResponse.has("message")) {
                                errorMessage = errorResponse.getString("message");
                            } else if (errorResponse.has("detail")) {
                                errorMessage = errorResponse.getString("detail");
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
                        } else if (statusCode == 422) {
                            errorMessage = "Data validation failed: " + errorMessage;
                        }
                    }

                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                return headers;
            }
        };


        requestQueue.add(request);

    }


    private boolean validateFields() {
        boolean isValid = true;

        // Validate Institution Name
        if (etInstitutionName.getText().toString().trim().isEmpty()) {
            etInstitutionName.setError("Institution name is required");
            isValid = false;
        }

        // Validate Location
        if (etLocation.getText().toString().trim().isEmpty()) {
            etLocation.setError("Location is required");
            isValid = false;
        }

        // Validate Major
        if (etMajor.getText().toString().trim().isEmpty()) {
            etMajor.setError("Major/Field of study is required");
            isValid = false;
        }

        // Validate Degree
        if (actvDegree.getText().toString().trim().isEmpty()) {
            actvDegree.setError("Degree is required");
            isValid = false;
        }

        // Validate Start Date
        if (etStartDate.getText().toString().trim().isEmpty()) {
            etStartDate.setError("Start date is required");
            isValid = false;
        }

        // Validate End Date if not currently studying
        if (!switchCurrent.isChecked() && etEndDate.getText().toString().trim().isEmpty()) {
            etEndDate.setError("End date is required when not currently studying");
            isValid = false;
        }

        return isValid;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }
}