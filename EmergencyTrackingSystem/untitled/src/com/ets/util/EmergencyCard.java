package com.ets.util;

import com.ets.model.Emergency;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class EmergencyCard {
    
    public static VBox createEmergencyCard(Emergency emergency, boolean isUserView) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setMaxWidth(600);
        card.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 12px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3); " +
                "-fx-border-radius: 12px;");
        
        // Add hover effect
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: #f8f9fa; " +
                "-fx-background-radius: 12px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 15, 0, 0, 5); " +
                "-fx-border-radius: 12px; " +
                "-fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white; " +
                "-fx-background-radius: 12px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3); " +
                "-fx-border-radius: 12px;"));
        
        // Header with type and status
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        // Emergency type icon and label
        String typeIcon = getEmergencyTypeIcon(emergency.getEmergencyType());
        String typeColor = getEmergencyTypeColor(emergency.getEmergencyType());
        
        Label typeIconLabel = new Label(typeIcon);
        typeIconLabel.setFont(Font.font(36));
        
        VBox typeInfo = new VBox(3);
        Label typeLabel = new Label(emergency.getEmergencyType().toString());
        typeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        typeLabel.setTextFill(Color.web(typeColor));
        
        Label statusLabel = createStatusBadge(emergency.getStatus());
        typeInfo.getChildren().addAll(typeLabel, statusLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Time badge
        Label timeLabel = new Label(formatTime(emergency.getCreatedAt()));
        timeLabel.setFont(Font.font("Segoe UI", 11));
        timeLabel.setTextFill(Color.web("#7f8c8d"));
        timeLabel.setStyle("-fx-background-color: #ecf0f1; " +
                "-fx-background-radius: 6px; " +
                "-fx-padding: 4px 10px;");
        
        header.getChildren().addAll(typeIconLabel, typeInfo, spacer, timeLabel);
        
        // Description section
        if (emergency.getDescription() != null && !emergency.getDescription().isEmpty()) {
            Label descLabel = new Label(emergency.getDescription());
            descLabel.setFont(Font.font("Segoe UI", 14));
            descLabel.setTextFill(Color.web("#34495e"));
            descLabel.setWrapText(true);
            descLabel.setMaxWidth(560);
            descLabel.setStyle("-fx-padding: 10px; " +
                    "-fx-background-color: #f8f9fa; " +
                    "-fx-background-radius: 8px;");
            card.getChildren().add(descLabel);
        }
        
        // Location section with icon
        HBox locationBox = new HBox(10);
        locationBox.setAlignment(Pos.CENTER_LEFT);
        Label locationIcon = new Label("📍");
        locationIcon.setFont(Font.font(16));
        Label locationLabel = new Label(emergency.getLocationAddress() != null ? 
                emergency.getLocationAddress() : "Location not specified");
        locationLabel.setFont(Font.font("Segoe UI", 13));
        locationLabel.setTextFill(Color.web("#7f8c8d"));
        locationBox.getChildren().addAll(locationIcon, locationLabel);
        
        // User/Responder info
        if (!isUserView && emergency.getUserName() != null) {
            HBox userBox = new HBox(10);
            userBox.setAlignment(Pos.CENTER_LEFT);
            Label userIcon = new Label("👤");
            userIcon.setFont(Font.font(16));
            Label userLabel = new Label("Reported by: " + emergency.getUserName());
            userLabel.setFont(Font.font("Segoe UI", 13));
            userLabel.setTextFill(Color.web("#34495e"));
            userBox.getChildren().addAll(userIcon, userLabel);
            card.getChildren().add(userBox);
        }
        
        card.getChildren().addAll(header, locationBox);
        
        return card;
    }
    
    public static HBox createEmergencyListItem(Emergency emergency, boolean showActions) {
        HBox item = new HBox(15);
        item.setPadding(new Insets(15));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 10px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");
        
        // Add hover effect
        item.setOnMouseEntered(e -> item.setStyle(
                "-fx-background-color: #f8f9fa; " +
                "-fx-background-radius: 10px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3); " +
                "-fx-cursor: hand;"));
        item.setOnMouseExited(e -> item.setStyle(
                "-fx-background-color: white; " +
                "-fx-background-radius: 10px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);"));
        
        // Type icon
        String typeIcon = getEmergencyTypeIcon(emergency.getEmergencyType());
        String typeColor = getEmergencyTypeColor(emergency.getEmergencyType());
        
        VBox iconBox = new VBox();
        iconBox.setAlignment(Pos.CENTER);
        iconBox.setMinWidth(60);
        iconBox.setStyle("-fx-background-color: " + typeColor + "20; " +
                "-fx-background-radius: 8px; " +
                "-fx-padding: 10px;");
        Label iconLabel = new Label(typeIcon);
        iconLabel.setFont(Font.font(28));
        iconBox.getChildren().add(iconLabel);
        
        // Info section
        VBox infoBox = new VBox(8);
        infoBox.setPrefWidth(400);
        
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label typeLabel = new Label(emergency.getEmergencyType().toString());
        typeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        typeLabel.setTextFill(Color.web(typeColor));
        Label statusBadge = createStatusBadge(emergency.getStatus());
        titleRow.getChildren().addAll(typeLabel, statusBadge);
        
        if (emergency.getUserName() != null) {
            Label userLabel = new Label("👤 " + emergency.getUserName());
            userLabel.setFont(Font.font("Segoe UI", 13));
            userLabel.setTextFill(Color.web("#7f8c8d"));
            infoBox.getChildren().add(userLabel);
        }
        
        if (emergency.getLocationAddress() != null) {
            Label locationLabel = new Label("📍 " + emergency.getLocationAddress());
            locationLabel.setFont(Font.font("Segoe UI", 12));
            locationLabel.setTextFill(Color.web("#95a5a6"));
            locationLabel.setWrapText(true);
            infoBox.getChildren().add(locationLabel);
        }
        
        Label timeLabel = new Label("🕐 " + formatTime(emergency.getCreatedAt()));
        timeLabel.setFont(Font.font("Segoe UI", 11));
        timeLabel.setTextFill(Color.web("#bdc3c7"));
        infoBox.getChildren().addAll(titleRow, timeLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        item.getChildren().addAll(iconBox, infoBox, spacer);
        
        return item;
    }
    
    private static Label createStatusBadge(Emergency.EmergencyStatus status) {
        Label badge = new Label();
        badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        badge.setPadding(new Insets(4, 10, 4, 10));
        badge.setStyle("-fx-background-radius: 12px; " +
                "-fx-border-radius: 12px;");
        
        switch (status) {
            case PENDING:
                badge.setText("⏳ PENDING");
                badge.setTextFill(Color.WHITE);
                badge.setStyle(badge.getStyle() + " -fx-background-color: #f39c12;");
                break;
            case ACCEPTED:
                badge.setText("✅ ACCEPTED");
                badge.setTextFill(Color.WHITE);
                badge.setStyle(badge.getStyle() + " -fx-background-color: #3498db;");
                break;
            case RESOLVED:
                badge.setText("✔️ RESOLVED");
                badge.setTextFill(Color.WHITE);
                badge.setStyle(badge.getStyle() + " -fx-background-color: #27ae60;");
                break;
        }
        
        return badge;
    }
    
    private static String getEmergencyTypeIcon(Emergency.EmergencyType type) {
        return switch (type) {
            case MEDICAL -> "🚑";
            case FIRE -> "🔥";
            case CRIME -> "🚔";
            case ACCIDENT -> "⚠️";
        };
    }
    
    private static String getEmergencyTypeColor(Emergency.EmergencyType type) {
        return switch (type) {
            case MEDICAL -> "#e74c3c";
            case FIRE -> "#e67e22";
            case CRIME -> "#3498db";
            case ACCIDENT -> "#f1c40f";
        };
    }
    
    private static String formatTime(java.sql.Timestamp timestamp) {
        if (timestamp == null) return "Unknown time";
        long diff = System.currentTimeMillis() - timestamp.getTime();
        long minutes = diff / 60000;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) return days + " day" + (days > 1 ? "s" : "") + " ago";
        if (hours > 0) return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        if (minutes > 0) return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        return "Just now";
    }
}

