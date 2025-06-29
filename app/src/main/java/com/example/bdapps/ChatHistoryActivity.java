package com.example.bdapps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatHistoryActivity extends AppCompatActivity {
    private static final String TAG = "ChatHistoryActivity";
    private static final String API_URL = "https://dormitorybackend.duckdns.org/api/llm/conversations";

    private RecyclerView recyclerView;
    private ChatHistoryAdapter adapter;
    private ProgressBar progressBar;
    private List<Conversation> conversationList;
    private RequestQueue requestQueue;
    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_history);
        SharedPreferences prefs = this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        accessToken = prefs.getString("access_token", null);


        initViews();
        setupRecyclerView();
        loadChatHistory();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewChatHistory);
        progressBar = findViewById(R.id.progressBar);
        conversationList = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(this);

        // Check if views are found
        if (recyclerView == null) {
            Log.e(TAG, "RecyclerView not found in layout");
        }
        if (progressBar == null) {
            Log.e(TAG, "ProgressBar not found in layout");
        }
    }

    private void setupRecyclerView() {
        adapter = new ChatHistoryAdapter(conversationList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadChatHistory() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                API_URL,
                null,
                response -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    try {
                        parseConversations(response);
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error", e);
                        showError("Error parsing chat history");
                    }
                },
                error -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    Log.e(TAG, "API request error", error);
                    showError("Failed to load chat history");
                }
        ){
            @Override
            public Map<String,String> getHeaders() throws AuthFailureError{
                Map<String,String> headers=new HashMap<>();
                headers.put("Authorization","Bearer "+accessToken);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void parseConversations(JSONArray jsonArray) throws JSONException {
        conversationList.clear();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject conversationObj = jsonArray.getJSONObject(i);

            Conversation conversation = new Conversation();
            conversation.setId(conversationObj.optString("id", ""));
            conversation.setTitle(conversationObj.optString("title", "Chat " + (i + 1)));
            conversation.setCreatedAt(conversationObj.optString("created_at", ""));
            conversation.setUpdatedAt(conversationObj.optString("updated_at", ""));

            // Parse messages if they exist
            if (conversationObj.has("messages")) {
                JSONArray messagesArray = conversationObj.getJSONArray("messages");
                List<PreviousMessage> messages = new ArrayList<>();

                for (int j = 0; j < messagesArray.length(); j++) {
                    JSONObject messageObj = messagesArray.getJSONObject(j);

                    PreviousMessage message = new PreviousMessage();
                    message.setId(messageObj.optString("id", ""));
                    message.setContent(messageObj.optString("content", ""));
                    message.setRole(messageObj.optString("role", "user"));
                    message.setTimestamp(messageObj.optString("timestamp", ""));

                    messages.add(message);
                }
                conversation.setMessages(messages);
            }

            conversationList.add(conversation);
        }

        adapter.notifyDataSetChanged();

        if (conversationList.isEmpty()) {
            showError("No chat history found");
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}