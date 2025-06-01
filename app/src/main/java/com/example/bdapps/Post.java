package com.example.bdapps;

public class Post {
    private String content;
    private String username;
    private String timeAgo;
    private long timestamp;

    public Post(String content,String username){
        this.content=content;
        this.username=username;
        this.timestamp=System.currentTimeMillis();
        this.timeAgo="Just now";
    }

    public String getContent() {
        return content;
    }

    public String getUsername() {
        return username;
    }

    public String getTimeAgo() {
        return timeAgo;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
