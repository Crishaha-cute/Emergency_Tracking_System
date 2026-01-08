package com.ets.controller;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.application.Platform;
import com.ets.model.User;
import com.ets.model.Emergency;
import com.ets.model.Emergency.EmergencyType;
import com.ets.model.Emergency.EmergencyStatus;
import com.ets.model.Notification;
import com.ets.util.DatabaseConnection;
import com.ets.util.LoadingOverlay;
import com.ets.util.EmergencyCard;
import com.ets.util.NotificationCard;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDashboardController {
    private Stage stage;
    private User currentUser;
    private BorderPane mainLayout;
    private boolean isEditMode = false;
    private TextField editFullNameField, editEmailField, editPhoneField, editEmergencyContactField;
    private PasswordField editPasswordField;
    private Button activeNavButton;

    public UserDashboardController(Stage stage, User user) {
        this.stage = stage;
        this.currentUser = user;
    }

    public void showDashboard() {
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #f5f6fa;");

        showHomePage();
        mainLayout.setLeft(createSidebar());

        StackPane root = new StackPane();
        root.getChildren().add(mainLayout);
        
        Scene scene = new Scene(root);
        
        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), mainLayout);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        
        stage.setScene(scene);
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(220);
        sidebar.setAlignment(Pos.TOP_CENTER);
        sidebar.setPadding(new Insets(20, 15, 20, 15));
        sidebar.setStyle("-fx-background-color: linear-gradient(to bottom, #87CEEB, #5F9EA0); " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 2, 0);");

        // Logo/Header Section
        VBox headerSection = new VBox(8);
        headerSection.setAlignment(Pos.CENTER);
        headerSection.setPadding(new Insets(0, 0, 25, 0));
        
        Label logoIcon = new Label("🚨");
        logoIcon.setFont(Font.font(36));
        
        Label appName = new Label("Emergency\nTracking");
        appName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        appName.setTextFill(Color.web("#1E3A5F"));
        appName.setAlignment(Pos.CENTER);
        appName.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        
        headerSection.getChildren().addAll(logoIcon, appName);
        
        // Navigation Buttons
        Button homeBtn = createSidebarButton("🏠", "Home");
        Button mapBtn = createSidebarButton("🗺️", "Map");
        Button emergenciesBtn = createSidebarButton("📋", "Emergencies");
        Button notificationsBtn = createSidebarButton("🔔", "Notifications");
        Button profileBtn = createSidebarButton("👤", "Profile");

        homeBtn.setOnAction(e -> {
            setActiveButton(homeBtn);
            navigateWithLoading(() -> showHomePage(), "Loading home...");
        });
        mapBtn.setOnAction(e -> {
            setActiveButton(mapBtn);
            navigateWithLoading(() -> showMapPage(), "Loading map...");
        });
        emergenciesBtn.setOnAction(e -> {
            setActiveButton(emergenciesBtn);
            navigateWithLoading(() -> showEmergenciesPage(), "Loading emergencies...");
        });
        notificationsBtn.setOnAction(e -> {
            setActiveButton(notificationsBtn);
            navigateWithLoading(() -> showNotificationsPage(), "Loading notifications...");
        });
        profileBtn.setOnAction(e -> {
            setActiveButton(profileBtn);
            navigateWithLoading(() -> showProfilePage(), "Loading profile...");
        });

        // Set Home as active by default
        setActiveButton(homeBtn);

        // Spacer to push content to top
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // User info at bottom
        VBox userSection = new VBox(5);
        userSection.setAlignment(Pos.CENTER);
        userSection.setPadding(new Insets(15, 0, 0, 0));
        userSection.setStyle("-fx-border-color: rgba(30,58,95,0.3); " +
                "-fx-border-width: 1px 0 0 0;");
        
        Label userName = new Label(currentUser.getFullName());
        userName.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        userName.setTextFill(Color.web("#1E3A5F"));
        userName.setMaxWidth(180);
        userName.setWrapText(true);
        userName.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        
        Label userRole = new Label(currentUser.getRole().toString());
        userRole.setFont(Font.font("Segoe UI", 11));
        userRole.setTextFill(Color.web("#2C5282"));
        
        userSection.getChildren().addAll(userName, userRole);

        sidebar.getChildren().addAll(headerSection, homeBtn, mapBtn, emergenciesBtn, 
                notificationsBtn, profileBtn, spacer, userSection);
        return sidebar;
    }

    private Button createSidebarButton(String icon, String text) {
        Button btn = new Button();
        btn.setPrefWidth(190);
        btn.setPrefHeight(55);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setContentDisplay(ContentDisplay.LEFT);
        btn.setGraphicTextGap(15);
        btn.setPadding(new Insets(0, 0, 0, 20));
        
        HBox content = new HBox(12);
        content.setAlignment(Pos.CENTER_LEFT);
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(20));
        Label textLabel = new Label(text);
        textLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        textLabel.setTextFill(Color.web("#1E3A5F"));
        content.getChildren().addAll(iconLabel, textLabel);
        btn.setGraphic(content);
        
        btn.setStyle("-fx-background-color: transparent; " +
                "-fx-text-fill: #1E3A5F; " +
                "-fx-border-radius: 12px; " +
                "-fx-background-radius: 12px; " +
                "-fx-cursor: hand;");
        
        btn.setOnMouseEntered(e -> {
            if (btn != activeNavButton) {
                btn.setStyle("-fx-background-color: rgba(255,255,255,0.4); " +
                        "-fx-text-fill: #0F172A; " +
                        "-fx-border-radius: 12px; " +
                        "-fx-background-radius: 12px; " +
                        "-fx-cursor: hand;");
            }
        });
        
        btn.setOnMouseExited(e -> {
            if (btn != activeNavButton) {
                btn.setStyle("-fx-background-color: transparent; " +
                        "-fx-text-fill: #1E3A5F; " +
                        "-fx-border-radius: 12px; " +
                        "-fx-background-radius: 12px; " +
                        "-fx-cursor: hand;");
            }
        });
        
        return btn;
    }
    
    private void setActiveButton(Button button) {
        // Reset previous active button
        if (activeNavButton != null) {
            activeNavButton.setStyle("-fx-background-color: transparent; " +
                    "-fx-text-fill: #1E3A5F; " +
                    "-fx-border-radius: 12px; " +
                    "-fx-background-radius: 12px; " +
                    "-fx-cursor: hand;");
            
            // Reset text color in the graphic
            if (activeNavButton.getGraphic() instanceof HBox) {
                HBox graphic = (HBox) activeNavButton.getGraphic();
                for (javafx.scene.Node node : graphic.getChildren()) {
                    if (node instanceof Label) {
                        Label label = (Label) node;
                        if (!label.getText().matches(".*[🏠🗺️📋🔔👤].*")) {
                            label.setTextFill(Color.web("#1E3A5F"));
                        }
                    }
                }
            }
        }
        
        // Set new active button
        activeNavButton = button;
        activeNavButton.setStyle("-fx-background-color: linear-gradient(to right, #1E3A5F, #2C5282); " +
                "-fx-text-fill: white; " +
                "-fx-border-radius: 12px; " +
                "-fx-background-radius: 12px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(30,58,95,0.4), 8, 0, 0, 2);");
        
        // Update text color in the graphic
        if (activeNavButton.getGraphic() instanceof HBox) {
            HBox graphic = (HBox) activeNavButton.getGraphic();
            for (javafx.scene.Node node : graphic.getChildren()) {
                if (node instanceof Label) {
                    Label label = (Label) node;
                    if (!label.getText().matches(".*[🏠🗺️📋🔔👤].*")) {
                        label.setTextFill(Color.WHITE);
                    }
                }
            }
        }
    }
    
    private void navigateWithLoading(Runnable action, String message) {
        if (mainLayout.getParent() instanceof StackPane) {
            StackPane root = (StackPane) mainLayout.getParent();
            LoadingOverlay.showLoadingWithCallback(root, message, action);
        } else {
            // If not in a StackPane, wrap it
            StackPane root = new StackPane();
            root.getChildren().add(mainLayout);
            stage.getScene().setRoot(root);
            LoadingOverlay.showLoadingWithCallback(root, message, action);
        }
    }

    private void showHomePage() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #F8FAFC; " +
                "-fx-background: transparent; " +
                "-fx-border: none;");
        
        VBox content = new VBox(25);
        content.setPadding(new Insets(30, 30, 50, 30));
        content.setStyle("-fx-background-color: #F8FAFC;");

        // Welcome Header Section with gradient background effect
        VBox welcomeSection = new VBox(10);
        welcomeSection.setPadding(new Insets(25, 30, 25, 30));
        welcomeSection.setStyle("-fx-background-color: linear-gradient(to right, #DC2626, #991B1B); " +
                "-fx-background-radius: 16px; " +
                "-fx-effect: dropshadow(gaussian, rgba(220,38,38,0.3), 15, 0, 0, 5);");
        
        HBox welcomeRow = new HBox(15);
        welcomeRow.setAlignment(Pos.CENTER_LEFT);
        
        // Welcome icon
        Label welcomeIcon = new Label("👋");
        welcomeIcon.setFont(Font.font(40));
        
        VBox welcomeText = new VBox(5);
        Label welcomeLabel = new Label("Welcome back!");
        welcomeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        welcomeLabel.setTextFill(Color.WHITE);
        
        Label userNameLabel = new Label(currentUser.getFullName());
        userNameLabel.setFont(Font.font("Segoe UI", 18));
        userNameLabel.setTextFill(Color.web("#FEE2E2"));
        
        welcomeText.getChildren().addAll(welcomeLabel, userNameLabel);
        welcomeRow.getChildren().addAll(welcomeIcon, welcomeText);
        welcomeSection.getChildren().add(welcomeRow);
        
        // Statistics Cards Row
        HBox statsRow = new HBox(15);
        statsRow.setAlignment(Pos.CENTER);
        
        int activeCount = getActiveEmergencyCount();
        int totalCount = getTotalEmergencyCount();
        int resolvedCount = getResolvedEmergencyCount();
        int unreadNotifications = getUnreadNotificationCount();
        
        VBox activeCard = createStatCard("🚨", String.valueOf(activeCount), "Active", "#DC2626", "#FEE2E2");
        VBox totalCard = createStatCard("📊", String.valueOf(totalCount), "Total Reports", "#3B82F6", "#DBEAFE");
        VBox resolvedCard = createStatCard("✅", String.valueOf(resolvedCount), "Resolved", "#10B981", "#D1FAE5");
        VBox notificationCard = createStatCard("🔔", String.valueOf(unreadNotifications), "Unread", "#F59E0B", "#FEF3C7");
        
        statsRow.getChildren().addAll(activeCard, totalCard, resolvedCard, notificationCard);
        
        // SOS Emergency Section
        VBox sosSection = new VBox(15);
        sosSection.setAlignment(Pos.CENTER);
        sosSection.setPadding(new Insets(30));
        sosSection.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 20px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 20, 0, 0, 8);");
        
        Label sosTitle = new Label("Emergency Assistance");
        sosTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        sosTitle.setTextFill(Color.web("#111827"));
        
        Label sosSubtitle = new Label("Tap the button below to report an emergency");
        sosSubtitle.setFont(Font.font("Segoe UI", 14));
        sosSubtitle.setTextFill(Color.web("#6B7280"));
        
        // Enhanced SOS Button with pulse effect
        Button sosButton = new Button("🚨\nSEND SOS");
        sosButton.setPrefSize(260, 260);
        sosButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        sosButton.setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 100%, #DC2626, #991B1B); " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 130; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(220,38,38,0.5), 25, 0, 0, 8);");
        sosButton.setOnMouseEntered(e -> sosButton.setStyle(
                "-fx-background-color: radial-gradient(center 50% 50%, radius 100%, #B91C1C, #7F1D1D); " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 130; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(220,38,38,0.7), 30, 0, 0, 12); " +
                "-fx-scale-x: 1.05; " +
                "-fx-scale-y: 1.05;"));
        sosButton.setOnMouseExited(e -> sosButton.setStyle(
                "-fx-background-color: radial-gradient(center 50% 50%, radius 100%, #DC2626, #991B1B); " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 130; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(220,38,38,0.5), 25, 0, 0, 8); " +
                "-fx-scale-x: 1.0; " +
                "-fx-scale-y: 1.0;"));
        sosButton.setOnAction(e -> showEmergencyDialog());

        sosSection.getChildren().addAll(sosTitle, sosSubtitle, sosButton);
        
        content.getChildren().addAll(welcomeSection, sosSection, statsRow);
        scrollPane.setContent(content);
        mainLayout.setCenter(scrollPane);
    }
    
    private VBox createStatCard(String icon, String value, String label, String color, String bgColor) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(180);
        card.setPrefHeight(140);
        card.setStyle("-fx-background-color: " + bgColor + "; " +
                "-fx-background-radius: 14px; " +
                "-fx-border-color: " + color + "40; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 14px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3);");
        
        // Icon container
        VBox iconContainer = new VBox();
        iconContainer.setAlignment(Pos.CENTER);
        iconContainer.setMinWidth(50);
        iconContainer.setMinHeight(50);
        iconContainer.setStyle("-fx-background-color: " + color + "20; " +
                "-fx-background-radius: 12px; " +
                "-fx-padding: 8px;");
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(28));
        iconContainer.getChildren().add(iconLabel);
        
        // Value
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        valueLabel.setTextFill(Color.web(color));
        
        // Label
        Label labelText = new Label(label);
        labelText.setFont(Font.font("Segoe UI", 13));
        labelText.setTextFill(Color.web("#6B7280"));
        
        card.getChildren().addAll(iconContainer, valueLabel, labelText);
        
        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: " + bgColor + "DD; " +
                "-fx-background-radius: 14px; " +
                "-fx-border-color: " + color + "60; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 14px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 12, 0, 0, 4); " +
                "-fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: " + bgColor + "; " +
                "-fx-background-radius: 14px; " +
                "-fx-border-color: " + color + "40; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 14px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3);"));
        
        return card;
    }
    
    private int getTotalEmergencyCount() {
        String query = "SELECT COUNT(*) FROM emergencies WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, currentUser.getUserId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    private int getResolvedEmergencyCount() {
        String query = "SELECT COUNT(*) FROM emergencies WHERE user_id = ? AND status = 'RESOLVED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, currentUser.getUserId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    private int getUnreadNotificationCount() {
        String query = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = false";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, currentUser.getUserId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void showEmergencyDialog() {
        Dialog<Emergency> dialog = new Dialog<>();
        dialog.setTitle("🚨 Report Emergency");
        dialog.setHeaderText(null);
        dialog.getDialogPane().setStyle("-fx-font-family: 'Segoe UI';");

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #f5f6fa;");

        // Header with icon
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER);
        Label headerIcon = new Label("🚨");
        headerIcon.setFont(Font.font(48));
        VBox headerText = new VBox(5);
        Label headerTitle = new Label("Report Emergency");
        headerTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        headerTitle.setTextFill(Color.web("#2c3e50"));
        Label headerSubtitle = new Label("Provide details about your emergency");
        headerSubtitle.setFont(Font.font("Segoe UI", 13));
        headerSubtitle.setTextFill(Color.web("#7f8c8d"));
        headerText.getChildren().addAll(headerTitle, headerSubtitle);
        header.getChildren().addAll(headerIcon, headerText);
        content.getChildren().add(header);

        // Emergency Type Selection with visual cards
        Label typeLabel = new Label("Select Emergency Type:");
        typeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        typeLabel.setTextFill(Color.web("#2c3e50"));
        
        GridPane typeGrid = new GridPane();
        typeGrid.setHgap(15);
        typeGrid.setVgap(15);
        typeGrid.setAlignment(Pos.CENTER);

        ComboBox<EmergencyType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(EmergencyType.values());
        typeCombo.setValue(EmergencyType.MEDICAL);
        typeCombo.setPrefWidth(400);
        typeCombo.setPrefHeight(50);
        typeCombo.setStyle("-fx-background-color: white; " +
                "-fx-border-color: #dee2e6; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-font-size: 15px; " +
                "-fx-font-family: 'Segoe UI'; " +
                "-fx-padding: 12px;");
        
        // Create visual type selector buttons
        HBox typeButtons = new HBox(10);
        typeButtons.setAlignment(Pos.CENTER);
        for (EmergencyType type : EmergencyType.values()) {
            Button typeBtn = createTypeButton(type);
            typeBtn.setOnAction(e -> typeCombo.setValue(type));
            typeButtons.getChildren().add(typeBtn);
        }

        // Description
        Label descLabel = new Label("Description:");
        descLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        descLabel.setTextFill(Color.web("#2c3e50"));

        TextArea descArea = new TextArea();
        descArea.setPromptText("Describe the emergency situation in detail...");
        descArea.setPrefRowCount(5);
        descArea.setPrefWidth(400);
        descArea.setMaxWidth(400);
        descArea.setFont(Font.font("Segoe UI", 14));
        descArea.setStyle("-fx-background-color: white; " +
                "-fx-border-color: #dee2e6; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-padding: 12px;");

        // Location
        Label locationLabel = new Label("Location/Address:");
        locationLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        locationLabel.setTextFill(Color.web("#2c3e50"));

        TextField addressField = new TextField();
        addressField.setPromptText("Enter location or address");
        addressField.setPrefWidth(400);
        addressField.setPrefHeight(45);
        addressField.setFont(Font.font("Segoe UI", 14));
        addressField.setStyle("-fx-background-color: white; " +
                "-fx-border-color: #dee2e6; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-padding: 12px;");

        // Location info badge
        HBox locationInfoBox = new HBox(10);
        locationInfoBox.setAlignment(Pos.CENTER_LEFT);
        locationInfoBox.setPadding(new Insets(10, 15, 10, 15));
        locationInfoBox.setStyle("-fx-background-color: #e8f4f8; " +
                "-fx-background-radius: 8px; " +
                "-fx-border-color: #3498db; " +
                "-fx-border-width: 1px; " +
                "-fx-border-radius: 8px;");
        Label locationIcon = new Label("📍");
        locationIcon.setFont(Font.font(18));
        Label locationInfo = new Label("Using current location: Tandag, Caraga");
        locationInfo.setFont(Font.font("Segoe UI", 12));
        locationInfo.setTextFill(Color.web("#2980b9"));
        locationInfoBox.getChildren().addAll(locationIcon, locationInfo);

        content.getChildren().addAll(typeLabel, typeButtons, typeCombo, 
                descLabel, descArea, locationLabel, addressField, locationInfoBox);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Style dialog buttons
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: #e74c3c; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 8px 20px; " +
                "-fx-background-radius: 6px;");

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                // Simulate location (in real app, use GPS API)
                double lat = 9.0820 + (Math.random() * 0.01 - 0.005);
                double lng = 126.3061 + (Math.random() * 0.01 - 0.005);

                String address = addressField.getText().isEmpty() ?
                        "Near Tandag City, Surigao del Sur" : addressField.getText();

                Emergency emergency = new Emergency(
                        currentUser.getUserId(),
                        typeCombo.getValue(),
                        descArea.getText(),
                        lat, lng,
                        address
                );

                if (createEmergency(emergency)) {
                    showAlert("Success", "Emergency reported successfully!\n" +
                            "Location: " + String.format("%.6f, %.6f", lat, lng));
                    showHomePage();
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showMapPage() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #F8FAFC;");

        // Header with icon and info
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        VBox headerText = new VBox(5);
        Label title = new Label("🗺️ Map View");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#0F172A"));
        
        Label subtitle = new Label("View your emergency locations on the map");
        subtitle.setFont(Font.font("Segoe UI", 14));
        subtitle.setTextFill(Color.web("#64748B"));
        
        headerText.getChildren().addAll(title, subtitle);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Get user's active emergencies to show on map
        List<Emergency> userEmergencies = getUserEmergencies();

        // Map info badge
        HBox mapInfoBox = new HBox(10);
        mapInfoBox.setAlignment(Pos.CENTER);
        mapInfoBox.setPadding(new Insets(8, 16, 8, 16));
        mapInfoBox.setStyle("-fx-background-color: #EFF6FF; " +
                "-fx-background-radius: 10px; " +
                "-fx-border-color: #3B82F6; " +
                "-fx-border-width: 1.5px; " +
                "-fx-border-radius: 10px;");
        Label mapInfoIcon = new Label("📍");
        mapInfoIcon.setFont(Font.font(16));
        Label mapInfoLabel = new Label(userEmergencies.size() + " emergency location" + 
                (userEmergencies.size() != 1 ? "s" : "") + " shown");
        mapInfoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        mapInfoLabel.setTextFill(Color.web("#1E40AF"));
        mapInfoBox.getChildren().addAll(mapInfoIcon, mapInfoLabel);
        
        header.getChildren().addAll(headerText, spacer, mapInfoBox);

        // Map container with rounded corners
        VBox mapContainer = new VBox();
        mapContainer.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 16px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5); " +
                "-fx-padding: 15px;");

        VBox mapView;
        if (!userEmergencies.isEmpty()) {
            // Show map with user's emergency locations
            mapView = com.ets.view.MapView.createMultiMarkerMap(userEmergencies);
        } else {
            // Show default location
            mapView = com.ets.view.MapView.createDefaultMap();
        }

        mapContainer.getChildren().add(mapView);

        content.getChildren().addAll(header, mapContainer);
        mainLayout.setCenter(content);
    }

    private void showEmergenciesPage() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #f5f6fa;");

        Label title = new Label("My Emergencies");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2c3e50"));

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; " +
                "-fx-background: transparent; " +
                "-fx-border: none;");
        scrollPane.setPrefHeight(480);

        VBox emergencyContainer = new VBox(15);
        emergencyContainer.setPadding(new Insets(10));
        emergencyContainer.setStyle("-fx-background-color: #f5f6fa;");

        List<Emergency> emergencies = getUserEmergencies();

        if (emergencies.isEmpty()) {
            VBox emptyBox = new VBox(20);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(60));
            Label emptyIcon = new Label("📋");
            emptyIcon.setFont(Font.font(64));
            Label emptyText = new Label("No emergencies reported yet");
            emptyText.setFont(Font.font("Segoe UI", 18));
            emptyText.setTextFill(Color.web("#95a5a6"));
            emptyBox.getChildren().addAll(emptyIcon, emptyText);
            emergencyContainer.getChildren().add(emptyBox);
        } else {
        for (Emergency em : emergencies) {
                VBox card = EmergencyCard.createEmergencyCard(em, true);
                emergencyContainer.getChildren().add(card);
            }
        }

        scrollPane.setContent(emergencyContainer);
        content.getChildren().addAll(title, scrollPane);
        mainLayout.setCenter(content);
    }
    
    private Button createTypeButton(EmergencyType type) {
        String icon = switch (type) {
            case MEDICAL -> "🚑";
            case FIRE -> "🔥";
            case CRIME -> "🚔";
            case ACCIDENT -> "⚠️";
        };
        String color = switch (type) {
            case MEDICAL -> "#e74c3c";
            case FIRE -> "#e67e22";
            case CRIME -> "#3498db";
            case ACCIDENT -> "#f1c40f";
        };
        
        Button btn = new Button(icon + "\n" + type.toString());
        btn.setPrefSize(90, 90);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        btn.setStyle("-fx-background-color: " + color + "20; " +
                "-fx-text-fill: " + color + "; " +
                "-fx-border-color: " + color + "; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + color + "40; " +
                "-fx-text-fill: " + color + "; " +
                "-fx-border-color: " + color + "; " +
                "-fx-border-width: 3px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + color + "20; " +
                "-fx-text-fill: " + color + "; " +
                "-fx-border-color: " + color + "; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-cursor: hand;"));
        return btn;
    }

    private void showNotificationsPage() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #F8FAFC;");

        // Header with title and action buttons
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("🔔 Notifications");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#0F172A"));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Mark all as read button
        Button markAllReadBtn = new Button("Mark All as Read");
        markAllReadBtn.setFont(Font.font("Segoe UI", 13));
        markAllReadBtn.setStyle("-fx-background-color: transparent; " +
                "-fx-text-fill: #3B82F6; " +
                "-fx-border-color: #3B82F6; " +
                "-fx-border-width: 1.5px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-padding: 8px 16px; " +
                "-fx-cursor: hand;");
        markAllReadBtn.setOnMouseEntered(e -> markAllReadBtn.setStyle(
                "-fx-background-color: #EFF6FF; " +
                "-fx-text-fill: #2563EB; " +
                "-fx-border-color: #2563EB; " +
                "-fx-border-width: 1.5px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-padding: 8px 16px; " +
                "-fx-cursor: hand;"));
        markAllReadBtn.setOnMouseExited(e -> markAllReadBtn.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-text-fill: #3B82F6; " +
                "-fx-border-color: #3B82F6; " +
                "-fx-border-width: 1.5px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-padding: 8px 16px; " +
                "-fx-cursor: hand;"));
        markAllReadBtn.setOnAction(e -> {
            markAllNotificationsAsRead();
            showNotificationsPage();
        });
        
        header.getChildren().addAll(title, spacer, markAllReadBtn);
        
        // Scrollable notification container
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; " +
                "-fx-background: transparent; " +
                "-fx-border: none;");
        scrollPane.setPrefHeight(480);

        VBox notificationContainer = new VBox(15);
        notificationContainer.setPadding(new Insets(10));
        notificationContainer.setStyle("-fx-background-color: #F8FAFC;");

        List<Notification> notifications = getUserNotifications();

        if (notifications.isEmpty()) {
            VBox emptyBox = new VBox(20);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(80));
            Label emptyIcon = new Label("🔔");
            emptyIcon.setFont(Font.font(64));
            Label emptyTitle = new Label("No notifications");
            emptyTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
            emptyTitle.setTextFill(Color.web("#475569"));
            Label emptyText = new Label("You're all caught up! New notifications will appear here.");
            emptyText.setFont(Font.font("Segoe UI", 14));
            emptyText.setTextFill(Color.web("#94A3B8"));
            emptyBox.getChildren().addAll(emptyIcon, emptyTitle, emptyText);
            notificationContainer.getChildren().add(emptyBox);
        } else {
            // Group notifications by date
            String currentGroup = "";
        for (Notification notif : notifications) {
                String dateGroup = NotificationCard.getDateGroupLabel(notif.getCreatedAt());
                
                // Add date group header if changed
                if (!dateGroup.equals(currentGroup)) {
                    if (!currentGroup.isEmpty()) {
                        // Add spacing between groups
                        Region groupSpacer = new Region();
                        groupSpacer.setPrefHeight(10);
                        notificationContainer.getChildren().add(groupSpacer);
                    }
                    
                    Label groupLabel = new Label(dateGroup);
                    groupLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
                    groupLabel.setTextFill(Color.web("#64748B"));
                    groupLabel.setPadding(new Insets(8, 0, 8, 0));
                    notificationContainer.getChildren().add(groupLabel);
                    currentGroup = dateGroup;
                }
                
                // Add notification card
                VBox card = NotificationCard.createNotificationCard(notif);
                
                // Add click handler to mark as read
                if (!notif.isRead()) {
                    card.setOnMouseClicked(e -> {
                        markNotificationAsRead(notif.getNotificationId());
                        showNotificationsPage();
                    });
                }
                
                notificationContainer.getChildren().add(card);
            }
        }
        
        scrollPane.setContent(notificationContainer);
        content.getChildren().addAll(header, scrollPane);
        mainLayout.setCenter(content);
    }
    
    private void markNotificationAsRead(int notificationId) {
        String query = "UPDATE notifications SET is_read = true WHERE notification_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, notificationId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void markAllNotificationsAsRead() {
        String query = "UPDATE notifications SET is_read = true WHERE user_id = ? AND is_read = false";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, currentUser.getUserId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showProfilePage() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #F8FAFC; " +
                "-fx-background: transparent; " +
                "-fx-border: none;");
        
        VBox content = new VBox(25);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30, 30, 50, 30));
        content.setStyle("-fx-background-color: #F8FAFC;");

        // Profile Header with Avatar
        VBox headerSection = new VBox(15);
        headerSection.setAlignment(Pos.CENTER);
        headerSection.setPadding(new Insets(25, 30, 25, 30));
        headerSection.setStyle("-fx-background-color: linear-gradient(to right, #3B82F6, #2563EB); " +
                "-fx-background-radius: 20px; " +
                "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.3), 15, 0, 0, 5);");
        
        // Avatar circle
        VBox avatarContainer = new VBox();
        avatarContainer.setAlignment(Pos.CENTER);
        avatarContainer.setMinWidth(120);
        avatarContainer.setMinHeight(120);
        avatarContainer.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 60; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);");
        Label avatarIcon = new Label("👤");
        avatarIcon.setFont(Font.font(56));
        avatarContainer.getChildren().add(avatarIcon);
        
        Label profileName = new Label(currentUser.getFullName());
        profileName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        profileName.setTextFill(Color.WHITE);
        
        Label profileRole = new Label(currentUser.getRole().toString());
        profileRole.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        profileRole.setTextFill(Color.web("#DBEAFE"));
        profileRole.setStyle("-fx-background-color: rgba(255,255,255,0.25); " +
                "-fx-background-radius: 12px; " +
                "-fx-padding: 6px 16px;");
        
        headerSection.getChildren().addAll(avatarContainer, profileName, profileRole);

        // Profile Info Card
        VBox infoBox = new VBox(20);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setPadding(new Insets(35));
        infoBox.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 18px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");
        infoBox.setMaxWidth(600);

        // Header with title and edit button
        HBox infoHeader = new HBox(15);
        infoHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label infoTitle = new Label("📋 Account Information");
        infoTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        infoTitle.setTextFill(Color.web("#111827"));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button editBtn = new Button(isEditMode ? "✖ Cancel" : "✏️ Edit Profile");
        editBtn.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        if (isEditMode) {
            editBtn.setStyle("-fx-background-color: #6B7280; " +
                    "-fx-text-fill: white; " +
                    "-fx-border-radius: 10px; " +
                    "-fx-background-radius: 10px; " +
                    "-fx-padding: 10px 20px; " +
                    "-fx-cursor: hand;");
            editBtn.setOnMouseEntered(e -> editBtn.setStyle(
                    "-fx-background-color: #4B5563; " +
                    "-fx-text-fill: white; " +
                    "-fx-border-radius: 10px; " +
                    "-fx-background-radius: 10px; " +
                    "-fx-padding: 10px 20px; " +
                    "-fx-cursor: hand;"));
        } else {
            editBtn.setStyle("-fx-background-color: #3B82F6; " +
                    "-fx-text-fill: white; " +
                    "-fx-border-radius: 10px; " +
                    "-fx-background-radius: 10px; " +
                    "-fx-padding: 10px 20px; " +
                    "-fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.3), 8, 0, 0, 2);");
            editBtn.setOnMouseEntered(e -> editBtn.setStyle(
                    "-fx-background-color: #2563EB; " +
                    "-fx-text-fill: white; " +
                    "-fx-border-radius: 10px; " +
                    "-fx-background-radius: 10px; " +
                    "-fx-padding: 10px 20px; " +
                    "-fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.4), 10, 0, 0, 3);"));
        }
        editBtn.setOnMouseExited(e -> {
            if (isEditMode) {
                editBtn.setStyle("-fx-background-color: #6B7280; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-radius: 10px; " +
                        "-fx-background-radius: 10px; " +
                        "-fx-padding: 10px 20px; " +
                        "-fx-cursor: hand;");
            } else {
                editBtn.setStyle("-fx-background-color: #3B82F6; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-radius: 10px; " +
                        "-fx-background-radius: 10px; " +
                        "-fx-padding: 10px 20px; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.3), 8, 0, 0, 2);");
            }
        });
        editBtn.setOnAction(e -> {
            isEditMode = !isEditMode;
            showProfilePage();
        });
        
        infoHeader.getChildren().addAll(infoTitle, spacer, editBtn);
        VBox.setMargin(infoHeader, new Insets(0, 0, 15, 0));

        if (isEditMode) {
            // Edit Mode - Editable Fields
            editFullNameField = new TextField(currentUser.getFullName());
            editEmailField = new TextField(currentUser.getEmail());
            editPhoneField = new TextField(currentUser.getPhoneNumber());
            editEmergencyContactField = new TextField(
                    currentUser.getEmergencyContact() != null ? currentUser.getEmergencyContact() : "");
            
            VBox fullNameField = createEditableField("👤", "Full Name", editFullNameField);
            VBox emailField = createEditableField("📧", "Email", editEmailField);
            VBox phoneField = createEditableField("📱", "Phone Number", editPhoneField);
            VBox emergencyContactField = createEditableField("🚨", "Emergency Contact", editEmergencyContactField);
            
            // Password field
            VBox passwordField = new VBox(8);
            passwordField.setPadding(new Insets(15));
            passwordField.setStyle("-fx-background-color: #F9FAFB; " +
                    "-fx-background-radius: 12px; " +
                    "-fx-border-color: #E5E7EB; " +
                    "-fx-border-width: 1.5px; " +
                    "-fx-border-radius: 12px;");
            
            HBox passwordHeader = new HBox(10);
            passwordHeader.setAlignment(Pos.CENTER_LEFT);
            Label passwordIcon = new Label("🔒");
            passwordIcon.setFont(Font.font(20));
            Label passwordLabel = new Label("Password");
            passwordLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
            passwordLabel.setTextFill(Color.web("#6B7280"));
            passwordHeader.getChildren().addAll(passwordIcon, passwordLabel);
            
            editPasswordField = new PasswordField();
            editPasswordField.setPromptText("Leave blank to keep current password");
            editPasswordField.setFont(Font.font("Segoe UI", 14));
            editPasswordField.setStyle("-fx-background-color: white; " +
                    "-fx-border-color: #D1D5DB; " +
                    "-fx-border-width: 1.5px; " +
                    "-fx-border-radius: 8px; " +
                    "-fx-padding: 10px 15px;");
            editPasswordField.setOnMouseEntered(e -> editPasswordField.setStyle(
                    "-fx-background-color: white; " +
                    "-fx-border-color: #3B82F6; " +
                    "-fx-border-width: 2px; " +
                    "-fx-border-radius: 8px; " +
                    "-fx-padding: 10px 15px;"));
            editPasswordField.setOnMouseExited(e -> editPasswordField.setStyle(
                    "-fx-background-color: white; " +
                    "-fx-border-color: #D1D5DB; " +
                    "-fx-border-width: 1.5px; " +
                    "-fx-border-radius: 8px; " +
                    "-fx-padding: 10px 15px;"));
            
            passwordField.getChildren().addAll(passwordHeader, editPasswordField);
            
            // Role display (non-editable)
            HBox roleItem = createInfoItem("🎭", "Role", currentUser.getRole().toString());
            roleItem.setStyle(roleItem.getStyle() + " -fx-background-color: #EFF6FF; " +
                    "-fx-border-color: #3B82F6; " +
                    "-fx-border-width: 2px;");
            
            // Save button
            Button saveBtn = new Button("💾 Save Changes");
            saveBtn.setPrefWidth(Double.MAX_VALUE);
            saveBtn.setPrefHeight(50);
            saveBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
            saveBtn.setStyle("-fx-background-color: #10B981; " +
                    "-fx-text-fill: white; " +
                    "-fx-border-radius: 12px; " +
                    "-fx-background-radius: 12px; " +
                    "-fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, rgba(16,185,129,0.4), 12, 0, 0, 4);");
            saveBtn.setOnMouseEntered(e -> saveBtn.setStyle(
                    "-fx-background-color: #059669; " +
                    "-fx-text-fill: white; " +
                    "-fx-border-radius: 12px; " +
                    "-fx-background-radius: 12px; " +
                    "-fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, rgba(16,185,129,0.6), 15, 0, 0, 6); " +
                    "-fx-scale-x: 1.01; " +
                    "-fx-scale-y: 1.01;"));
            saveBtn.setOnMouseExited(e -> saveBtn.setStyle(
                    "-fx-background-color: #10B981; " +
                    "-fx-text-fill: white; " +
                    "-fx-border-radius: 12px; " +
                    "-fx-background-radius: 12px; " +
                    "-fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, rgba(16,185,129,0.4), 12, 0, 0, 4); " +
                    "-fx-scale-x: 1.0; " +
                    "-fx-scale-y: 1.0;"));
            saveBtn.setOnAction(e -> saveProfileChanges());
            
            infoBox.getChildren().addAll(infoHeader, fullNameField, emailField, 
                    phoneField, emergencyContactField, passwordField, roleItem, saveBtn);
        } else {
            // View Mode - Display Only
            HBox nameItem = createInfoItem("👤", "Full Name", currentUser.getFullName());
            HBox emailItem = createInfoItem("📧", "Email", currentUser.getEmail());
            HBox phoneItem = createInfoItem("📱", "Phone Number", currentUser.getPhoneNumber());
            HBox usernameItem = createInfoItem("🔑", "Username", currentUser.getUsername());
            HBox emergencyContactItem = createInfoItem("🚨", "Emergency Contact", 
                    currentUser.getEmergencyContact() != null && !currentUser.getEmergencyContact().isEmpty() 
                            ? currentUser.getEmergencyContact() : "Not set");
            HBox roleItem = createInfoItem("🎭", "Role", currentUser.getRole().toString());
            roleItem.setStyle(roleItem.getStyle() + " -fx-background-color: #EFF6FF; " +
                    "-fx-border-color: #3B82F6; " +
                    "-fx-border-width: 2px;");

            infoBox.getChildren().addAll(infoHeader, nameItem, emailItem, phoneItem, 
                    usernameItem, emergencyContactItem, roleItem);
        }

        // Action Buttons Row
        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER);
        
        // Change Password Button (only in view mode)
        if (!isEditMode) {
            Button changePasswordBtn = new Button("🔒 Change Password");
            changePasswordBtn.setPrefWidth(200);
            changePasswordBtn.setPrefHeight(50);
            changePasswordBtn.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
            changePasswordBtn.setStyle("-fx-background-color: white; " +
                    "-fx-text-fill: #3B82F6; " +
                    "-fx-border-color: #3B82F6; " +
                    "-fx-border-width: 2px; " +
                    "-fx-border-radius: 12px; " +
                    "-fx-background-radius: 12px; " +
                    "-fx-cursor: hand;");
            changePasswordBtn.setOnMouseEntered(e -> changePasswordBtn.setStyle(
                    "-fx-background-color: #EFF6FF; " +
                    "-fx-text-fill: #2563EB; " +
                    "-fx-border-color: #2563EB; " +
                    "-fx-border-width: 2px; " +
                    "-fx-border-radius: 12px; " +
                    "-fx-background-radius: 12px; " +
                    "-fx-cursor: hand;"));
            changePasswordBtn.setOnMouseExited(e -> changePasswordBtn.setStyle(
                    "-fx-background-color: white; " +
                    "-fx-text-fill: #3B82F6; " +
                    "-fx-border-color: #3B82F6; " +
                    "-fx-border-width: 2px; " +
                    "-fx-border-radius: 12px; " +
                    "-fx-background-radius: 12px; " +
                    "-fx-cursor: hand;"));
            changePasswordBtn.setOnAction(e -> {
                isEditMode = true;
                showProfilePage();
            });
            actionButtons.getChildren().add(changePasswordBtn);
        }
        
        // Logout Button
        Button logoutBtn = new Button("🚪 Logout");
        logoutBtn.setPrefWidth(200);
        logoutBtn.setPrefHeight(50);
        logoutBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        logoutBtn.setStyle("-fx-background-color: #DC2626; " +
                "-fx-text-fill: white; " +
                "-fx-border-radius: 12px; " +
                "-fx-background-radius: 12px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(220,38,38,0.4), 12, 0, 0, 4);");
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle(
                "-fx-background-color: #B91C1C; " +
                "-fx-text-fill: white; " +
                "-fx-border-radius: 12px; " +
                "-fx-background-radius: 12px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(220,38,38,0.6), 15, 0, 0, 6); " +
                "-fx-scale-x: 1.02; " +
                "-fx-scale-y: 1.02;"));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle(
                "-fx-background-color: #DC2626; " +
                "-fx-text-fill: white; " +
                "-fx-border-radius: 12px; " +
                "-fx-background-radius: 12px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(220,38,38,0.4), 12, 0, 0, 4); " +
                "-fx-scale-x: 1.0; " +
                "-fx-scale-y: 1.0;"));
        logoutBtn.setOnAction(e -> {
            new AuthController(stage).showLoginPage();
        });
        actionButtons.getChildren().add(logoutBtn);

        content.getChildren().addAll(headerSection, infoBox, actionButtons);
        scrollPane.setContent(content);
        mainLayout.setCenter(scrollPane);
    }
    
    private VBox createEditableField(String icon, String label, TextField textField) {
        VBox field = new VBox(8);
        field.setPadding(new Insets(15));
        field.setStyle("-fx-background-color: #F9FAFB; " +
                "-fx-background-radius: 12px; " +
                "-fx-border-color: #E5E7EB; " +
                "-fx-border-width: 1.5px; " +
                "-fx-border-radius: 12px;");
        
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(20));
        Label labelText = new Label(label);
        labelText.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        labelText.setTextFill(Color.web("#6B7280"));
        header.getChildren().addAll(iconLabel, labelText);
        
        textField.setFont(Font.font("Segoe UI", 14));
        textField.setStyle("-fx-background-color: white; " +
                "-fx-border-color: #D1D5DB; " +
                "-fx-border-width: 1.5px; " +
                "-fx-border-radius: 8px; " +
                "-fx-padding: 10px 15px;");
        textField.setOnMouseEntered(e -> textField.setStyle(
                "-fx-background-color: white; " +
                "-fx-border-color: #3B82F6; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 8px; " +
                "-fx-padding: 10px 15px;"));
        textField.setOnMouseExited(e -> textField.setStyle(
                "-fx-background-color: white; " +
                "-fx-border-color: #D1D5DB; " +
                "-fx-border-width: 1.5px; " +
                "-fx-border-radius: 8px; " +
                "-fx-padding: 10px 15px;"));
        
        field.getChildren().addAll(header, textField);
        return field;
    }
    
    private void saveProfileChanges() {
        String fullName = editFullNameField.getText().trim();
        String email = editEmailField.getText().trim();
        String phone = editPhoneField.getText().trim();
        String emergencyContact = editEmergencyContactField.getText().trim();
        String password = editPasswordField.getText().trim();
        
        // Validation
        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            showAlert("Validation Error", "Full Name, Email, and Phone Number are required fields!");
            return;
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert("Validation Error", "Please enter a valid email address!");
            return;
        }
        
        navigateWithLoading(() -> {
            try {
                String query;
                
                if (password.isEmpty()) {
                    // Update without password
                    query = "UPDATE users SET full_name = ?, email = ?, phone_number = ?, emergency_contact = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement ps = conn.prepareStatement(query)) {
                        ps.setString(1, fullName);
                        ps.setString(2, email);
                        ps.setString(3, phone);
                        ps.setString(4, emergencyContact.isEmpty() ? null : emergencyContact);
                        ps.setInt(5, currentUser.getUserId());
                        
                        int rowsAffected = ps.executeUpdate();
                        if (rowsAffected > 0) {
                            // Update current user object
                            currentUser.setFullName(fullName);
                            currentUser.setEmail(email);
                            currentUser.setPhoneNumber(phone);
                            currentUser.setEmergencyContact(emergencyContact.isEmpty() ? null : emergencyContact);
                            
                            Platform.runLater(() -> {
                                showAlert("Success", "Profile updated successfully!");
                                isEditMode = false;
                                showProfilePage();
                            });
                        } else {
                            Platform.runLater(() -> {
                                showAlert("Error", "Failed to update profile. Please try again.");
                            });
                        }
                    }
                } else {
                    // Update with password
                    query = "UPDATE users SET full_name = ?, email = ?, phone_number = ?, emergency_contact = ?, password = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement ps = conn.prepareStatement(query)) {
                        ps.setString(1, fullName);
                        ps.setString(2, email);
                        ps.setString(3, phone);
                        ps.setString(4, emergencyContact.isEmpty() ? null : emergencyContact);
                        ps.setString(5, password);
                        ps.setInt(6, currentUser.getUserId());
                        
                        int rowsAffected = ps.executeUpdate();
                        if (rowsAffected > 0) {
                            // Update current user object
                            currentUser.setFullName(fullName);
                            currentUser.setEmail(email);
                            currentUser.setPhoneNumber(phone);
                            currentUser.setEmergencyContact(emergencyContact.isEmpty() ? null : emergencyContact);
                            currentUser.setPassword(password);
                            
                            Platform.runLater(() -> {
                                showAlert("Success", "Profile and password updated successfully!");
                                isEditMode = false;
                                showProfilePage();
                            });
                        } else {
                            Platform.runLater(() -> {
                                showAlert("Error", "Failed to update profile. Please try again.");
                            });
                        }
                    }
                }
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    showAlert("Database Error", "An error occurred while updating your profile: " + e.getMessage());
                });
                e.printStackTrace();
            }
        }, "Saving changes...");
    }
    
    private HBox createInfoItem(String icon, String label, String value) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12, 15, 12, 15));
        item.setStyle("-fx-background-color: #F9FAFB; " +
                "-fx-background-radius: 10px; " +
                "-fx-border-color: #E5E7EB; " +
                "-fx-border-width: 1px; " +
                "-fx-border-radius: 10px;");
        
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(20));
        
        VBox textBox = new VBox(3);
        Label labelText = new Label(label);
        labelText.setFont(Font.font("Segoe UI", 11));
        labelText.setTextFill(Color.web("#6B7280"));
        
        Label valueText = new Label(value);
        valueText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        valueText.setTextFill(Color.web("#111827"));
        
        textBox.getChildren().addAll(labelText, valueText);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        item.getChildren().addAll(iconLabel, textBox, spacer);
        return item;
    }

    private boolean createEmergency(Emergency emergency) {
        String query = "INSERT INTO emergencies (user_id, emergency_type, description, latitude, longitude, location_address, status) VALUES (?, ?, ?, ?, ?, ?, 'PENDING')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, emergency.getUserId());
            stmt.setString(2, emergency.getEmergencyType().toString());
            stmt.setString(3, emergency.getDescription());
            stmt.setDouble(4, emergency.getLatitude());
            stmt.setDouble(5, emergency.getLongitude());
            stmt.setString(6, emergency.getLocationAddress());

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int emergencyId = rs.getInt(1);
                createNotification(currentUser.getUserId(), emergencyId,
                        "Emergency Reported", "Your " + emergency.getEmergencyType() + " emergency has been reported.");
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void createNotification(int userId, int emergencyId, String title, String message) {
        String query = "INSERT INTO notifications (user_id, emergency_id, title, message) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, emergencyId);
            stmt.setString(3, title);
            stmt.setString(4, message);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getActiveEmergencyCount() {
        String query = "SELECT COUNT(*) FROM emergencies WHERE user_id = ? AND status != 'RESOLVED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentUser.getUserId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private List<Emergency> getUserEmergencies() {
        List<Emergency> emergencies = new ArrayList<>();
        String query = "SELECT * FROM emergencies WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentUser.getUserId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Emergency em = new Emergency();
                em.setEmergencyId(rs.getInt("emergency_id"));
                em.setUserId(rs.getInt("user_id"));
                em.setEmergencyType(EmergencyType.valueOf(rs.getString("emergency_type")));
                em.setStatus(EmergencyStatus.valueOf(rs.getString("status")));
                em.setDescription(rs.getString("description"));
                em.setLatitude(rs.getDouble("latitude"));
                em.setLongitude(rs.getDouble("longitude"));
                em.setLocationAddress(rs.getString("location_address"));
                em.setCreatedAt(rs.getTimestamp("created_at"));
                emergencies.add(em);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return emergencies;
    }

    private List<Notification> getUserNotifications() {
        List<Notification> notifications = new ArrayList<>();
        String query = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT 20";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentUser.getUserId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Notification notif = new Notification();
                notif.setNotificationId(rs.getInt("notification_id"));
                notif.setUserId(rs.getInt("user_id"));
                notif.setEmergencyId(rs.getInt("emergency_id"));
                notif.setTitle(rs.getString("title"));
                notif.setMessage(rs.getString("message"));
                notif.setRead(rs.getBoolean("is_read"));
                notif.setCreatedAt(rs.getTimestamp("created_at"));
                notifications.add(notif);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifications;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}