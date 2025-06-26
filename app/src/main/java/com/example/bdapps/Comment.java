package com.example.bdapps;

public class Comment {
    private Integer id;
    private String content;
    private String author;
    private String createdAt;
    private Integer postId;

    public Comment(){}
    public Comment(String content,String author,Integer postId){
        this.content=content;
        this.author=author;
        this.postId=postId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }
}
