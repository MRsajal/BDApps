package com.example.bdapps;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class Login extends AppCompatActivity {

    EditText etUsername,etPassword;
    Button btnLogin,btnGoToRegister;
    DatabaseHelper_login databaseHelperLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername=findViewById(R.id.et_username);
        etPassword=findViewById(R.id.et_password);
        btnLogin=findViewById(R.id.btn_login);
        btnGoToRegister=findViewById(R.id.btn_go_to_register);

        checkLoginStatus();
        databaseHelperLogin=new DatabaseHelper_login(this);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
        btnGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, Registration.class));
                finish();
            }
        });


    }

    private void checkLoginStatus() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String savedToken = prefs.getString("access_token", null);
        String savedUsername = prefs.getString("username", null);

        if (savedToken != null && savedUsername != null) {
            // User is already logged in, go to MainActivity
            Intent intent = new Intent(Login.this, MainActivity.class);
            intent.putExtra("access_token", savedToken);
            intent.putExtra("current_username", savedUsername);
            startActivity(intent);
            finish();
        }
    }

    private void loginUser() {
        SharedPreferences prefs1 = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String savedToken = prefs1.getString("access_token", null);
        String savedUsername = prefs1.getString("username", null);

        if (savedUsername != null) {
            // Token exists, go to MainActivity directly
            Intent intent = new Intent(Login.this, MainActivity.class);
            intent.putExtra("access_token", savedToken);
            intent.putExtra("current_username", savedUsername);
            startActivity(intent);
            finish(); // Don't come back to login screen
            return;
        }

        String username=etUsername.getText().toString().trim();
        String password=etPassword.getText().toString().trim();

        if(TextUtils.isEmpty(username)){
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        String url="https://dormitorybackend.duckdns.org/api/token";
        JSONObject jsonBody=new JSONObject();
        try {
            jsonBody.put("username",username);
            jsonBody.put("password",password);
        }catch (JSONException e){
            e.printStackTrace();
            Toast.makeText(this, "Error creating login request", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    try {
                        String accessToken = response.getString("access");

                        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                        prefs.edit().putString("access_token", accessToken).apply();
                        prefs.edit().putString("username",username).apply();
                        //prefs.edit().putString("user_email",username).apply();

                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(Login.this, MainActivity.class);
                        intent.putExtra("current_username", username);
                        intent.putExtra("access_token", accessToken); // send access token
                        startActivity(intent);
                        finish();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to parse token", Toast.LENGTH_SHORT).show();
                    }
                }
                ,
                error -> {
                    if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                        Toast.makeText(Login.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Login.this, "Login failed: " + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            @Override
            public String getBodyContentType(){
                return "application/json; charset=utf-8";
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }
}