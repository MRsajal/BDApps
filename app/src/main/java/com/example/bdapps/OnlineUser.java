package com.example.bdapps;

public class OnlineUser {
    private String user;
    private String domain;
    private String goal;
    private String totalStudiedTimeToday;
    private long onlineStartTime;
    private long currentOnlineTime;
    private long baseElapsedTime; // New field to store initial elapsed time from backend

    public OnlineUser() {
        this.onlineStartTime = System.currentTimeMillis();
        this.currentOnlineTime = 0;
        this.baseElapsedTime = 0;
    }

    public OnlineUser(String user, String domain, String goal, String totalStudiedTimeToday) {
        this.user = user;
        this.domain = domain;
        this.goal = goal;
        this.totalStudiedTimeToday = totalStudiedTimeToday;
        this.onlineStartTime = System.currentTimeMillis();
        this.currentOnlineTime = 0;
        this.baseElapsedTime = parseElapsedTimeFromBackend(totalStudiedTimeToday);
    }

    // New constructor with online start time
    public OnlineUser(String user, String domain, String goal, String totalStudiedTimeToday, long onlineStartTime) {
        this.user = user;
        this.domain = domain;
        this.goal = goal;
        this.totalStudiedTimeToday = totalStudiedTimeToday;
        this.onlineStartTime = onlineStartTime;
        this.currentOnlineTime = 0;
        this.baseElapsedTime = parseElapsedTimeFromBackend(totalStudiedTimeToday);
    }

    // Method to parse elapsed time from backend format "04:18:23.323629"
    private long parseElapsedTimeFromBackend(String timeString) {
        if (timeString == null || timeString.isEmpty() || timeString.equals("null")) {
            return 0;
        }

        try {
            // Split by dot to remove microseconds if present
            String mainTime = timeString.split("\\.")[0];
            String[] parts = mainTime.split(":");

            if (parts.length >= 3) {
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                int seconds = Integer.parseInt(parts[2]);

                // Convert to milliseconds
                return (hours * 3600L + minutes * 60L + seconds) * 1000L;
            }
        } catch (NumberFormatException e) {
            // If parsing fails, return 0
            return 0;
        }
        return 0;
    }

    // Getters
    public String getUser() {
        return user;
    }

    public String getDomain() {
        return domain;
    }

    public String getGoal() {
        return goal;
    }

    public String getTotalStudiedTimeToday() {
        return totalStudiedTimeToday;
    }

    public long getOnlineStartTime() {
        return onlineStartTime;
    }

    public long getCurrentOnlineTime() {
        return currentOnlineTime;
    }

    public long getBaseElapsedTime() {
        return baseElapsedTime;
    }

    // Setters
    public void setUser(String user) {
        this.user = user;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public void setTotalStudiedTimeToday(String totalStudiedTimeToday) {
        this.totalStudiedTimeToday = totalStudiedTimeToday;
        // Update base elapsed time when total studied time changes
        this.baseElapsedTime = parseElapsedTimeFromBackend(totalStudiedTimeToday);
    }

    public void setOnlineStartTime(long onlineStartTime) {
        this.onlineStartTime = onlineStartTime;
    }

    public void setCurrentOnlineTime(long currentOnlineTime) {
        this.currentOnlineTime = currentOnlineTime;
    }

    public void setBaseElapsedTime(long baseElapsedTime) {
        this.baseElapsedTime = baseElapsedTime;
    }

    // Helper method to calculate actual elapsed time (base time + time since online start)
    public long calculateElapsedTime() {
        long currentSessionTime = System.currentTimeMillis() - onlineStartTime;
        return baseElapsedTime + currentSessionTime;
    }

    // Helper method to get formatted online time (now shows total elapsed time from backend + current session)
    public String getFormattedOnlineTime() {
        long totalElapsedTime = calculateElapsedTime();
        long totalSeconds = totalElapsedTime / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    // Helper method to format study time today
    public String getFormattedStudyTimeToday() {
        if (totalStudiedTimeToday == null || totalStudiedTimeToday.isEmpty()) {
            return "0h 0m";
        }

        try {
            // Check if it's in HH:MM:SS format
            if (totalStudiedTimeToday.contains(":")) {
                String mainTime = totalStudiedTimeToday.split("\\.")[0];
                String[] parts = mainTime.split(":");

                if (parts.length >= 3) {
                    int hours = Integer.parseInt(parts[0]);
                    int minutes = Integer.parseInt(parts[1]);

                    if (hours > 0) {
                        return hours + "h " + minutes + "m";
                    } else {
                        return minutes + "m";
                    }
                }
            } else {
                // Assuming the time is in minutes
                int totalMinutes = Integer.parseInt(totalStudiedTimeToday);
                int hours = totalMinutes / 60;
                int minutes = totalMinutes % 60;

                if (hours > 0) {
                    return hours + "h " + minutes + "m";
                } else {
                    return minutes + "m";
                }
            }
        } catch (NumberFormatException e) {
            // If parsing fails, return the original string
            return totalStudiedTimeToday;
        }

        return totalStudiedTimeToday;
    }

    @Override
    public String toString() {
        return "OnlineUser{" +
                "user='" + user + '\'' +
                ", domain='" + domain + '\'' +
                ", goal='" + goal + '\'' +
                ", totalStudiedTimeToday='" + totalStudiedTimeToday + '\'' +
                ", onlineTime=" + getFormattedOnlineTime() +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        OnlineUser that = (OnlineUser) obj;
        return user != null ? user.equals(that.user) : that.user == null;
    }

    @Override
    public int hashCode() {
        return user != null ? user.hashCode() : 0;
    }
}