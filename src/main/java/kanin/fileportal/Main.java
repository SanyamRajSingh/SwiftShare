package kanin.fileportal;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main class serves as the primary entry point for the SwiftShare JavaFX application.
 * It initializes the SQLite database and loads the main user interface defined in FXML.
 */
public class Main extends Application {

    // Global reference to the main application window (used by controllers for dialogs and alerts)
    public static Stage rootWindow;

    @Override
    public void start(Stage rootWindow) {
        try {
            // Store reference to the primary stage for global access
            Main.rootWindow = rootWindow;

            // ✅ Initialize the database (creates new DB if not already present)
            DatabaseManager.initializeDatabase();

            // Load the main UI layout from the FXML file
            FXMLLoader loader = new FXMLLoader(Main.class.getClassLoader().getResource("main.fxml"));
            Scene scene = new Scene(loader.load());

            // Configure window appearance and behavior
            rootWindow.setScene(scene);
            rootWindow.setTitle("SwiftShare");
            rootWindow.setMinWidth(706);
            rootWindow.setMinHeight(372);
            rootWindow.setOnCloseRequest(e -> System.exit(0));
            rootWindow.show();

            // Confirmation message in the console
            System.out.println("✅ SwiftShare started successfully. Database ready.");

        } catch (IOException e) {
            // Handle UI loading issues (e.g., missing FXML or resource errors)
            System.err.println("❌ Failed to load UI: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // Handle any unexpected startup issues
            System.err.println("⚠️ Unexpected error during startup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Application entry point — launches the JavaFX runtime
    public static void main(String[] args) {
        launch(args);
    }
}
