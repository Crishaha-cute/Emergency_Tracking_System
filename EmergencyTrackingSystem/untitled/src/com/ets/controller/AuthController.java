package com.ets.controller;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.ets.model.User;
import com.ets.model.User.UserRole;
import com.ets.util.DatabaseConnection;
import com.ets.util.LoadingOverlay;

import java.io.File;
import java.net.URI;
import java.sql.*;

public class AuthController {
    private Stage stage;
    private User currentUser;

    public AuthController(Stage stage) {
        this.stage = stage;
    }

    public void showLoginPage() {
        StackPane root = new StackPane();
        
        HBox content = new HBox();

        // Left section - Branding
        VBox leftSection = createBrandingSection();
        HBox.setHgrow(leftSection, Priority.ALWAYS);

        // Right section - Login form
        VBox rightSection = createLoginForm();
        HBox.setHgrow(rightSection, Priority.ALWAYS);

        content.getChildren().addAll(leftSection, rightSection);
        root.getChildren().add(content);

        Scene scene = new Scene(root, 1000, 600);
        
        // Set uniform window size
        stage.setWidth(1000);
        stage.setHeight(600);
        stage.setMinWidth(1000);
        stage.setMinHeight(600);
        
        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), content);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        
        stage.setScene(scene);
    }

    private VBox createBrandingSection() {
        VBox section = new VBox();
        section.setMinWidth(500);
        section.setAlignment(Pos.TOP_CENTER);
        section.setPadding(new Insets(40, 20, 20, 20));

        // Set background image
        try {
            java.net.URL imageUrl = getClass().getResource("/resources/login.jpg");
            if (imageUrl == null) {
                // Try alternative path
                imageUrl = getClass().getResource("../../resources/login.jpg");
            }
            if (imageUrl == null) {
                // Try file system path as fallback
                File imageFile = new File("src/resources/login.jpg");
                if (!imageFile.exists()) {
                    imageFile = new File(System.getProperty("user.dir"), "src/resources/login.jpg");
                }
                if (imageFile.exists()) {
                    URI imageUri = imageFile.toURI();
                    String imagePath = imageUri.toString();
                    section.setStyle("-fx-background-image: url('" + imagePath + "'); " +
                            "-fx-background-size: cover; " +
                            "-fx-background-position: center bottom;");
                } else {
                    throw new Exception("Image file not found");
                }
            } else {
                String imagePath = imageUrl.toExternalForm();
                section.setStyle("-fx-background-image: url('" + imagePath + "'); " +
                        "-fx-background-size: cover; " +
                        "-fx-background-position: center bottom;");
            }
        } catch (Exception e) {
            System.err.println("Failed to load background image: " + e.getMessage());
            // Fallback to a solid color if image fails to load
            section.setStyle("-fx-background-color: #e74c3c;");
        }

        // Logo
        Label logo = new Label("🚨");
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 90));
        logo.setTextFill(Color.WHITE);
        logo.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);");

        // Title
        Label title = new Label("Emergency Tracking System");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        title.setTextFill(Color.WHITE);
        title.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 8, 0, 0, 2);");
        VBox.setMargin(title, new Insets(-10, 0, 0, 0));

        // VBox for logo + title
        VBox textBox = new VBox(5); // smaller spacing
        textBox.setAlignment(Pos.TOP_CENTER);
        textBox.getChildren().addAll(logo, title);

        // Spacer to push content to top
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Add elements to section
        section.getChildren().addAll(textBox, spacer);

        return section;
    }


    private VBox createLoginForm() {
        VBox section = new VBox(20);
        section.setMinWidth(500);
        section.setAlignment(Pos.TOP_CENTER);
        section.setPadding(new Insets(50, 40, 40, 40));
        section.setStyle("-fx-background-color: #ffffff;");

        Label welcomeLabel = new Label("Welcome Back");
        welcomeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 42));
        welcomeLabel.setTextFill(Color.web("#2c3e50"));
        VBox.setMargin(welcomeLabel, new Insets(0, 0, 10, 0));
        VBox.setVgrow(welcomeLabel, Priority.NEVER);
        
        Label subtitleLabel = new Label("Sign in to continue");
        subtitleLabel.setFont(Font.font("Segoe UI", 14));
        subtitleLabel.setTextFill(Color.web("#7f8c8d"));
        VBox.setMargin(subtitleLabel, new Insets(0, 0, 30, 0));

        Region spacer = new Region();
        spacer.setPrefHeight(20);

        // Common size for all inputs and buttons
        double inputWidth = 360;
        double inputHeight = 50;

        // Username field
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setPrefWidth(inputWidth);
        usernameField.setMaxWidth(inputWidth);
        usernameField.setPrefHeight(inputHeight);
        usernameField.setFont(Font.font("Segoe UI", 14));
        usernameField.setStyle("-fx-background-color: #f8f9fa; " +
                "-fx-border-color: #dee2e6; " +
                "-fx-border-width: 1.5px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-padding: 12px 16px;");
        usernameField.setOnMouseEntered(e -> usernameField.setStyle(
                "-fx-background-color: #ffffff; " +
                "-fx-border-color: #3498db; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-padding: 12px 16px;"));
        usernameField.setOnMouseExited(e -> usernameField.setStyle(
                "-fx-background-color: #f8f9fa; " +
                "-fx-border-color: #dee2e6; " +
                "-fx-border-width: 1.5px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-padding: 12px 16px;"));

        // Password field
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefWidth(inputWidth);
        passwordField.setMaxWidth(inputWidth);
        passwordField.setPrefHeight(inputHeight);
        passwordField.setFont(Font.font("Segoe UI", 14));
        passwordField.setStyle("-fx-background-color: #f8f9fa; " +
                "-fx-border-color: #dee2e6; " +
                "-fx-border-width: 1.5px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-padding: 12px 16px;");
        passwordField.setOnMouseEntered(e -> passwordField.setStyle(
                "-fx-background-color: #ffffff; " +
                "-fx-border-color: #3498db; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-padding: 12px 16px;"));
        passwordField.setOnMouseExited(e -> passwordField.setStyle(
                "-fx-background-color: #f8f9fa; " +
                "-fx-border-color: #dee2e6; " +
                "-fx-border-width: 1.5px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-padding: 12px 16px;"));

        // Role combo box
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("User", "Responder", "Admin");
        roleCombo.setValue("User");
        roleCombo.setPrefWidth(inputWidth);
        roleCombo.setMaxWidth(inputWidth);
        roleCombo.setPrefHeight(inputHeight);
        roleCombo.setStyle("-fx-background-color: #f8f9fa; " +
                "-fx-border-color: #dee2e6; " +
                "-fx-border-width: 1.5px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-font-size: 14px; " +
                "-fx-font-family: 'Segoe UI';");

        // Login button
        Button loginBtn = new Button("Sign In");
        loginBtn.setPrefWidth(inputWidth);
        loginBtn.setMaxWidth(inputWidth);
        loginBtn.setPrefHeight(inputHeight);
        loginBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        loginBtn.setStyle("-fx-background-color: #e74c3c; " +
                "-fx-text-fill: white; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(231,76,60,0.3), 8, 0, 0, 2);");
        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle(
                "-fx-background-color: #c0392b; " +
                "-fx-text-fill: white; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(231,76,60,0.5), 10, 0, 0, 3);"));
        loginBtn.setOnMouseExited(e -> loginBtn.setStyle(
                "-fx-background-color: #e74c3c; " +
                "-fx-text-fill: white; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(231,76,60,0.3), 8, 0, 0, 2);"));

        // Register button
        Button registerBtn = new Button("Don't have an account? Register");
        registerBtn.setPrefWidth(inputWidth);
        registerBtn.setMaxWidth(inputWidth);
        registerBtn.setPrefHeight(40);
        registerBtn.setFont(Font.font("Segoe UI", 13));
        registerBtn.setStyle("-fx-background-color: transparent; " +
                "-fx-text-fill: #3498db; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-cursor: hand; " +
                "-fx-underline: false;");
        registerBtn.setOnMouseEntered(e -> registerBtn.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-text-fill: #2980b9; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-cursor: hand; " +
                "-fx-underline: true;"));
        registerBtn.setOnMouseExited(e -> registerBtn.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-text-fill: #3498db; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-cursor: hand; " +
                "-fx-underline: false;"));

        // Login button action with loading
        loginBtn.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String role = roleCombo.getValue().toUpperCase();
            
            if (username.isEmpty() || password.isEmpty()) {
                showAlert("Validation Error", "Please enter both username and password!");
                return;
            }

            // Show loading overlay
            StackPane root = (StackPane) stage.getScene().getRoot();
            LoadingOverlay.showLoadingWithCallback(root, "Signing in...", () -> {
                User user = authenticateUser(username, password, role);
                if (user != null) {
                    currentUser = user;
                    if (user.getRole() == UserRole.USER) {
                        new UserDashboardController(stage, user).showDashboard();
                    } else if (user.getRole() == UserRole.RESPONDER) {
                        new ResponderDashboardController(stage, user).showDashboard();
                    } else if (user.getRole() == UserRole.ADMIN) {
                        new AdminDashboardController(stage, user).showDashboard();
                    }
                } else {
                    showAlert("Login Failed", "Invalid credentials or role!");
                }
            });
        });

        // Register button action
        registerBtn.setOnAction(e -> showRegistrationPage());

        // Add all elements to the VBox
        section.getChildren().addAll(welcomeLabel, subtitleLabel, spacer, usernameField, passwordField,
                roleCombo, loginBtn, registerBtn);

        return section;
    }



    private void showRegistrationPage() {
        StackPane root = new StackPane();
        
        HBox content = new HBox();

        VBox leftSection = createBrandingSection();
        HBox.setHgrow(leftSection, Priority.ALWAYS);
        
        VBox rightSection = createRegistrationForm();
        HBox.setHgrow(rightSection, Priority.ALWAYS);

        content.getChildren().addAll(leftSection, rightSection);
        root.getChildren().add(content);

        Scene scene = new Scene(root, 1000, 600);
        
        // Set uniform window size
        stage.setWidth(1000);
        stage.setHeight(600);
        stage.setMinWidth(1000);
        stage.setMinHeight(600);
        
        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), content);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        
        stage.setScene(scene);
    }

    private VBox createRegistrationForm() {
        // Outer container with white background
        VBox outerSection = new VBox();
        outerSection.setMinWidth(500);
        outerSection.setStyle("-fx-background-color: #ffffff;");
        
        // ScrollPane to make form scrollable
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent; " +
                "-fx-background: transparent; " +
                "-fx-border: none; " +
                "-fx-padding: 0;");
        
        // Inner content container
        VBox section = new VBox(20);
        section.setMinWidth(500);
        section.setAlignment(Pos.TOP_CENTER);
        section.setPadding(new Insets(50, 40, 40, 40));
        section.setStyle("-fx-background-color: #ffffff;");

        Label titleLabel = new Label("Create Account");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 42));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        VBox.setMargin(titleLabel, new Insets(0, 0, 10, 0));
        VBox.setVgrow(titleLabel, Priority.NEVER);
        
        Label subtitleLabel = new Label("Join us today");
        subtitleLabel.setFont(Font.font("Segoe UI", 14));
        subtitleLabel.setTextFill(Color.web("#7f8c8d"));
        VBox.setMargin(subtitleLabel, new Insets(0, 0, 30, 0));

        Region spacer = new Region();
        spacer.setPrefHeight(20);

        // Common size for all inputs and buttons (matching login form)
        double inputWidth = 360;
        double inputHeight = 50;

        // Helper method to style text fields (matching login form style)
        String fieldStyle = "-fx-background-color: #f8f9fa; " +
                "-fx-border-color: #dee2e6; " +
                "-fx-border-width: 1.5px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-padding: 12px 16px;";
        
        String fieldHoverStyle = "-fx-background-color: #ffffff; " +
                "-fx-border-color: #3498db; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-padding: 12px 16px;";

        // Full Name
        TextField fullNameField = createStyledTextField("Full Name", inputWidth, inputHeight, fieldStyle, fieldHoverStyle);
        fullNameField.setFont(Font.font("Segoe UI", 14));
        
        // Email
        TextField emailField = createStyledTextField("Email", inputWidth, inputHeight, fieldStyle, fieldHoverStyle);
        emailField.setFont(Font.font("Segoe UI", 14));
        
        // Phone Number
        TextField phoneField = createStyledTextField("Phone Number", inputWidth, inputHeight, fieldStyle, fieldHoverStyle);
        phoneField.setFont(Font.font("Segoe UI", 14));
        
        // Username
        TextField usernameField = createStyledTextField("Username", inputWidth, inputHeight, fieldStyle, fieldHoverStyle);
        usernameField.setFont(Font.font("Segoe UI", 14));
        
        // Password
        PasswordField passwordField = createStyledPasswordField("Password", inputWidth, inputHeight, fieldStyle, fieldHoverStyle);
        passwordField.setFont(Font.font("Segoe UI", 14));
        
        // Role ComboBox (matching login form style)
        // Note: Admin role is not available for registration - only admins can create admin accounts
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("User", "Responder");
        roleCombo.setValue("User");
        roleCombo.setPrefWidth(inputWidth);
        roleCombo.setMaxWidth(inputWidth);
        roleCombo.setPrefHeight(inputHeight);
        roleCombo.setStyle("-fx-background-color: #f8f9fa; " +
                "-fx-border-color: #dee2e6; " +
                "-fx-border-width: 1.5px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-font-size: 14px; " +
                "-fx-font-family: 'Segoe UI';");
        
        // Emergency Contact
        TextField emergencyContactField = createStyledTextField("Emergency Contact (Optional)", inputWidth, inputHeight, fieldStyle, fieldHoverStyle);
        emergencyContactField.setFont(Font.font("Segoe UI", 14));

        // Register Button
        Button registerBtn = new Button("Create Account");
        registerBtn.setPrefWidth(inputWidth);
        registerBtn.setMaxWidth(inputWidth);
        registerBtn.setPrefHeight(inputHeight);
        registerBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        registerBtn.setStyle("-fx-background-color: #e74c3c; " +
                "-fx-text-fill: white; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(231,76,60,0.3), 8, 0, 0, 2);");
        registerBtn.setOnMouseEntered(e -> registerBtn.setStyle(
                "-fx-background-color: #c0392b; " +
                "-fx-text-fill: white; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(231,76,60,0.5), 10, 0, 0, 3);"));
        registerBtn.setOnMouseExited(e -> registerBtn.setStyle(
                "-fx-background-color: #e74c3c; " +
                "-fx-text-fill: white; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(231,76,60,0.3), 8, 0, 0, 2);"));

        // Back Button
        Button backBtn = new Button("Back to Login");
        backBtn.setPrefWidth(inputWidth);
        backBtn.setMaxWidth(inputWidth);
        backBtn.setPrefHeight(40);
        backBtn.setFont(Font.font("Segoe UI", 13));
        backBtn.setStyle("-fx-background-color: transparent; " +
                "-fx-text-fill: #3498db; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-cursor: hand; " +
                "-fx-underline: false;");
        backBtn.setOnMouseEntered(e -> backBtn.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-text-fill: #2980b9; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-cursor: hand; " +
                "-fx-underline: true;"));
        backBtn.setOnMouseExited(e -> backBtn.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-text-fill: #3498db; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-cursor: hand; " +
                "-fx-underline: false;"));

        // Actions with loading
        registerBtn.setOnAction(e -> {
            StackPane root = (StackPane) stage.getScene().getRoot();
            LoadingOverlay.showLoadingWithCallback(root, "Creating account...", () -> {
                if (registerUser(fullNameField.getText(), emailField.getText(),
                        phoneField.getText(), usernameField.getText(),
                        passwordField.getText(), roleCombo.getValue(),
                        emergencyContactField.getText())) {
                    showAlert("Success", "Registration successful! Please login.");
                    showLoginPage();
                }
            });
        });

        backBtn.setOnAction(e -> showLoginPage());

        // Add all elements (matching login form order and spacing)
        section.getChildren().addAll(titleLabel, subtitleLabel, spacer, 
                fullNameField, emailField, phoneField, usernameField, passwordField, 
                roleCombo, emergencyContactField, registerBtn, backBtn);
        
        // Set scroll pane content
        scrollPane.setContent(section);
        
        // Add scroll pane to outer section
        outerSection.getChildren().add(scrollPane);
        
        return outerSection;
    }


    private User authenticateUser(String username, String password, String role) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ? AND role = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setPhoneNumber(rs.getString("phone_number"));
                user.setUsername(rs.getString("username"));
                try {
                    user.setRole(UserRole.valueOf(rs.getString("role")));
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid role in database: " + rs.getString("role"));
                    return null;
                }
                user.setEmergencyContact(rs.getString("emergency_contact"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private boolean registerUser(String fullName, String email, String phone,
                                 String username, String password, String role,
                                 String emergencyContact) {
        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() ||
                username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please fill all required fields!");
            return false;
        }

        String query = "INSERT INTO users (full_name, email, phone_number, username, password, role, emergency_contact) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, fullName);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.setString(4, username);
            stmt.setString(5, password);
            stmt.setString(6, role.toUpperCase());
            stmt.setString(7, emergencyContact.isEmpty() ? null : emergencyContact);

            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            String errorMsg = e.getMessage();
            if (errorMsg.contains("Data truncated for column 'role'")) {
                showAlert("Database Error", "Admin role is not enabled in database. " +
                        "Please run this SQL command first:\n\n" +
                        "ALTER TABLE users MODIFY COLUMN role ENUM('USER', 'RESPONDER', 'ADMIN') NOT NULL;\n\n" +
                        "See URGENT_FIX_ADMIN_ROLE.sql for details.");
            } else {
                showAlert("Error", "Registration failed: " + errorMsg);
            }
            e.printStackTrace();
        }
        return false;
    }

    private TextField createStyledTextField(String prompt, double width, double height, String style, String hoverStyle) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefWidth(width);
        field.setMaxWidth(width);
        field.setPrefHeight(height);
        field.setStyle(style);
        field.setOnMouseEntered(e -> field.setStyle(hoverStyle));
        field.setOnMouseExited(e -> field.setStyle(style));
        return field;
    }
    
    private PasswordField createStyledPasswordField(String prompt, double width, double height, String style, String hoverStyle) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        field.setPrefWidth(width);
        field.setMaxWidth(width);
        field.setPrefHeight(height);
        field.setStyle(style);
        field.setOnMouseEntered(e -> field.setStyle(hoverStyle));
        field.setOnMouseExited(e -> field.setStyle(style));
        return field;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");
        alert.showAndWait();
    }
}