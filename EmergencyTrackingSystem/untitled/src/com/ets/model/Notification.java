package com.ets.model;

import java.sql.Timestamp;

public class Notification {
    private int notificationId;
    private int userId;
    private Integer emergencyId;
    private String title;
    private String message;
    private boolean isRead;
    private Timestamp createdAt;

    // Constructors
    public Notification() {}

    public Notification(int userId, Integer emergencyId, String title, String message) {
        this.userId = userId;
        this.emergencyId = emergencyId;
        this.title = title;
        this.message = message;
        this.isRead = false;
    }

    // Getters and Setters
    public int getNotificationId() { return notificationId; }
    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Integer getEmergencyId() { return emergencyId; }
    public void setEmergencyId(Integer emergencyId) { this.emergencyId = emergencyId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean isRead) { this.isRead = isRead; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}