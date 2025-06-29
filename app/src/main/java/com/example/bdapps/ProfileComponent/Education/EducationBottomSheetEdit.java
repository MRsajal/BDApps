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
import com.example.bdapps.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EducationBottomSheetEdit extends BottomSheetDialogFragment {

    private static final String TAG = "EducationBottomSheetEdit";
    private static final String API_BASE_URL = "https://dormitorybackend.duckdns.org/api/auth/profile/education";

    // Views
    private TextInputEditText etInstitutionId;
    private TextInputEditText etInstitutionNameEdit;
    private TextInputEditText etLocationEdit;
    private TextInputEditText etWebsiteEdit;
    private TextInputEditText etMajorEdit;
    private TextInputEditText actvDegreeEdit;
    private TextInputEditText etSeriesEdit;
    private TextInputEditText etStartDateEdit;
    private TextInputEditText etEndDate;
    private TextInputEditText etDescriptionEdit;
    private SwitchMaterial switchCurrentEdit;
    private TextInputLayout tilEndDateEdit;
    private MaterialButton btnDeletelEducationEdit;
    private MaterialButton btnCancelEducationEdit;
    private MaterialButton btnSaveEducationEdit;

    // Data
    private int educationId;
    private int position;
    private RequestQueue requestQueue;
    private SharedPreferences prefs;
    private OnEducationActionListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public interface OnEducationActionListener {
        void onEducationSavedEdit();
        void onEducationCancelledEdit();
        void onEducationDeleteEdit();
    }

    public void setOnEducationActionListener(OnEducationActionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestQueue = Volley.newRequestQueue(requireContext());
        prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        // Get arguments
        Bundle args = getArguments();
        if (args != null) {
            educationId = args.getInt("education_id", 0);
            position = args.getInt("position", -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.education_bottom_sheet_edit_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupClickListeners();
        loadEducationData();
    }

    private void initializeViews(View view) {
        etInstitutionId = view.findViewById(R.id.etInstitutionId);
        etInstitutionNameEdit = view.findViewById(R.id.etInstitutionNameEdit);
        etLocationEdit = view.findViewById(R.id.etLocationEdit);
        etWebsiteEdit = view.findViewById(R.id.etWebsiteEdit);
        etMajorEdit = view.findViewById(R.id.etMajorEdit);
        actvDegreeEdit = view.findViewById(R.id.actvDegreeEdit);
        etSeriesEdit = view.findViewById(R.id.etSeriesEdit);
        etStartDateEdit = view.findViewById(R.id.etStartDateEdit);
        etEndDate = view.findViewById(R.id.etEndDate);
        etDescriptionEdit = view.findViewById(R.id.etDescriptionEdit);
        switchCurrentEdit = view.findViewById(R.id.switchCurrentEdit);
        tilEndDateEdit = view.findViewById(R.id.tilEndDateEdit);
        btnDeletelEducationEdit = view.findViewById(R.id.btnDeletelEducationEdit);
        btnCancelEducationEdit = view.findViewById(R.id.btnCancelEducationEdit);
        btnSaveEducationEdit = view.findViewById(R.id.btnSaveEducationEdit);
    }

    private void setupClickListeners() {
        // Date picker for start date
        etStartDateEdit.setOnClickListener(v -> showDatePicker(true));

        // Date picker for end date
        etEndDate.setOnClickListener(v -> showDatePicker(false));

        // Switch listener for current education
        switchCurrentEdit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                tilEndDateEdit.setVisibility(View.GONE);
                etEndDate.setText("");
            } else {
                tilEndDateEdit.setVisibility(View.VISIBLE);
            }
        });

        // Button listeners
        btnSaveEducationEdit.setOnClickListener(v -> saveEducationData());
        btnCancelEducationEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEducationCancelledEdit();
            }
            dismiss();
        });
        btnDeletelEducationEdit.setOnClickListener(v -> deleteEducationData());
    }

    private void loadEducationData() {
        String accessToken = prefs.getString("access_token", "");

        if (accessToken.isEmpty()) {
            Toast.makeText(getContext(), "No access token found", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = API_BASE_URL + "/" + educationId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        populateFields(response);
                    } catch (Exception e) {
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
                            errorMessage = "Education data not found";
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

    private void populateFields(JSONObject educationData) throws JSONException {
        // Set basic education fields
        etMajorEdit.setText(educationData.optString("major", ""));
        actvDegreeEdit.setText(educationData.optString("degree", ""));
        etSeriesEdit.setText(educationData.optString("series", ""));
        etDescriptionEdit.setText(educationData.optString("description", ""));

        // Set dates
        String startDate = educationData.optString("start_date", "");
        String endDate = educationData.optString("end_date", "");
        boolean isCurrent = educationData.optBoolean("is_current", false);

        if (!startDate.isEmpty()) {
            etStartDateEdit.setText(formatDateForDisplay(startDate));
        }

        switchCurrentEdit.setChecked(isCurrent);
        if (isCurrent) {
            tilEndDateEdit.setVisibility(View.GONE);
            etEndDate.setText("");
        } else {
            tilEndDateEdit.setVisibility(View.VISIBLE);
            if (!endDate.isEmpty()) {
                etEndDate.setText(formatDateForDisplay(endDate));
            }
        }

        // Set institution data
        JSONObject institutionData = educationData.optJSONObject("institution");
        if (institutionData != null) {
            int institutionId = institutionData.optInt("id", 0);
            etInstitutionId.setText(String.valueOf(institutionId));
            etInstitutionNameEdit.setText(institutionData.optString("name", ""));
            etLocationEdit.setText(institutionData.optString("location", ""));
            etWebsiteEdit.setText(institutionData.optString("website", ""));
        }
    }

    private void saveEducationData() {
        if (!validateFields()) {
            return;
        }

        String accessToken = prefs.getString("access_token", "");
        if (accessToken.isEmpty()) {
            Toast.makeText(getContext(), "No access token found", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject requestData = createRequestData();
            String url = API_BASE_URL + "/" + educationId;

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PATCH,
                    url,
                    requestData,
                    response -> {
                        Toast.makeText(getContext(), "Education updated successfully", Toast.LENGTH_SHORT).show();
                        if (listener != null) {
                            listener.onEducationSavedEdit();
                        }
                        dismiss();
                    },
                    error -> {
                        Log.e(TAG, "Error saving education data", error);
                        String errorMessage = "Failed to save education data";

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

        } catch (JSONException e) {
            Log.e(TAG, "Error creating request data", e);
            Toast.makeText(getContext(), "Error preparing data", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteEducationData() {
        String accessToken = prefs.getString("access_token", "");
        if (accessToken.isEmpty()) {
            Toast.makeText(getContext(), "No access token found", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = API_BASE_URL + "/" + educationId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                response -> {
                    Toast.makeText(getContext(), "Education deleted successfully", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onEducationDeleteEdit();
                    }
                    dismiss();
                },
                error -> {
                    Log.e(TAG, "Error deleting education data", error);
                    String errorMessage = "Failed to delete education data";
                    dismiss();

                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        if (statusCode == 401) {
                            errorMessage = "Unauthorized access. Please login again.";
                        } else if (statusCode == 404) {
                            errorMessage = "Education data not found";
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

    private boolean validateFields() {
        boolean isValid = true;

        // Validate required fields
        if (etInstitutionNameEdit.getText().toString().trim().isEmpty()) {
            etInstitutionNameEdit.setError("Institution name is required");
            isValid = false;
        }

        if (etLocationEdit.getText().toString().trim().isEmpty()) {
            etLocationEdit.setError("Location is required");
            isValid = false;
        }

        if (etMajorEdit.getText().toString().trim().isEmpty()) {
            etMajorEdit.setError("Major/Field of Study is required");
            isValid = false;
        }

        if (actvDegreeEdit.getText().toString().trim().isEmpty()) {
            actvDegreeEdit.setError("Degree is required");
            isValid = false;
        }

        if (etStartDateEdit.getText().toString().trim().isEmpty()) {
            etStartDateEdit.setError("Start date is required");
            isValid = false;
        }

        // Validate end date if not current
        if (!switchCurrentEdit.isChecked() && etEndDate.getText().toString().trim().isEmpty()) {
            etEndDate.setError("End date is required when not current");
            isValid = false;
        }

        return isValid;
    }

    private JSONObject createRequestData() throws JSONException {
        JSONObject requestData = new JSONObject();

        // Institution data
        JSONObject institutionData = new JSONObject();
        String institutionIdStr = etInstitutionId.getText().toString().trim();
        if (!institutionIdStr.isEmpty() && !institutionIdStr.equals("0")) {
            institutionData.put("id", Integer.parseInt(institutionIdStr));
        }
        institutionData.put("name", etInstitutionNameEdit.getText().toString().trim());
        institutionData.put("location", etLocationEdit.getText().toString().trim());
        institutionData.put("website", etWebsiteEdit.getText().toString().trim());
        institutionData.put("students", ""); // Default empty as per your data structure

        requestData.put("institution", institutionData);

        // Education data
        requestData.put("major", etMajorEdit.getText().toString().trim());
        requestData.put("degree", actvDegreeEdit.getText().toString().trim());
        requestData.put("degree_display", actvDegreeEdit.getText().toString().trim()); // Same as degree for now
        requestData.put("series", etSeriesEdit.getText().toString().trim());
        requestData.put("description", etDescriptionEdit.getText().toString().trim());
        requestData.put("is_current", switchCurrentEdit.isChecked());

        // Date fields
        String startDateStr = etStartDateEdit.getText().toString().trim();
        if (!startDateStr.isEmpty()) {
            requestData.put("start_date", formatDateForApi(startDateStr));
        }

        if (!switchCurrentEdit.isChecked()) {
            String endDateStr = etEndDate.getText().toString().trim();
            if (!endDateStr.isEmpty()) {
                requestData.put("end_date", formatDateForApi(endDateStr));
            }
        } else {
            requestData.put("end_date", JSONObject.NULL);
        }

        return requestData;
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();

        // Set current date from field if available
        TextInputEditText dateField = isStartDate ? etStartDateEdit : etEndDate;
        String currentDate = dateField.getText().toString().trim();
        if (!currentDate.isEmpty()) {
            try {
                Date date = displayDateFormat.parse(currentDate);
                if (date != null) {
                    calendar.setTime(date);
                }
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing current date", e);
            }
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    String formattedDate = displayDateFormat.format(calendar.getTime());
                    dateField.setText(formattedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private String formatDateForDisplay(String apiDate) {
        try {
            Date date = dateFormat.parse(apiDate);
            if (date != null) {
                return displayDateFormat.format(date);
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date for display: " + apiDate, e);
        }
        return apiDate; // Return original if parsing fails
    }

    private String formatDateForApi(String displayDate) {
        try {
            Date date = displayDateFormat.parse(displayDate);
            if (date != null) {
                return dateFormat.format(date);
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error formatting date for API: " + displayDate, e);
        }
        return displayDate; // Return original if parsing fails
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }
}