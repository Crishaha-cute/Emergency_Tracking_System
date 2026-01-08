package com.ets.controller;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.ets.model.User;
import com.ets.model.User.UserRole;
import com.ets.model.Emergency;
import com.ets.model.Emergency.EmergencyType;
import com.ets.model.Emergency.EmergencyStatus;
import com.ets.util.DatabaseConnection;
import com.ets.util.LoadingOverlay;
import com.ets.util.EmergencyCard;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardController {
    private Stage stage;
    private User currentUser;
    private BorderPane mainLayout;
    private Button activeNavButton;

    public AdminDashboardController(Stage stage, User user) {
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
        Button homeBtn = createSidebarButton("🏠", "Dashboard");
        Button usersBtn = createSidebarButton("👥", "Users");
        Button emergenciesBtn = createSidebarButton("📋", "Emergencies");
        Button statisticsBtn = createSidebarButton("📊", "Statistics");
        Button profileBtn = createSidebarButton("👤", "Profile");

        homeBtn.setOnAction(e -> {
            setActiveButton(homeBtn);
            navigateWithLoading(() -> showHomePage(), "Loading dashboard...");
        });
        usersBtn.setOnAction(e -> {
            setActiveButton(usersBtn);
            navigateWithLoading(() -> showUsersPage(), "Loading users...");
        });
        emergenciesBtn.setOnAction(e -> {
            setActiveButton(emergenciesBtn);
            navigateWithLoading(() -> showEmergenciesPage(), "Loading emergencies...");
        });
        statisticsBtn.setOnAction(e -> {
            setActiveButton(statisticsBtn);
            navigateWithLoading(() -> showStatisticsPage(), "Loading statistics...");
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
        
        Label userRole = new Label("ADMIN");
        userRole.setFont(Font.font("Segoe UI", 11));
        userRole.setTextFill(Color.web("#2C5282"));
        
        userSection.getChildren().addAll(userName, userRole);

        sidebar.getChildren().addAll(headerSection, homeBtn, usersBtn, emergenciesBtn, 
                statisticsBtn, profileBtn, spacer, userSection);
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
                        if (!label.getText().matches(".*[🏠👥📋📊👤].*")) {
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
                    if (!label.getText().matches(".*[🏠👥📋📊👤].*")) {
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

        // Welcome Header Section
        VBox welcomeSection = new VBox(10);
        welcomeSection.setPadding(new Insets(25, 30, 25, 30));
        welcomeSection.setStyle("-fx-background-color: linear-gradient(to right, #3B82F6, #2563EB); " +
                "-fx-background-radius: 16px; " +
                "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.3), 15, 0, 0, 5);");
        
        HBox welcomeRow = new HBox(15);
        welcomeRow.setAlignment(Pos.CENTER_LEFT);
        
        Label welcomeIcon = new Label("👋");
        welcomeIcon.setFont(Font.font(40));
        
        VBox welcomeText = new VBox(5);
        Label welcomeLabel = new Label("Admin Dashboard");
        welcomeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        welcomeLabel.setTextFill(Color.WHITE);
        
        Label userNameLabel = new Label("Welcome, " + currentUser.getFullName());
        userNameLabel.setFont(Font.font("Segoe UI", 18));
        userNameLabel.setTextFill(Color.web("#DBEAFE"));
        
        welcomeText.getChildren().addAll(welcomeLabel, userNameLabel);
        welcomeRow.getChildren().addAll(welcomeIcon, welcomeText);
        welcomeSection.getChildren().add(welcomeRow);

        // Statistics Cards Row
        HBox statsRow = new HBox(15);
        statsRow.setAlignment(Pos.CENTER);
        
        int totalUsers = getTotalUserCount();
        int totalEmergencies = getTotalEmergencyCount();
        int pendingEmergencies = getPendingEmergencyCount();
        int activeEmergencies = getActiveEmergencyCount();
        int resolvedEmergencies = getResolvedEmergencyCount();
        
        VBox usersCard = createStatCard("👥", String.valueOf(totalUsers), "Total Users", "#3B82F6", "#DBEAFE");
        VBox emergenciesCard = createStatCard("📋", String.valueOf(totalEmergencies), "Total Emergencies", "#DC2626", "#FEE2E2");
        VBox pendingCard = createStatCard("⏳", String.valueOf(pendingEmergencies), "Pending", "#F59E0B", "#FEF3C7");
        VBox activeCard = createStatCard("🚨", String.valueOf(activeEmergencies), "Active", "#DC2626", "#FEE2E2");
        VBox resolvedCard = createStatCard("✅", String.valueOf(resolvedEmergencies), "Resolved", "#10B981", "#D1FAE5");

        statsRow.getChildren().addAll(usersCard, emergenciesCard, pendingCard, activeCard, resolvedCard);
        
        content.getChildren().addAll(welcomeSection, statsRow);
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
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        valueLabel.setTextFill(Color.web(color));
        
        Label labelText = new Label(label);
        labelText.setFont(Font.font("Segoe UI", 13));
        labelText.setTextFill(Color.web("#6B7280"));
        
        card.getChildren().addAll(iconContainer, valueLabel, labelText);
        
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

    private void showUsersPage() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #F8FAFC; " +
                "-fx-background: transparent; " +
                "-fx-border: none;");
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #F8FAFC;");

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("👥 User Management");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#0F172A"));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addUserBtn = new Button("➕ Add User");
        addUserBtn.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        addUserBtn.setStyle("-fx-background-color: #10B981; " +
                "-fx-text-fill: white; " +
                "-fx-border-radius: 10px; " +
                "-fx-background-radius: 10px; " +
                "-fx-padding: 10px 20px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(16,185,129,0.3), 8, 0, 0, 2);");
        addUserBtn.setOnMouseEntered(e -> addUserBtn.setStyle(
                "-fx-background-color: #059669; " +
                "-fx-text-fill: white; " +
                "-fx-border-radius: 10px; " +
                "-fx-background-radius: 10px; " +
                "-fx-padding: 10px 20px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(16,185,129,0.5), 10, 0, 0, 3);"));
        addUserBtn.setOnMouseExited(e -> addUserBtn.setStyle(
                "-fx-background-color: #10B981; " +
                "-fx-text-fill: white; " +
                "-fx-border-radius: 10px; " +
                "-fx-background-radius: 10px; " +
                "-fx-padding: 10px 20px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(16,185,129,0.3), 8, 0, 0, 2);"));
        addUserBtn.setOnAction(e -> showAddUserDialog());
        
        header.getChildren().addAll(title, spacer, addUserBtn);

        // Users Table
        TableView<User> usersTable = new TableView<>();
        usersTable.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 16px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");
        
        TableColumn<User, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        nameCol.setPrefWidth(200);
        
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);
        
        TableColumn<User, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        phoneCol.setPrefWidth(150);
        
        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setPrefWidth(150);
        
        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(user.getRole().toString());
        });
        roleCol.setPrefWidth(100);
        
        TableColumn<User, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(200);
        actionsCol.setCellFactory(param -> new TableCell<User, Void>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox box = new HBox(5);
            
            {
                editBtn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-cursor: hand;");
                box.setAlignment(Pos.CENTER);
                box.getChildren().addAll(editBtn, deleteBtn);
                
                editBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    showEditUserDialog(user);
                });
                
                deleteBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    if (user.getUserId() == currentUser.getUserId()) {
                        showAlert("Error", "You cannot delete your own account!");
                        return;
                    }
                    if (confirmDelete("Delete User", "Are you sure you want to delete user: " + user.getFullName() + "?")) {
                        deleteUser(user.getUserId());
                        showUsersPage();
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
            }
        });
        
        usersTable.getColumns().add(nameCol);
        usersTable.getColumns().add(emailCol);
        usersTable.getColumns().add(phoneCol);
        usersTable.getColumns().add(usernameCol);
        usersTable.getColumns().add(roleCol);
        usersTable.getColumns().add(actionsCol);
        usersTable.getItems().addAll(getAllUsers());
        
        content.getChildren().addAll(header, usersTable);
        scrollPane.setContent(content);
        mainLayout.setCenter(scrollPane);
    }

    private void showEmergenciesPage() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #F8FAFC; " +
                "-fx-background: transparent; " +
                "-fx-border: none;");
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #F8FAFC;");

        Label title = new Label("📋 Emergency Management");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#0F172A"));

        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-font-family: 'Segoe UI';");
        
        // All Emergencies Tab
        Tab allTab = new Tab("All Emergencies");
        allTab.setClosable(false);
        
        ScrollPane allScroll = new ScrollPane();
        allScroll.setFitToWidth(true);
        allScroll.setStyle("-fx-background-color: transparent;");
        
        VBox allContainer = new VBox(15);
        allContainer.setPadding(new Insets(10));
        allContainer.setStyle("-fx-background-color: #F8FAFC;");
        
        List<Emergency> allEmergencies = getAllEmergencies();
        if (allEmergencies.isEmpty()) {
            VBox emptyBox = new VBox(20);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(60));
            Label emptyIcon = new Label("📋");
            emptyIcon.setFont(Font.font(64));
            Label emptyText = new Label("No emergencies found");
            emptyText.setFont(Font.font("Segoe UI", 18));
            emptyText.setTextFill(Color.web("#95a5a6"));
            emptyBox.getChildren().addAll(emptyIcon, emptyText);
            allContainer.getChildren().add(emptyBox);
        } else {
            for (Emergency em : allEmergencies) {
                VBox card = EmergencyCard.createEmergencyCard(em, false);
                
                HBox buttonBox = new HBox(10);
                buttonBox.setAlignment(Pos.CENTER_RIGHT);
                buttonBox.setPadding(new Insets(10, 0, 0, 0));
                
                if (em.getStatus() == EmergencyStatus.PENDING || em.getStatus() == EmergencyStatus.ACCEPTED) {
                    Button resolveBtn = new Button("✅ Mark Resolved");
                    resolveBtn.setPrefWidth(150);
                    resolveBtn.setPrefHeight(40);
                    resolveBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                    resolveBtn.setStyle("-fx-background-color: #10B981; " +
                            "-fx-text-fill: white; " +
                            "-fx-border-radius: 8px; " +
                            "-fx-background-radius: 8px; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(16,185,129,0.3), 5, 0, 0, 2);");
                    resolveBtn.setOnAction(e -> {
                        resolveEmergency(em.getEmergencyId(), em.getUserId());
                        showEmergenciesPage();
                    });
                    buttonBox.getChildren().add(resolveBtn);
                }
                
                Button deleteBtn = new Button("🗑️ Delete");
                deleteBtn.setPrefWidth(120);
                deleteBtn.setPrefHeight(40);
                deleteBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                deleteBtn.setStyle("-fx-background-color: #DC2626; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-radius: 8px; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(220,38,38,0.3), 5, 0, 0, 2);");
                deleteBtn.setOnAction(e -> {
                    if (confirmDelete("Delete Emergency", "Are you sure you want to delete this emergency?")) {
                        deleteEmergency(em.getEmergencyId());
                        showEmergenciesPage();
                    }
                });
                buttonBox.getChildren().add(deleteBtn);
                
                card.getChildren().add(buttonBox);
                allContainer.getChildren().add(card);
            }
        }
        allScroll.setContent(allContainer);
        allTab.setContent(allScroll);
        
        tabPane.getTabs().add(allTab);
        content.getChildren().addAll(title, tabPane);
        scrollPane.setContent(content);
        mainLayout.setCenter(scrollPane);
    }

    private void showStatisticsPage() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #F8FAFC; " +
                "-fx-background: transparent; " +
                "-fx-border: none;");
        
        VBox content = new VBox(25);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #F8FAFC;");

        Label title = new Label("📊 Statistics & Analytics");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#0F172A"));

        // Statistics Grid
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(20);
        statsGrid.setAlignment(Pos.CENTER);
        
        // User Statistics
        VBox userStatsCard = createStatsCard("👥 User Statistics", 
                "Total Users: " + getTotalUserCount() + "\n" +
                "Regular Users: " + getUserCountByRole("USER") + "\n" +
                "Responders: " + getUserCountByRole("RESPONDER") + "\n" +
                "Admins: " + getUserCountByRole("ADMIN"));
        
        // Emergency Statistics
        VBox emergencyStatsCard = createStatsCard("🚨 Emergency Statistics",
                "Total: " + getTotalEmergencyCount() + "\n" +
                "Pending: " + getPendingEmergencyCount() + "\n" +
                "Active: " + getActiveEmergencyCount() + "\n" +
                "Resolved: " + getResolvedEmergencyCount());
        
        // Emergency Type Statistics
        VBox typeStatsCard = createStatsCard("📋 By Type",
                "Medical: " + getEmergencyCountByType("MEDICAL") + "\n" +
                "Fire: " + getEmergencyCountByType("FIRE") + "\n" +
                "Crime: " + getEmergencyCountByType("CRIME") + "\n" +
                "Accident: " + getEmergencyCountByType("ACCIDENT"));
        
        statsGrid.add(userStatsCard, 0, 0);
        statsGrid.add(emergencyStatsCard, 1, 0);
        statsGrid.add(typeStatsCard, 2, 0);
        
        content.getChildren().addAll(title, statsGrid);
        scrollPane.setContent(content);
        mainLayout.setCenter(scrollPane);
    }
    
    private VBox createStatsCard(String title, String content) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));
        card.setPrefWidth(250);
        card.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 16px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web("#111827"));
        
        Label contentLabel = new Label(content);
        contentLabel.setFont(Font.font("Segoe UI", 14));
        contentLabel.setTextFill(Color.web("#6B7280"));
        
        card.getChildren().addAll(titleLabel, contentLabel);
        return card;
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

        VBox headerSection = new VBox(15);
        headerSection.setAlignment(Pos.CENTER);
        headerSection.setPadding(new Insets(25, 30, 25, 30));
        headerSection.setStyle("-fx-background-color: linear-gradient(to right, #3B82F6, #2563EB); " +
                "-fx-background-radius: 20px; " +
                "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.3), 15, 0, 0, 5);");
        
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
        
        Label profileRole = new Label("ADMIN");
        profileRole.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        profileRole.setTextFill(Color.web("#DBEAFE"));
        profileRole.setStyle("-fx-background-color: rgba(255,255,255,0.25); " +
                "-fx-background-radius: 12px; " +
                "-fx-padding: 6px 16px;");
        
        headerSection.getChildren().addAll(avatarContainer, profileName, profileRole);

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
        HBox roleItem = createInfoItem("🎭", "Role", "ADMIN");
        roleItem.setStyle(roleItem.getStyle() + " -fx-background-color: #EFF6FF; " +
                "-fx-border-color: #3B82F6; " +
                "-fx-border-width: 2px;");

        infoBox.getChildren().addAll(infoTitle, nameItem, emailItem, phoneItem, 
                usernameItem, roleItem);

        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER);
        
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

    // Database Methods
    private int getTotalUserCount() {
        String query = "SELECT COUNT(*) FROM users";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getTotalEmergencyCount() {
        String query = "SELECT COUNT(*) FROM emergencies";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getPendingEmergencyCount() {
        String query = "SELECT COUNT(*) FROM emergencies WHERE status = 'PENDING'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getActiveEmergencyCount() {
        String query = "SELECT COUNT(*) FROM emergencies WHERE status = 'ACCEPTED'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getResolvedEmergencyCount() {
        String query = "SELECT COUNT(*) FROM emergencies WHERE status = 'RESOLVED'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getUserCountByRole(String role) {
        String query = "SELECT COUNT(*) FROM users WHERE role = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, role);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getEmergencyCountByType(String type) {
        String query = "SELECT COUNT(*) FROM emergencies WHERE emergency_type = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, type);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setPhoneNumber(rs.getString("phone_number"));
                user.setUsername(rs.getString("username"));
                user.setRole(UserRole.valueOf(rs.getString("role")));
                user.setEmergencyContact(rs.getString("emergency_contact"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    private List<Emergency> getAllEmergencies() {
        List<Emergency> emergencies = new ArrayList<>();
        String query = "SELECT e.*, u.full_name as user_name FROM emergencies e " +
                "LEFT JOIN users u ON e.user_id = u.user_id ORDER BY e.created_at DESC";
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
                if (rs.getString("user_name") != null) {
                    em.setUserName(rs.getString("user_name"));
                }
                emergencies.add(em);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return emergencies;
    }

    private void showAddUserDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Add New User");
        dialog.setHeaderText(null);
        dialog.getDialogPane().setStyle("-fx-font-family: 'Segoe UI';");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("USER", "RESPONDER", "ADMIN");
        roleCombo.setValue("USER");

        content.getChildren().addAll(
                new Label("Full Name:"), fullNameField,
                new Label("Email:"), emailField,
                new Label("Phone:"), phoneField,
                new Label("Username:"), usernameField,
                new Label("Password:"), passwordField,
                new Label("Role:"), roleCombo
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                if (fullNameField.getText().isEmpty() || emailField.getText().isEmpty() ||
                    usernameField.getText().isEmpty() || passwordField.getText().isEmpty()) {
                    showAlert("Error", "Please fill all required fields!");
                    return null;
                }
                if (addUser(fullNameField.getText(), emailField.getText(), phoneField.getText(),
                        usernameField.getText(), passwordField.getText(), roleCombo.getValue())) {
                    showAlert("Success", "User added successfully!");
                    showUsersPage();
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showEditUserDialog(User user) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText(null);
        dialog.getDialogPane().setStyle("-fx-font-family: 'Segoe UI';");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        TextField fullNameField = new TextField(user.getFullName());
        TextField emailField = new TextField(user.getEmail());
        TextField phoneField = new TextField(user.getPhoneNumber());
        TextField usernameField = new TextField(user.getUsername());
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Leave blank to keep current password");
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("USER", "RESPONDER", "ADMIN");
        roleCombo.setValue(user.getRole().toString());

        content.getChildren().addAll(
                new Label("Full Name:"), fullNameField,
                new Label("Email:"), emailField,
                new Label("Phone:"), phoneField,
                new Label("Username:"), usernameField,
                new Label("Password (optional):"), passwordField,
                new Label("Role:"), roleCombo
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                if (updateUser(user.getUserId(), fullNameField.getText(), emailField.getText(),
                        phoneField.getText(), usernameField.getText(), passwordField.getText(),
                        roleCombo.getValue())) {
                    showAlert("Success", "User updated successfully!");
                    showUsersPage();
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private boolean addUser(String fullName, String email, String phone, String username, String password, String role) {
        String query = "INSERT INTO users (full_name, email, phone_number, username, password, role) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, fullName);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.setString(4, username);
            stmt.setString(5, password);
            stmt.setString(6, role);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            showAlert("Error", "Failed to add user: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private boolean updateUser(int userId, String fullName, String email, String phone, String username, String password, String role) {
        String query;
        if (password.isEmpty()) {
            query = "UPDATE users SET full_name = ?, email = ?, phone_number = ?, username = ?, role = ? WHERE user_id = ?";
        } else {
            query = "UPDATE users SET full_name = ?, email = ?, phone_number = ?, username = ?, password = ?, role = ? WHERE user_id = ?";
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            int paramIndex = 1;
            stmt.setString(paramIndex++, fullName);
            stmt.setString(paramIndex++, email);
            stmt.setString(paramIndex++, phone);
            stmt.setString(paramIndex++, username);
            if (!password.isEmpty()) {
                stmt.setString(paramIndex++, password);
            }
            stmt.setString(paramIndex++, role);
            stmt.setInt(paramIndex, userId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            showAlert("Error", "Failed to update user: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private void deleteUser(int userId) {
        String query = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            showAlert("Success", "User deleted successfully!");
        } catch (SQLException e) {
            showAlert("Error", "Failed to delete user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void resolveEmergency(int emergencyId, int userId) {
        String query = "UPDATE emergencies SET status = 'RESOLVED', resolved_at = NOW() WHERE emergency_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, emergencyId);
            stmt.executeUpdate();
            showAlert("Success", "Emergency marked as resolved!");
        } catch (SQLException e) {
            showAlert("Error", "Failed to resolve emergency: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteEmergency(int emergencyId) {
        String query = "DELETE FROM emergencies WHERE emergency_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, emergencyId);
            stmt.executeUpdate();
            showAlert("Success", "Emergency deleted successfully!");
        } catch (SQLException e) {
            showAlert("Error", "Failed to delete emergency: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean confirmDelete(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

