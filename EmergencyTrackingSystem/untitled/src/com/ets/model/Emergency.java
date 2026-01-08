package com.ets.model;

import java.sql.Timestamp;

public class Emergency {
    private int emergencyId;
    private int userId;
    private Integer responderId;
    private EmergencyType emergencyType;
    private EmergencyStatus status;
    private String description;
    private double latitude;
    private double longitude;
    private String locationAddress;
    private Timestamp createdAt;
    private Timestamp acceptedAt;
    private Timestamp resolvedAt;

    // For display purposes
    private String userName;
    private String responderName;

    public enum EmergencyType {
        MEDICAL, FIRE, CRIME, ACCIDENT
    }

    public enum EmergencyStatus {
        PENDING, ACCEPTED, RESOLVED
    }

    // Constructors
    public Emergency() {}

    public Emergency(int userId, EmergencyType emergencyType, String description,
                     double latitude, double longitude, String locationAddress) {
        this.userId = userId;
        this.emergencyType = emergencyType;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationAddress = locationAddress;
        this.status = EmergencyStatus.PENDING;
    }

    // Getters and Setters
    public int getEmergencyId() { return emergencyId; }
    public void setEmergencyId(int emergencyId) { this.emergencyId = emergencyId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Integer getResponderId() { return responderId; }
    public void setResponderId(Integer responderId) { this.responderId = responderId; }

    public EmergencyType getEmergencyType() { return emergencyType; }
    public void setEmergencyType(EmergencyType emergencyType) {
        this.emergencyType = emergencyType;
    }

    public EmergencyStatus getStatus() { return status; }
    public void setStatus(EmergencyStatus status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getLocationAddress() { return locationAddress; }
    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(Timestamp acceptedAt) { this.acceptedAt = acceptedAt; }

    public Timestamp getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Timestamp resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getResponderName() { return responderName; }
    public void setResponderName(String responderName) { this.responderName = responderName; }
}