package com.example.bdapps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatDetailActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private List<PreviousMessage> messages;
    private String conversationId;
    private String conversationTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        conversationId = getIntent().getStringExtra("conversation_id");
        conversationTitle = getIntent().getStringExtra("conversation_title");

        initViews();
        setupRecyclerView();
        loadConversationMessages();
    }
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewMessages);
        messages = new ArrayList<>();

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(conversationTitle);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(messages, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);
    }
    private void loadConversationMessages() {
            String url = "https://dormitorybackend.duckdns.org/api/llm/conversations/" + conversationId;

            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            String accessToken = prefs.getString("access_token", null);

            if (accessToken == null) {
                // Handle missing token
                Toast.makeText(this, "Access token missing", Toast.LENGTH_SHORT).show();
                return;
            }

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    response -> {
                        try {
                            JSONArray messagesArray = response.getJSONArray("messages");
                            messages.clear();

                            for (int i = 0; i < messagesArray.length(); i++) {
                                JSONObject messageObj = messagesArray.getJSONObject(i);

                                PreviousMessage message = new PreviousMessage();
                                message.setId(String.valueOf(messageObj.optInt("id")));
                                message.setRole(messageObj.optString("sender", "user")); // using "sender"
                                message.setContent(messageObj.optString("text", ""));
                                message.setTimestamp(messageObj.optString("timestamp", ""));

                                messages.add(message);
                            }

                            messageAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Error parsing messages", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Toast.makeText(this, "Failed to load messages", Toast.LENGTH_SHORT).show();
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + accessToken); // NOTE the space after Bearer
                    return headers;
                }
            };

            Volley.newRequestQueue(this).add(request);
        }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}