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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 💾 SwiftShare Transfer History Window
 * Modern dark mode with auto-refresh and manual reload.
 */
public class HistoryWindow {

    private static final String DB_URL = "jdbc:sqlite:swiftshare.db";
    private static Timeline autoRefreshTimeline;

    public static void show() {
        Stage stage = new Stage();
        stage.setTitle("Transfer History");

        // 🧾 Table Setup
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
        dateCol.setPrefWidth(150);

        TableColumn<TransferRecord, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> c.getValue().statusProperty());
        statusCol.setPrefWidth(100);

        table.getColumns().addAll(fileCol, senderCol, receiverCol, sizeCol, dateCol, statusCol);

        // 🎨 Header Title
        Label title = new Label("📦 SwiftShare Transfer History");
        title.setStyle(
                "-fx-font-size: 20px;" +
                "-fx-text-fill: linear-gradient(to right, #00c6ff, #0078ff);" +
                "-fx-font-weight: bold;"
        );

        // 🕓 Auto-refresh label
        Label refreshLabel = new Label("Auto-refreshing every 5 seconds...");
        refreshLabel.setStyle("-fx-text-fill: #bbbbbb; -fx-font-size: 12px;");

        // 🔄 Manual refresh button
        Button manualRefresh = new Button("↻ Refresh Now");
        manualRefresh.setOnAction(e -> new Thread(() -> loadData(table)).start());
        manualRefresh.setStyle(
                "-fx-background-color: linear-gradient(to right, #0078ff, #00c6ff);" +
                "-fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-background-radius: 8; -fx-padding: 6 16 6 16;" +
                "-fx-cursor: hand;"
        );
        manualRefresh.setOnMouseEntered(e -> manualRefresh.setStyle(
                "-fx-background-color: linear-gradient(to right, #0096ff, #33ccff);" +
                "-fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-background-radius: 8; -fx-padding: 6 16 6 16;" +
                "-fx-cursor: hand;"
        ));
        manualRefresh.setOnMouseExited(e -> manualRefresh.setStyle(
                "-fx-background-color: linear-gradient(to right, #0078ff, #00c6ff);" +
                "-fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-background-radius: 8; -fx-padding: 6 16 6 16;" +
                "-fx-cursor: hand;"
        ));

        // 🧭 Top section layout
        HBox topBar = new HBox(15, title, manualRefresh);
        topBar.setPadding(new Insets(5, 0, 10, 0));

        // 🖼 Root container
        VBox root = new VBox(10, topBar, refreshLabel, table);
        root.setPadding(new Insets(20));
        root.setPrefSize(800, 480);
        root.setStyle("-fx-background-color: #121212; -fx-border-color: #2b2b2b; -fx-border-radius: 8;");

        Scene scene = new Scene(root);
        scene.getStylesheets().add(Main.class.getResource("/main.css").toExternalForm());

        stage.setScene(scene);
        stage.show();

        // 🔁 Start auto-refreshing
        startAutoRefresh(table);

        // 🛑 Stop when window closes
        stage.setOnCloseRequest(event -> stopAutoRefresh());
    }

    /** 🔁 Load data from database */
    private static void loadData(TableView<TransferRecord> table) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM transfer_history ORDER BY transfer_date DESC")) {

            Platform.runLater(() -> table.getItems().clear());

            while (rs.next()) {
                TransferRecord record = new TransferRecord(
                        rs.getString("file_name"),
                        rs.getString("sender"),
                        rs.getString("receiver"),
                        String.valueOf(rs.getLong("size_bytes")),
                        rs.getString("transfer_date"),
                        rs.getString("status")
                );
                Platform.runLater(() -> table.getItems().add(record));
            }

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        "Error loading history: " + e.getMessage(),
                        ButtonType.OK);
                alert.setTitle("Database Error");
                alert.showAndWait();
            });
        }
    }

    /** 🕓 Start auto-refresh every 5 seconds */
    private static void startAutoRefresh(TableView<TransferRecord> table) {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
        }

        autoRefreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0), event -> new Thread(() -> loadData(table)).start()),
                new KeyFrame(Duration.seconds(5))
        );
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    /** 🛑 Stop refreshing */
    private static void stopAutoRefresh() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
            autoRefreshTimeline = null;
        }
    }
}
