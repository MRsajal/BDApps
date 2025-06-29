package com.example.bdapps;

import com.example.bdapps.PreviousMessage;

import java.util.ArrayList;
import java.util.List;

class Conversation {
    private String id;
    private String title;
    private String createdAt;
    private String updatedAt;
    private List<PreviousMessage> messages;

    // Constructors
    public Conversation() {
        this.messages = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public List<PreviousMessage> getMessages() { return messages; }
    public void setMessages(List<PreviousMessage> messages) { this.messages = messages; }

    public String getPreviewText() {
        if (messages != null && !messages.isEmpty()) {
            return messages.get(0).getContent();
        }
        return "No messages";
    }
}