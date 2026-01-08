import javafx.application.Application;
import javafx.stage.Stage;
import com.ets.controller.AuthController;
import com.ets.util.DatabaseConnection;

public class EmergencyTrackingSystem extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Test database connection
            DatabaseConnection.getConnection();
            System.out.println("Database connected successfully!");

            // Start with login page
            AuthController authController = new AuthController(primaryStage);
            authController.showLoginPage();

            primaryStage.setTitle("Emergency Tracking System");
            primaryStage.setResizable(true);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error starting application: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        DatabaseConnection.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}