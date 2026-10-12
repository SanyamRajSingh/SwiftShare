package kanin.fileportal;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static kanin.fileportal.Main.rootWindow;

/**
 * Controller
 *
 * Central JavaFX Controller for the SwiftShare main window (main.fxml).
 * Bridges the presentation layer with backend P2P networking and SQLite history services.
 *
 * Key Responsibilities:
 * - Handles user input for IP address, port, and host toggle.
 * - Manages desktop FileChooser and DirectoryChooser file selection dialogs.
 * - Validates input parameters before initiating transfers.
 * - Spawns FileTransferThread instances on worker threads to keep the UI fully responsive.
 * - Launches the HistoryWindow dialog.
 */
public class Controller implements Initializable {

    // ==================================================
    // FXML UI Component Injections
    // ==================================================
    @FXML
    private TextField ipInput;
    @FXML
    private TextField portInput;
    @FXML
    private TextField uploadPath;
    @FXML
    private TextField downloadPath;
    @FXML
    private VBox transferContainer;
    @FXML
    private CheckBox hosting;

    // Accordion container displaying active transfer progress cards
    public static final Accordion transferList = new Accordion();

    private static final int DEFAULT_PORT = 54000;

    // ==================================================
    // Initialization
    // ==================================================

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Attach the global transfer accordion to the UI container
        if (!transferContainer.getChildren().contains(transferList)) {
            transferContainer.getChildren().add(transferList);
        }
    }

    // ==================================================
    // File and Directory Selection
    // ==================================================

    /** Opens FileChooser dialog to select a source file to send */
    @FXML
    public void setFileToUpload() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("SwiftShare — Choose File to Send");
        File file = chooser.showOpenDialog(rootWindow);
        if (file != null) {
            uploadPath.setText(file.getAbsolutePath());
        }
    }

    /** Opens DirectoryChooser dialog to select destination folder for received files */
    @FXML
    public void setSaveLocation() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("SwiftShare — Choose Save Destination");
        File folder = chooser.showDialog(rootWindow);
        if (folder != null) {
            downloadPath.setText(folder.getAbsolutePath());
        }
    }

    // ==================================================
    // Transfer Triggers (Send & Receive)
    // ==================================================

    /** Initiates outgoing file transmission (Send mode) */
    @FXML
    public void upload() {
        transfer(false);
    }

    /** Initiates incoming file reception (Receive mode) */
    @FXML
    public void download() {
        transfer(true);
    }

    // ==================================================
    // Core Transfer Dispatcher
    // ==================================================

    /**
     * Validates user inputs and launches a FileTransferThread worker thread.
     *
     * @param download true if receiving a file into a directory, false if sending a file
     */
    private void transfer(boolean download) {
        String ip = ipInput.getText().trim();
        int port = DEFAULT_PORT;

        // 1. Validate custom port input
        try {
            int input = Integer.parseInt(portInput.getText().trim());
            if (input > 1024 && input < 65536) {
                port = input;
            }
        } catch (NumberFormatException ignored) {}

        // 2. Validate file and path configuration
        File file;
        boolean valid;
        String[] fileErrorMsgs;
        String confirmMsg;

        if (download) {
            file = new File(downloadPath.getText().trim());
            valid = file.exists() && file.isDirectory();
            fileErrorMsgs = new String[]{"Error: Invalid Save Location", "Please select an existing and writable folder."};
            confirmMsg = String.format("Confirm incoming file transfer to '%s' on %s:%d",
                    file.getName(), hosting.isSelected() ? "localhost" : ip, port);
        } else {
            file = new File(uploadPath.getText().trim());
            valid = file.exists() && file.isFile();
            fileErrorMsgs = new String[]{"Error: Desired File Unavailable", "Please select a valid and accessible file path."};
            confirmMsg = String.format("Confirm outbound file transfer of '%s' on %s:%d",
                    file.getName(), hosting.isSelected() ? "localhost" : ip, port);
        }

        // 3. Confirm and dispatch transfer thread
        if (valid) {
            if (alertMsg(confirmMsg, "Press OK to proceed with the transfer.", Alert.AlertType.CONFIRMATION)) {
                if (hosting.isSelected()) {
                    new FileTransferThread(file, port).start();
                } else if (!ip.isEmpty()) {
                    new FileTransferThread(file, port, ip).start();
                } else {
                    alertMsg("Connection Error", "Please provide a valid IP address or check 'Host'.", Alert.AlertType.ERROR);
                }
            }
        } else {
            alertMsg(fileErrorMsgs[0], fileErrorMsgs[1], Alert.AlertType.ERROR);
        }
    }

    // ==================================================
    // History Window Display
    // ==================================================

    /** Opens the SQLite Transfer History window */
    @FXML
    public void showHistory() {
        try {
            HistoryWindow.show();
        } catch (Exception e) {
            e.printStackTrace();
            alertMsg("Error", "Failed to open transfer history: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ==================================================
    // Dialog Utility
    // ==================================================

    /**
     * Displays a JavaFX Alert dialog.
     * Supports both two-button confirmation (OK/Cancel) and single-button notification dialogs.
     *
     * @param content header text of the alert
     * @param subtext content details
     * @param type    the Alert.AlertType
     * @return true if the user confirmed (OK), false otherwise
     */
    public static boolean alertMsg(String content, String subtext, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("SwiftShare");
        alert.setHeaderText(content);
        alert.setContentText(subtext);

        if (type == Alert.AlertType.CONFIRMATION) {
            alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        } else {
            alert.getButtonTypes().setAll(ButtonType.OK);
        }

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}

