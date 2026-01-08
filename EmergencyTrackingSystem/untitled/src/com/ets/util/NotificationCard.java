package com.ets.util;

import com.ets.model.Notification;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class NotificationCard {
    
    public static VBox createNotificationCard(Notification notification) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(18));
        card.setMaxWidth(700);
        
        // Determine notification type and color
        String typeIcon = getNotificationIcon(notification.getTitle());
        String typeColor = getNotificationColor(notification.getTitle());
        boolean isUnread = !notification.isRead();
        
        // Card styling based on read status
        if (isUnread) {
            card.setStyle("-fx-background-color: #EFF6FF; " +
                    "-fx-background-radius: 12px; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3); " +
                    "-fx-border-radius: 12px; " +
                    "-fx-border-color: #3B82F6; " +
                    "-fx-border-width: 2px;");
        } else {
            card.setStyle("-fx-background-color: white; " +
                    "-fx-background-radius: 12px; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2); " +
                    "-fx-border-radius: 12px; " +
                    "-fx-border-color: #E5E7EB; " +
                    "-fx-border-width: 1px;");
        }
        
        // Add hover effect
        card.setOnMouseEntered(e -> {
            if (isUnread) {
                card.setStyle("-fx-background-color: #DBEAFE; " +
                        "-fx-background-radius: 12px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 4); " +
                        "-fx-border-radius: 12px; " +
                        "-fx-border-color: #3B82F6; " +
                        "-fx-border-width: 2px; " +
                        "-fx-cursor: hand;");
            } else {
                card.setStyle("-fx-background-color: #F9FAFB; " +
                        "-fx-background-radius: 12px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 10, 0, 0, 3); " +
                        "-fx-border-radius: 12px; " +
                        "-fx-border-color: #E5E7EB; " +
                        "-fx-border-width: 1px; " +
                        "-fx-cursor: hand;");
            }
        });
        card.setOnMouseExited(e -> {
            if (isUnread) {
                card.setStyle("-fx-background-color: #EFF6FF; " +
                        "-fx-background-radius: 12px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3); " +
                        "-fx-border-radius: 12px; " +
                        "-fx-border-color: #3B82F6; " +
                        "-fx-border-width: 2px;");
            } else {
                card.setStyle("-fx-background-color: white; " +
                        "-fx-background-radius: 12px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2); " +
                        "-fx-border-radius: 12px; " +
                        "-fx-border-color: #E5E7EB; " +
                        "-fx-border-width: 1px;");
            }
        });
        
        // Header row with icon and title
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        
        // Icon container
        VBox iconContainer = new VBox();
        iconContainer.setAlignment(Pos.CENTER);
        iconContainer.setMinWidth(50);
        iconContainer.setMinHeight(50);
        iconContainer.setStyle("-fx-background-color: " + typeColor + "20; " +
                "-fx-background-radius: 10px; " +
                "-fx-padding: 8px;");
        Label iconLabel = new Label(typeIcon);
        iconLabel.setFont(Font.font(24));
        iconContainer.getChildren().add(iconLabel);
        
        // Title and time
        VBox titleBox = new VBox(4);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label(notification.getTitle());
        titleLabel.setFont(Font.font("Segoe UI", isUnread ? FontWeight.BOLD : FontWeight.SEMI_BOLD, 16));
        titleLabel.setTextFill(Color.web(isUnread ? "#1E40AF" : "#111827"));
        
        // Unread indicator dot
        if (isUnread) {
            Label unreadDot = new Label("●");
            unreadDot.setFont(Font.font(12));
            unreadDot.setTextFill(Color.web("#3B82F6"));
            titleRow.getChildren().add(unreadDot);
        }
        
        titleRow.getChildren().add(titleLabel);
        
        // Time badge
        Label timeLabel = new Label(formatTime(notification.getCreatedAt()));
        timeLabel.setFont(Font.font("Segoe UI", 11));
        timeLabel.setTextFill(Color.web("#6B7280"));
        timeLabel.setStyle("-fx-background-color: #F3F4F6; " +
                "-fx-background-radius: 6px; " +
                "-fx-padding: 4px 10px;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        HBox titleTimeRow = new HBox(10);
        titleTimeRow.setAlignment(Pos.CENTER_LEFT);
        titleTimeRow.getChildren().addAll(titleRow, spacer, timeLabel);
        
        titleBox.getChildren().add(titleTimeRow);
        
        header.getChildren().addAll(iconContainer, titleBox);
        
        // Message content
        Label messageLabel = new Label(notification.getMessage());
        messageLabel.setFont(Font.font("Segoe UI", 14));
        messageLabel.setTextFill(Color.web("#374151"));
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(650);
        messageLabel.setStyle("-fx-padding: 8px 12px; " +
                "-fx-background-color: " + (isUnread ? "#DBEAFE" : "#F9FAFB") + "; " +
                "-fx-background-radius: 8px;");
        
        card.getChildren().addAll(header, messageLabel);
        
        return card;
    }
    
    private static String getNotificationIcon(String title) {
        if (title == null) return "🔔";
        
        String lowerTitle = title.toLowerCase();
        if (lowerTitle.contains("emergency") || lowerTitle.contains("reported")) {
            return "🚨";
        } else if (lowerTitle.contains("accepted")) {
            return "✅";
        } else if (lowerTitle.contains("resolved")) {
            return "✔️";
        } else if (lowerTitle.contains("update")) {
            return "📢";
        } else if (lowerTitle.contains("alert")) {
            return "⚠️";
        } else {
            return "🔔";
        }
    }
    
    private static String getNotificationColor(String title) {
        if (title == null) return "#3B82F6";
        
        String lowerTitle = title.toLowerCase();
        if (lowerTitle.contains("emergency") || lowerTitle.contains("reported")) {
            return "#DC2626"; // Red
        } else if (lowerTitle.contains("accepted")) {
            return "#3B82F6"; // Blue
        } else if (lowerTitle.contains("resolved")) {
            return "#10B981"; // Green
        } else if (lowerTitle.contains("update")) {
            return "#F59E0B"; // Orange
        } else if (lowerTitle.contains("alert")) {
            return "#EF4444"; // Red
        } else {
            return "#3B82F6"; // Default blue
        }
    }
    
    private static String formatTime(Timestamp timestamp) {
        if (timestamp == null) return "Unknown time";
        
        LocalDateTime notificationTime = timestamp.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();
        
        long minutesAgo = java.time.Duration.between(notificationTime, now).toMinutes();
        long hoursAgo = minutesAgo / 60;
        long daysAgo = hoursAgo / 24;
        
        if (daysAgo > 7) {
            return notificationTime.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
        } else if (daysAgo > 0) {
            return daysAgo + " day" + (daysAgo > 1 ? "s" : "") + " ago";
        } else if (hoursAgo > 0) {
            return hoursAgo + " hour" + (hoursAgo > 1 ? "s" : "") + " ago";
        } else if (minutesAgo > 0) {
            return minutesAgo + " minute" + (minutesAgo > 1 ? "s" : "") + " ago";
        } else {
            return "Just now";
        }
    }
    
    public static String getDateGroupLabel(Timestamp timestamp) {
        if (timestamp == null) return "Older";
        
        LocalDate notificationDate = timestamp.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate thisWeek = today.minusDays(7);
        
        if (notificationDate.equals(today)) {
            return "Today";
        } else if (notificationDate.equals(yesterday)) {
            return "Yesterday";
        } else if (notificationDate.isAfter(thisWeek)) {
            return "This Week";
        } else {
            return "Older";
        }
    }
}

