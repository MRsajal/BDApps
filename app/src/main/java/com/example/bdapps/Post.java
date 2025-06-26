package com.example.bdapps;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Post {
    private String content;
    private String username;
    private Integer id;
    private String timeAgo;
    private long timestamp;

    public Post(String content,String username){
        this.content=content;
        this.username=username;
        this.timestamp=System.currentTimeMillis();
        this.timeAgo="Just now";
    }
    public Post(Integer id,String content, String username, String createdAt) {
        this.id=id;
        this.content = content;
        this.username = username;
        this.timestamp = parseTimestamp(createdAt);
        this.timeAgo = calculateTimeAgo(this.timestamp);
    }
    private long parseTimestamp(String createdAt) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = sdf.parse(createdAt);
            return date != null ? date.getTime() : System.currentTimeMillis();
        } catch (ParseException e) {
            e.printStackTrace();
            return System.currentTimeMillis();
        }
    }
    private String calculateTimeAgo(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - timestamp;

        // Convert to seconds
        long seconds = timeDiff / 1000;

        if (seconds < 60) {
            return "Just now";
        }

        // Convert to minutes
        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        }

        // Convert to hours
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        }

        // Convert to days
        long days = hours / 24;
        if (days < 7) {
            return days + (days == 1 ? " day ago" : " days ago");
        }

        // Convert to weeks
        long weeks = days / 7;
        if (weeks < 4) {
            return weeks + (weeks == 1 ? " week ago" : " weeks ago");
        }

        // Convert to months (approximate)
        long months = days / 30;
        if (months < 12) {
            return months + (months == 1 ? " month ago" : " months ago");
        }

        // Convert to years
        long years = days / 365;
        return years + (years == 1 ? " year ago" : " years ago");
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


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
