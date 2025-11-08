package kanin.fileportal;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Entry point for the SwiftShare application.
 * Initializes the database and loads the main JavaFX UI.
 */
public class Main extends Application {

    // Static reference for use in dialogs and controllers
    public static Stage rootWindow;

    @Override
    public void start(Stage rootWindow) {
        try {
            // Store root window reference for dialogs
            Main.rootWindow = rootWindow;

            // ✅ Initialize SQLite database (creates or upgrades schema automatically)
            DatabaseManager.initializeDatabase();

            // Load the main UI
            FXMLLoader loader = new FXMLLoader(Main.class.getClassLoader().getResource("main.fxml"));
            Scene scene = new Scene(loader.load());

            // Apply window settings
            rootWindow.setScene(scene);
            rootWindow.setTitle("SwiftShare");
            rootWindow.setMinWidth(706);
            rootWindow.setMinHeight(372);
            rootWindow.setOnCloseRequest(e -> System.exit(0));
            rootWindow.show();

            // Console confirmation
            System.out.println("✅ SwiftShare started successfully. Database ready.");

        } catch (IOException e) {
            System.err.println("❌ Failed to load UI: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("⚠️ Unexpected error during startup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
