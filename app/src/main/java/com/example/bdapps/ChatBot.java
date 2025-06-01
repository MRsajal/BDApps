package com.example.bdapps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ChatBot extends AppCompatActivity {
    RecyclerView chatRecyclerView;
    EditText messageEditText;
    ImageButton sendButton;
    ChatAdapter chatAdapter;
    List<ChatMessage> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);

        initViews();
        setupRecyclerView();
        setupClickListeners();
        addBotMessage("Hello! I'm your sample chatbot. I'll echo back whatever you send me!");
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
                sendMessage();
                return true;
            }
        });
    }

    private void sendMessage() {
        String userMessage=messageEditText.getText().toString().trim();
        if(!userMessage.isEmpty()){
            addUserMessage(userMessage);
//            messageEditText.setText("");
            chatRecyclerView.postDelayed(() -> {
                // Bot echoes the user message
                String botResponse = "You said: " + userMessage;
                addBotMessage(botResponse);
            }, 500);
        }
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
    }

}