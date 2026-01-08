package com.ets.util;

import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class LoadingOverlay {
    
    public static StackPane createLoadingOverlay(String message) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        overlay.setAlignment(Pos.CENTER);
        
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        
        // Spinning loader
        Label spinner = new Label("⏳");
        spinner.setFont(Font.font(48));
        
        RotateTransition rotate = new RotateTransition(Duration.seconds(1), spinner);
        rotate.setByAngle(360);
        rotate.setCycleCount(RotateTransition.INDEFINITE);
        rotate.play();
        
        // Loading message
        Label messageLabel = new Label(message);
        messageLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        messageLabel.setTextFill(Color.WHITE);
        
        content.getChildren().addAll(spinner, messageLabel);
        overlay.getChildren().add(content);
        
        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), overlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        
        return overlay;
    }
    
    public static void showLoadingWithCallback(StackPane container, String message, Runnable callback) {
        StackPane loading = createLoadingOverlay(message);
        container.getChildren().add(loading);
        
        // Simulate loading delay and execute callback
        new Thread(() -> {
            try {
                Thread.sleep(800); // Simulate loading time
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            javafx.application.Platform.runLater(() -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(300), loading);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(e -> container.getChildren().remove(loading));
                fadeOut.play();
                
                if (callback != null) {
                    callback.run();
                }
            });
        }).start();
    }
}

