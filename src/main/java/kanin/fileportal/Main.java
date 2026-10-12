package kanin.fileportal;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main
 *
 * Primary JavaFX Application entry point for SwiftShare.
 *
 * Architecture Highlights:
 * - Bootstrapped via Launcher.java to bypass the Java 11+ unnamed module / module-path checks
 *   when executing from a shaded fat JAR.
 * - Initializes the SQLite database schema (via DatabaseManager) upon startup.
 * - Loads the declarative FXML interface (main.fxml) and attaches the root Stage.
 * - Holds a static reference to the primary Stage (rootWindow) used for modal file choosers.
 */
public class Main extends Application {

    // ==================================================
    // Global Window Reference
    // ==================================================
    public static Stage rootWindow;

    // ==================================================
    // JavaFX Application Lifecycle
    // ==================================================

    @Override
    public void start(Stage primaryStage) {
        try {
            // Retain reference to root stage for dialogs and alerts
            Main.rootWindow = primaryStage;

            // Step 1: Ensure SQLite database schema is initialized
            DatabaseManager.initializeDatabase();

            // Step 2: Load declarative layout from FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
            Scene scene = new Scene(loader.load());

            // Step 3: Configure primary stage properties
            primaryStage.setScene(scene);
            primaryStage.setTitle("SwiftShare — Peer-to-Peer File Transfer");
            primaryStage.setMinWidth(706);
            primaryStage.setMinHeight(372);

            // Terminate background threads on application window closure
            primaryStage.setOnCloseRequest(event -> System.exit(0));

            // Step 4: Render window
            primaryStage.show();
            System.out.println("✅ SwiftShare started successfully. Database ready.");

        } catch (IOException e) {
            System.err.println("❌ Failed to load UI resources (main.fxml): " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("⚠️ Unexpected error during application startup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================================================
    // Main Method (Delegated from Launcher)
    // ==================================================

    public static void main(String[] args) {
        launch(args);
    }
}

