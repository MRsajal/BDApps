package com.example.bdapps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatBot extends AppCompatActivity {
    RecyclerView chatRecyclerView;
    Button history;
    EditText messageEditText;
    ImageButton sendButton;
    ChatAdapter chatAdapter;
    List<ChatMessage> messageList;
    private String conversationId;
    private static final String BASE_URL="https://dormitorybackend.duckdns.org/";
    String AUTH_TOKEN; // Replace with your actual token
    private RequestQueue requestQueue;
    private boolean isConversationsStarted=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);

        requestQueue= Volley.newRequestQueue(this);
        AUTH_TOKEN=getIntent().getStringExtra("access_token");

        initViews();
        setupRecyclerView();
        setupClickListeners();
        addBotMessage("Hello! I'm your sample chatbot.");
    }

    private void setupClickListeners() {
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        messageEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_SEND ||
                        (event!=null && event.getKeyCode()==KeyEvent.KEYCODE_ENTER)){
                    sendMessage();
                    return true;
                }
                return false;
            }
        });
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatBot.this,ChatHistoryActivity.class);
                startActivity(intent);
            }
        });
    }

    private void sendMessage() {
        String userMessage = messageEditText.getText().toString().trim();
        if (!userMessage.isEmpty()) {
            addUserMessage(userMessage);
            messageEditText.setText(""); // Clear input
            sendButton.setEnabled(false);

            if (!isConversationsStarted) {
                // First time: POST to create conversation
                startConversation(userMessage);
            } else {
                sendMessageToAPI(userMessage);
            }
        }
    }

    private void sendMessageToAPI(String message) {
        if(conversationId==null){
            showError("No active conversation");
            sendButton.setEnabled(true);
            return;
        }

        String url = BASE_URL + "api/llm/conversations/" + conversationId + "/send";

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("message", message);
        } catch (JSONException e) {
            e.printStackTrace();
            showError("Failed to create message");
            sendButton.setEnabled(true);
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    // Successfully sent
                    // Now fetch conversation messages
                    fetchMessagesFromConversation();
                },
                error -> {
                    handleAPIError(error);
                    sendButton.setEnabled(true);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + AUTH_TOKEN);
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(request);
    }

    private void fetchMessagesFromConversation() {
        String url = BASE_URL + "api/llm/conversations/" + conversationId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONArray messages = response.getJSONArray("messages");
                        for (int i = messages.length() - 1; i >= 0; i--) {
                            JSONObject messageObj = messages.getJSONObject(i);
                            String sender = messageObj.getString("sender");
                            String text = messageObj.getString("text");
                            if (!"user".equals(sender)) {
                                addBotMessage(text);
                                break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showError("Failed to parse messages");
                    }
                    sendButton.setEnabled(true);
                },
                error -> {
                    handleAPIError(error);
                    sendButton.setEnabled(true);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + AUTH_TOKEN);
                return headers;
            }
        };

        requestQueue.add(request);
    }


    private void handleAPIError(VolleyError error) {
        String errorMessage = "Network error occurred";

        if (error.networkResponse != null) {
            int statusCode = error.networkResponse.statusCode;
            errorMessage = "Server error: " + statusCode;

            if (error.networkResponse.data != null) {
                try {
                    String responseBody = new String(error.networkResponse.data, "utf-8");
                    Log.e("ChatBot", "Error response: " + responseBody);
                } catch (Exception e) {
                    Log.e("ChatBot", "Error parsing error response", e);
                }
            }
        } else if (error instanceof TimeoutError) {
            errorMessage = "Request timed out";
        } else if (error instanceof NoConnectionError) {
            errorMessage = "No internet connection";
        }

        showError(errorMessage);
    }

    private void showError(String errorMessage) {
        addBotMessage("Sorry, there was an error: " + errorMessage);
        Log.e("ChatBot", "Error: " + errorMessage);
    }

    private void startConversation(String initialMessage) {
        String url = BASE_URL + "api/llm/conversations";

        JSONObject requestBody=new JSONObject();
        try {
            requestBody.put("title",initialMessage);
        }catch (JSONException e){
            e.printStackTrace();
            showError("Failed to create conversation");
            sendButton.setEnabled(true);
            return;
        }

        JsonObjectRequest request=new JsonObjectRequest(
                com.android.volley.Request.Method.POST,
                url,
                requestBody,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            conversationId = response.getString("id");
                            isConversationsStarted = true;
                            // Now send the initial message using the correct endpoint
                            sendMessageToAPI(initialMessage);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showError("Failed to parse conversation response");
                            sendButton.setEnabled(true);
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handleAPIError(error);
                        sendButton.setEnabled(true);
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + AUTH_TOKEN);
                return headers;
            }
        };
        requestQueue.add(request);
    }

    private void addBotMessage(String botResponse) {
        ChatMessage chatMessage=new ChatMessage(botResponse,false);
        messageList.add(chatMessage);
        chatAdapter.notifyItemInserted(messageList.size()-1);
        scrollToBottom();
    }

    private void addUserMessage(String userMessage) {
        ChatMessage chatMessage=new ChatMessage(userMessage,true);
        messageList.add(chatMessage);
        chatAdapter.notifyItemInserted(messageList.size()-1);
        scrollToBottom();
    }

    private void scrollToBottom() {
        if(messageList.size()>0){
            chatRecyclerView.smoothScrollToPosition(messageList.size()-1);
        }
    }

    private void setupRecyclerView() {
        messageList=new ArrayList<>();
        chatAdapter=new ChatAdapter(messageList);

        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);
    }

    private void initViews() {
        chatRecyclerView=findViewById(R.id.chatRecyclerView);
        messageEditText=findViewById(R.id.messageEditText);
        sendButton=findViewById(R.id.sendButton);
        history=findViewById(R.id.buttonViewHistory);
    }
}