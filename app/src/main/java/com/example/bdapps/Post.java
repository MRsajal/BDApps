package com.example.bdapps;

import android.util.Log;

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
    public Post(String content, String username) {
        this.content = content;
        this.username = username;
        this.timestamp = System.currentTimeMillis();
        this.timeAgo = "Just now";
    }

    public Post(Integer id, String content, String username, String createdAt) {
        this.id = id;
        this.content = content;
        this.username = username;
        this.timestamp = parseTimestamp(createdAt);
        this.timeAgo = calculateTimeAgo(this.timestamp);
    }

    private long parseTimestamp(String createdAt) {
        // Common date formats from APIs
        SimpleDateFormat[] formats = {
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()),     // 2024-01-15T10:30:45.123Z
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()),         // 2024-01-15T10:30:45Z
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault()),  // 2024-01-15T10:30:45.123456Z
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),            // 2024-01-15T10:30:45
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),              // 2024-01-15 10:30:45
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault()),     // 2024-01-15T10:30:45.123+05:30
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())          // 2024-01-15T10:30:45+05:30
        };

        // Set all formats to UTC timezone
        for (SimpleDateFormat format : formats) {
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        // Try each format
        for (SimpleDateFormat format : formats) {
            try {
                Date date = format.parse(createdAt);
                if (date != null) {
                    Log.d("TIMESTAMP_DEBUG", "Successfully parsed: " + createdAt + " with format: " + format.toPattern());
                    return date.getTime();
                }
            } catch (ParseException e) {
                // Try next format
                continue;
            }
        }

        // If all formats fail, log the error and return current time
        Log.e("TIMESTAMP_ERROR", "Could not parse timestamp: " + createdAt);
        return System.currentTimeMillis();
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

    // Getters and setters
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

    public Integer getId() {
        return id;
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

    public void setId(Integer id) {
        this.id = id;
    }
}