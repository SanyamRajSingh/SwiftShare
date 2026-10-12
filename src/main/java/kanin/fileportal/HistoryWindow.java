package kanin.fileportal;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

/**
 * HistoryWindow
 *
 * Dedicated window for inspecting SwiftShare transfer history logged in SQLite.
 *
 * Highlights:
 * - Displays records in a JavaFX TableView bound to TransferRecord properties.
 * - Supports both on-demand manual refresh and automatic periodic polling (every 5 seconds).
 * - Offloads SQLite database queries to a background thread to prevent UI freezing.
 * - Manages auto-refresh timeline lifecycle cleanly per window instance.
 */
public class HistoryWindow {

    // ==================================================
    // State & Timer Management
    // ==================================================
    private static Timeline autoRefreshTimeline;

    // ==================================================
    // Window Display & Assembly
    // ==================================================

    /**
     * Creates, styles, and displays the Transfer History window.
     */
    public static void show() {
        Stage stage = new Stage();
        stage.setTitle("SwiftShare — Transfer History");

        // 1. TableView Setup and Columns Configuration
        TableView<TransferRecord> table = new TableView<>();
        table.getStyleClass().add("history-table");

        TableColumn<TransferRecord, String> fileCol = new TableColumn<>("File Name");
        fileCol.setCellValueFactory(c -> c.getValue().fileNameProperty());
        fileCol.setPrefWidth(180);

        TableColumn<TransferRecord, String> senderCol = new TableColumn<>("Sender");
        senderCol.setCellValueFactory(c -> c.getValue().senderProperty());
        senderCol.setPrefWidth(100);

        TableColumn<TransferRecord, String> receiverCol = new TableColumn<>("Receiver");
        receiverCol.setCellValueFactory(c -> c.getValue().receiverProperty());
        receiverCol.setPrefWidth(100);

        TableColumn<TransferRecord, String> sizeCol = new TableColumn<>("Size (bytes)");
        sizeCol.setCellValueFactory(c -> c.getValue().fileSizeProperty());
        sizeCol.setPrefWidth(100);

        TableColumn<TransferRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(c -> c.getValue().transferDateProperty());
        dateCol.setPrefWidth(160);

        TableColumn<TransferRecord, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> c.getValue().statusProperty());
        statusCol.setPrefWidth(120);

        table.getColumns().addAll(fileCol, senderCol, receiverCol, sizeCol, dateCol, statusCol);

        // 2. Header and Controls
        Label title = new Label("📦 Transfer History");
        title.setStyle(
                "-fx-font-size: 20px;" +
                "-fx-text-fill: linear-gradient(to right, #00c6ff, #0078ff);" +
                "-fx-font-weight: bold;"
        );

        Label refreshLabel = new Label("Auto-refreshing every 5 seconds...");
        refreshLabel.setStyle("-fx-text-fill: #bbbbbb; -fx-font-size: 12px;");

        Button manualRefresh = new Button("↻ Refresh Now");
        manualRefresh.setOnAction(e -> fetchAndPopulate(table));
        manualRefresh.setStyle(
                "-fx-background-color: linear-gradient(to right, #0078ff, #00c6ff);" +
                "-fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-background-radius: 8; -fx-padding: 6 16 6 16;" +
                "-fx-cursor: hand;"
        );

        HBox topBar = new HBox(15, title, manualRefresh);
        topBar.setPadding(new Insets(5, 0, 10, 0));

        // 3. Root Container
        VBox root = new VBox(10, topBar, refreshLabel, table);
        root.setPadding(new Insets(20));
        root.setPrefSize(840, 500);
        root.setStyle("-fx-background-color: #121212; -fx-border-color: #2b2b2b; -fx-border-radius: 8;");

        // 4. Scene and Window Display
        Scene scene = new Scene(root);
        try {
            scene.getStylesheets().add(Main.class.getResource("/main.css").toExternalForm());
        } catch (Exception ignored) {}

        stage.setScene(scene);
        stage.show();

        // 5. Start Auto-Refresh Timer and cleanup on window close
        startAutoRefresh(table);
        stage.setOnCloseRequest(event -> stopAutoRefresh());
    }

    // ==================================================
    // Data Loading (Offloaded to Background Worker Thread)
    // ==================================================

    /**
     * Queries the database on a background thread and updates the TableView on the JavaFX thread.
     */
    private static void fetchAndPopulate(TableView<TransferRecord> table) {
        Thread worker = new Thread(() -> {
            try {
                List<TransferRecord> records = DatabaseManager.fetchTransfers();
                Platform.runLater(() -> table.getItems().setAll(records));
            } catch (Exception e) {
                System.err.println("⚠️ Error loading history records: " + e.getMessage());
            }
        });
        worker.setDaemon(true);
        worker.setName("HistoryLoader-Thread");
        worker.start();
    }

    // ==================================================
    // Auto-Refresh Timer Management
    // ==================================================

    private static void startAutoRefresh(TableView<TransferRecord> table) {
        stopAutoRefresh();

        // Initial immediate load
        fetchAndPopulate(table);

        // Schedule recurring 5-second polling
        autoRefreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(5), event -> fetchAndPopulate(table))
        );
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    private static void stopAutoRefresh() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
            autoRefreshTimeline = null;
        }
    }
}

