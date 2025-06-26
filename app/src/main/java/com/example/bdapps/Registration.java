package com.example.bdapps;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

public class Registration extends AppCompatActivity {

    EditText etUsername, etName, etPassword, etConfirmPassword, etEmail;
    Button btnRegister, btnGoToLogin;
    DatabaseHelper_login databaseHelperLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        etUsername = findViewById(R.id.et_username);
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        btnGoToLogin = findViewById(R.id.btn_go_to_login);

        databaseHelperLogin = new DatabaseHelper_login(this);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
        btnGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Registration.this, Login.class));
                finish();
            }
        });
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        //API URL
        String url = "https://dormitorybackend.duckdns.org/api/auth/register";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username",username);
            jsonBody.put("name",name);
            jsonBody.put("email",email);
            jsonBody.put("password",password);
        }catch (JSONException e){
            e.printStackTrace();
            Toast.makeText(this, "Failed to create JSON", Toast.LENGTH_SHORT).show();
            return;
        }

        //Send request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Registration.this, Login.class));
                    finish();
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.statusCode == 400) {
                        Toast.makeText(this, "Validation error: Please check your input", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Registration failed: " + error.toString(), Toast.LENGTH_LONG).show();
                    }
                }
        ){
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };
        // Add request to queue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
    }
}