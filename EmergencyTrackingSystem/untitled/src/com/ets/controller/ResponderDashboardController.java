package com.ets.controller;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.ets.model.User;
import com.ets.model.Emergency;
import com.ets.model.Emergency.EmergencyStatus;
import com.ets.model.Emergency.EmergencyType;
import com.ets.model.Notification;
import com.ets.util.DatabaseConnection;
import com.ets.util.LoadingOverlay;
import com.ets.util.EmergencyCard;
import com.ets.util.NotificationCard;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResponderDashboardController {
    private Stage stage;
    private User currentUser;
    private BorderPane mainLayout;
    private Button activeNavButton;

    public ResponderDashboardController(Stage stage, User user) {
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
        
        int pendingCount = getPendingEmergencyCount();
        int activeCount = getMyActiveCount();
        int resolvedCount = getMyResolvedCount();
        
        VBox pendingCard = createStatCard("⏳", String.valueOf(pendingCount), "Pending", "#F59E0B", "#FEF3C7");
        VBox activeCard = createStatCard("🚨", String.valueOf(activeCount), "My Active", "#DC2626", "#FEE2E2");
        VBox resolvedCard = createStatCard("✅", String.valueOf(resolvedCount), "Resolved", "#10B981", "#D1FAE5");

        statsRow.getChildren().addAll(pendingCard, activeCard, resolvedCard);

        // Active emergencies list
        Label emergenciesTitle = new Label("Active Emergency Requests");
        emergenciesTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        emergenciesTitle.setTextFill(Color.web("#111827"));

        ScrollPane emergencyScrollPane = new ScrollPane();
        emergencyScrollPane.setFitToWidth(true);
        emergencyScrollPane.setStyle("-fx-background-color: transparent; " +
                "-fx-background: transparent; " +
                "-fx-border: none;");
        emergencyScrollPane.setPrefHeight(400);

        VBox emergencyContainer = new VBox(15);
        emergencyContainer.setPadding(new Insets(10));
        emergencyContainer.setStyle("-fx-background-color: #F8FAFC;");

        List<Emergency> emergencies = getPendingEmergencies();
        
        if (emergencies.isEmpty()) {
            VBox emptyBox = new VBox(20);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(60));
            Label emptyIcon = new Label("✅");
            emptyIcon.setFont(Font.font(64));
            Label emptyText = new Label("No pending emergencies");
            emptyText.setFont(Font.font("Segoe UI", 18));
            emptyText.setTextFill(Color.web("#95a5a6"));
            emptyBox.getChildren().addAll(emptyIcon, emptyText);
            emergencyContainer.getChildren().add(emptyBox);
        } else {
            for (Emergency em : emergencies) {
                VBox card = EmergencyCard.createEmergencyCard(em, false);
                
                // Add action buttons
                HBox buttonBox = new HBox(10);
                buttonBox.setAlignment(Pos.CENTER_RIGHT);
                buttonBox.setPadding(new Insets(10, 0, 0, 0));
                
                Button acceptBtn = new Button("✅ Accept");
                acceptBtn.setPrefWidth(120);
                acceptBtn.setPrefHeight(40);
                acceptBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                acceptBtn.setStyle("-fx-background-color: #27ae60; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-radius: 8px; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(39,174,96,0.3), 5, 0, 0, 2);");
                acceptBtn.setOnMouseEntered(e -> acceptBtn.setStyle(
                        "-fx-background-color: #229954; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-radius: 8px; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(39,174,96,0.5), 8, 0, 0, 3);"));
                acceptBtn.setOnMouseExited(e -> acceptBtn.setStyle(
                        "-fx-background-color: #27ae60; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-radius: 8px; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(39,174,96,0.3), 5, 0, 0, 2);"));
                acceptBtn.setOnAction(e -> {
                    acceptEmergency(em.getEmergencyId());
                    showHomePage();
                });
                
                Button viewMapBtn = new Button("🗺️ View Map");
                viewMapBtn.setPrefWidth(120);
                viewMapBtn.setPrefHeight(40);
                viewMapBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                viewMapBtn.setStyle("-fx-background-color: #3498db; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-radius: 8px; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(52,152,219,0.3), 5, 0, 0, 2);");
                viewMapBtn.setOnMouseEntered(e -> viewMapBtn.setStyle(
                        "-fx-background-color: #2980b9; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-radius: 8px; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(52,152,219,0.5), 8, 0, 0, 3);"));
                viewMapBtn.setOnMouseExited(e -> viewMapBtn.setStyle(
                        "-fx-background-color: #3498db; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-radius: 8px; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(52,152,219,0.3), 5, 0, 0, 2);"));
                viewMapBtn.setOnAction(e -> showEmergencyMap(em));
                
                buttonBox.getChildren().addAll(acceptBtn, viewMapBtn);
                card.getChildren().add(buttonBox);
                emergencyContainer.getChildren().add(card);
            }
        }
        
        emergencyScrollPane.setContent(emergencyContainer);

        content.getChildren().addAll(welcomeSection, statsRow, emergenciesTitle, emergencyScrollPane);
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
        
        Label subtitle = new Label("View emergency locations on the map");
        subtitle.setFont(Font.font("Segoe UI", 14));
        subtitle.setTextFill(Color.web("#64748B"));
        
        headerText.getChildren().addAll(title, subtitle);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Get all pending emergencies
        List<Emergency> emergencies = getPendingEmergencies();

        VBox mapView;
        if (!emergencies.isEmpty()) {
            // Show map with all emergency locations
            mapView = com.ets.view.MapView.createMultiMarkerMap(emergencies);
        } else {
            Label noData = new Label("No active emergencies to display on map");
            noData.setFont(Font.font("Arial", 14));
            noData.setStyle("-fx-text-fill: #7f8c8d;");
            VBox emptyBox = new VBox(20, noData);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPrefHeight(400);
            mapView = emptyBox;
        }

        content.getChildren().addAll(title, mapView);
        mainLayout.setCenter(content);
    }

    private void showEmergencyMap(Emergency em) {
        // Create a new stage for the map popup
        Stage mapStage = new Stage();
        mapStage.setTitle("🗺️ Emergency Location - " + em.getEmergencyType());

        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #f5f6fa;");

        // Use the visual emergency card
        VBox infoBox = EmergencyCard.createEmergencyCard(em, false);
        
        // Add coordinates info
        HBox coordBox = new HBox(10);
        coordBox.setAlignment(Pos.CENTER_LEFT);
        coordBox.setPadding(new Insets(10, 15, 10, 15));
        coordBox.setStyle("-fx-background-color: #e8f4f8; " +
                "-fx-background-radius: 8px; " +
                "-fx-border-color: #3498db; " +
                "-fx-border-width: 1px; " +
                "-fx-border-radius: 8px;");
        Label coordIcon = new Label("📍");
        coordIcon.setFont(Font.font(16));
        Label coordLabel = new Label(String.format("Coordinates: %.6f, %.6f", em.getLatitude(), em.getLongitude()));
        coordLabel.setFont(Font.font("Segoe UI", 12));
        coordLabel.setTextFill(Color.web("#2980b9"));
        coordBox.getChildren().addAll(coordIcon, coordLabel);
        infoBox.getChildren().add(coordBox);

        // Map view
        VBox mapView = com.ets.view.MapView.createSingleMarkerMap(
                em.getLatitude(),
                em.getLongitude(),
                em.getEmergencyType() + " - " + em.getUserName()
        );

        // Close button
        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14px;");
        closeBtn.setPrefWidth(150);
        closeBtn.setOnAction(e -> mapStage.close());

        HBox buttonBox = new HBox(closeBtn);
        buttonBox.setAlignment(Pos.CENTER);

        container.getChildren().addAll(infoBox, mapView, buttonBox);

        Scene scene = new Scene(container, 850, 650);
        mapStage.setScene(scene);
        mapStage.show();
    }

    private void showEmergenciesPage() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #f5f6fa;");

        Label title = new Label("My Emergencies");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#111827"));

        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-font-family: 'Segoe UI';");

        // Active tab
        Tab activeTab = new Tab("🟢 Active");
        activeTab.setClosable(false);
        
        ScrollPane activeScroll = new ScrollPane();
        activeScroll.setFitToWidth(true);
        activeScroll.setStyle("-fx-background-color: transparent;");
        
        VBox activeContainer = new VBox(15);
        activeContainer.setPadding(new Insets(10));
        activeContainer.setStyle("-fx-background-color: #f5f6fa;");
        
        List<Emergency> activeEmergencies = getMyActiveEmergencies();
        if (activeEmergencies.isEmpty()) {
            VBox emptyBox = new VBox(20);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(60));
            Label emptyIcon = new Label("✅");
            emptyIcon.setFont(Font.font(48));
            Label emptyText = new Label("No active emergencies");
            emptyText.setFont(Font.font("Segoe UI", 16));
            emptyText.setTextFill(Color.web("#95a5a6"));
            emptyBox.getChildren().addAll(emptyIcon, emptyText);
            activeContainer.getChildren().add(emptyBox);
        } else {
            for (Emergency em : activeEmergencies) {
                VBox card = EmergencyCard.createEmergencyCard(em, false);
                
                HBox buttonBox = new HBox(10);
                buttonBox.setAlignment(Pos.CENTER_RIGHT);
                buttonBox.setPadding(new Insets(10, 0, 0, 0));
                
                Button resolveBtn = new Button("✅ Mark Resolved");
                resolveBtn.setPrefWidth(150);
                resolveBtn.setPrefHeight(40);
                resolveBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                resolveBtn.setStyle("-fx-background-color: #27ae60; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-radius: 8px; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(39,174,96,0.3), 5, 0, 0, 2);");
                resolveBtn.setOnMouseEntered(e -> resolveBtn.setStyle(
                        "-fx-background-color: #229954; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-radius: 8px; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(39,174,96,0.5), 8, 0, 0, 3);"));
                resolveBtn.setOnMouseExited(e -> resolveBtn.setStyle(
                        "-fx-background-color: #27ae60; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-radius: 8px; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(39,174,96,0.3), 5, 0, 0, 2);"));
                resolveBtn.setOnAction(e -> {
                    resolveEmergency(em.getEmergencyId(), em.getUserId());
                    showEmergenciesPage();
                });
                
                buttonBox.getChildren().add(resolveBtn);
                card.getChildren().add(buttonBox);
                activeContainer.getChildren().add(card);
            }
        }
        activeScroll.setContent(activeContainer);
        activeTab.setContent(activeScroll);

        // Resolved tab
        Tab resolvedTab = new Tab("✔️ Resolved");
        resolvedTab.setClosable(false);
        
        ScrollPane resolvedScroll = new ScrollPane();
        resolvedScroll.setFitToWidth(true);
        resolvedScroll.setStyle("-fx-background-color: transparent;");
        
        VBox resolvedContainer = new VBox(15);
        resolvedContainer.setPadding(new Insets(10));
        resolvedContainer.setStyle("-fx-background-color: #f5f6fa;");
        
        List<Emergency> resolvedEmergencies = getMyResolvedEmergencies();
        if (resolvedEmergencies.isEmpty()) {
            VBox emptyBox = new VBox(20);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(60));
            Label emptyIcon = new Label("📋");
            emptyIcon.setFont(Font.font(48));
            Label emptyText = new Label("No resolved emergencies");
            emptyText.setFont(Font.font("Segoe UI", 16));
            emptyText.setTextFill(Color.web("#95a5a6"));
            emptyBox.getChildren().addAll(emptyIcon, emptyText);
            resolvedContainer.getChildren().add(emptyBox);
        } else {
            for (Emergency em : resolvedEmergencies) {
                VBox card = EmergencyCard.createEmergencyCard(em, false);
                resolvedContainer.getChildren().add(card);
            }
        }
        resolvedScroll.setContent(resolvedContainer);
        resolvedTab.setContent(resolvedScroll);

        tabPane.getTabs().addAll(activeTab, resolvedTab);
        content.getChildren().addAll(title, tabPane);
        mainLayout.setCenter(content);
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

        Label infoTitle = new Label("📋 Account Information");
        infoTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        infoTitle.setTextFill(Color.web("#111827"));
        VBox.setMargin(infoTitle, new Insets(0, 0, 15, 0));

        HBox nameItem = createInfoItem("👤", "Full Name", currentUser.getFullName());
        HBox emailItem = createInfoItem("📧", "Email", currentUser.getEmail());
        HBox phoneItem = createInfoItem("📱", "Phone Number", currentUser.getPhoneNumber());
        HBox usernameItem = createInfoItem("🔑", "Username", currentUser.getUsername());
        HBox roleItem = createInfoItem("🎭", "Role", currentUser.getRole().toString());
        roleItem.setStyle(roleItem.getStyle() + " -fx-background-color: #EFF6FF; " +
                "-fx-border-color: #3B82F6; " +
                "-fx-border-width: 2px;");

        infoBox.getChildren().addAll(infoTitle, nameItem, emailItem, phoneItem, 
                usernameItem, roleItem);

        // Action Buttons Row
        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER);
        
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

    private void acceptEmergency(int emergencyId) {
        String query = "UPDATE emergencies SET status = 'ACCEPTED', responder_id = ?, accepted_at = NOW() WHERE emergency_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentUser.getUserId());
            stmt.setInt(2, emergencyId);
            stmt.executeUpdate();

            // Get user_id for notification
            String getUserQuery = "SELECT user_id FROM emergencies WHERE emergency_id = ?";
            PreparedStatement getUserStmt = conn.prepareStatement(getUserQuery);
            getUserStmt.setInt(1, emergencyId);
            ResultSet rs = getUserStmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("user_id");
                createNotification(userId, emergencyId, "Emergency Accepted",
                        "Your emergency has been accepted by " + currentUser.getFullName());
            }

            showAlert("Success", "Emergency accepted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void resolveEmergency(int emergencyId, int userId) {
        String query = "UPDATE emergencies SET status = 'RESOLVED', resolved_at = NOW() WHERE emergency_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, emergencyId);
            stmt.executeUpdate();

            createNotification(userId, emergencyId, "Emergency Resolved",
                    "Your emergency has been resolved by " + currentUser.getFullName());

            showAlert("Success", "Emergency resolved successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    private int getPendingEmergencyCount() {
        String query = "SELECT COUNT(*) FROM emergencies WHERE status = 'PENDING'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getMyActiveCount() {
        String query = "SELECT COUNT(*) FROM emergencies WHERE responder_id = ? AND status = 'ACCEPTED'";
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

    private int getMyResolvedCount() {
        String query = "SELECT COUNT(*) FROM emergencies WHERE responder_id = ? AND status = 'RESOLVED'";
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

    private List<Emergency> getPendingEmergencies() {
        List<Emergency> emergencies = new ArrayList<>();
        String query = "SELECT e.*, u.full_name as user_name FROM emergencies e " +
                "JOIN users u ON e.user_id = u.user_id " +
                "WHERE e.status = 'PENDING' ORDER BY e.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

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
                em.setUserName(rs.getString("user_name"));
                emergencies.add(em);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return emergencies;
    }

    private List<Emergency> getMyActiveEmergencies() {
        List<Emergency> emergencies = new ArrayList<>();
        String query = "SELECT e.*, u.full_name as user_name FROM emergencies e " +
                "JOIN users u ON e.user_id = u.user_id " +
                "WHERE e.responder_id = ? AND e.status = 'ACCEPTED' ORDER BY e.accepted_at DESC";
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
                em.setAcceptedAt(rs.getTimestamp("accepted_at"));
                em.setUserName(rs.getString("user_name"));
                emergencies.add(em);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return emergencies;
    }

    private List<Emergency> getMyResolvedEmergencies() {
        List<Emergency> emergencies = new ArrayList<>();
        String query = "SELECT e.*, u.full_name as user_name FROM emergencies e " +
                "JOIN users u ON e.user_id = u.user_id " +
                "WHERE e.responder_id = ? AND e.status = 'RESOLVED' ORDER BY e.resolved_at DESC LIMIT 20";
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
                em.setResolvedAt(rs.getTimestamp("resolved_at"));
                em.setUserName(rs.getString("user_name"));
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