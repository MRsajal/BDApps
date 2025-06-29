package com.example.bdapps;

public class PreviousMessage {
//    private String id;
//    private String content;
//    private String role;
//    private String timestamp;
//    public PreviousMessage(){};
//
//    public PreviousMessage(String content,String role){
//        this.content=content;
//        this.role=role;
//    }
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public String getContent() {
//        return content;
//    }
//
//    public void setContent(String content) {
//        this.content = content;
//    }
//
//    public String getRole() {
//        return role;
//    }
//
//    public void setRole(String role) {
//        this.role = role;
//    }
//
//    public String getTimestamp() {
//        return timestamp;
//    }
//
//    public void setTimestamp(String timestamp) {
//        this.timestamp = timestamp;
//    }
//    public boolean isFormUser(){
//        return "user".equals(role);
//    }
private String id;
    private String content;
    private String role; // "user" or "assistant"
    private String timestamp;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public boolean isFromUser() {
        return "user".equals(role);
    }
}
