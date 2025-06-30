package com.example.bdapps.SearchPost;

public class Comment {
    private int id;
    private String author;
    private String content;
    private String created_at;
    public Comment() {}

    public Comment(int id, String author, String body, String created_at) {
        this.id = id;
        this.author = author;
        this.content = body;
        this.created_at = created_at;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}
