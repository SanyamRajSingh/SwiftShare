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

import static kanin.fileportal.Main.*;

/**
 * Controller class for handling all user actions and UI updates in SwiftShare.
 * Acts as a bridge between the JavaFX UI and backend logic.
 */
public class Controller implements Initializable {

    // ---------- UI Components (from FXML) ----------
    @FXML
    private TextField ipInput, portInput, uploadPath, downloadPath;
    @FXML
    private VBox transferContainer;
    @FXML
    private CheckBox hosting;

    // Accordion to show active transfer progress
    public static final Accordion transferList = new Accordion();

    // ---------- Initialization ----------
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Add the transfer list to the container when the app starts
        transferContainer.getChildren().addAll(transferList);
    }

    // ---------- File and Folder Selection ----------

    /** Opens a file chooser to select the file to upload */
    @FXML
    public void setFileToUpload() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose a File to Send");
        File file = chooser.showOpenDialog(rootWindow);
        if (file != null) {
            uploadPath.setText(file.getAbsolutePath());
        }
    }

    /** Opens a directory chooser to set the save location for received files */
    @FXML
    public void setSaveLocation() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose Save Location");
        File folder = chooser.showDialog(rootWindow);
        if (folder != null) {
            downloadPath.setText(folder.getAbsolutePath());
        }
    }

    // ---------- Upload / Download Buttons ----------

    /** Starts the upload (send) process */
    @FXML
    public void upload() {
        transfer(false);
    }

    /** Starts the download (receive) process */
    @FXML
    public void download() {
        transfer(true);
    }

    // ---------- Core Transfer Logic ----------

    /** Handles both upload and download processes depending on the boolean flag */
    private void transfer(boolean download) {
        String ip = ipInput.getText().trim();
        int port = 54000; // Default port

        // Validate custom port input
        try {
            int input = Integer.parseInt(portInput.getText().trim());
            if (input > 1024 && input < 65536) {
                port = input;
            }
        } catch (Exception ignored) {}

        // Validate file and directory paths
        File file = new File(uploadPath.getText().trim());
        boolean valid = !file.isDirectory();
        String[] fileErrorMsgs = {"Error: Desired File Unavailable.", "Please enter a valid and accessible file path"};
        String confirmMsg = String.format("Confirm outbound file transfer of '%s' on %s:%d",
                file.getName(), hosting.isSelected() ? "localhost" : ip, port);

        // Adjust conditions for download
        if (download) {
            file = new File(downloadPath.getText().trim());
            valid = file.isDirectory();
            fileErrorMsgs = new String[]{"Error: Invalid Save Location.", "Please enter a valid working directory"};
            confirmMsg = String.format("Confirm inbound file transfer to '%s' on %s:%d", file.getName(), ip, port);
        }

        // Execute transfer based on user input and mode (host/client)
        if (file.exists() && valid) {
            if (alertMsg(confirmMsg, "Please press OK to confirm.", Alert.AlertType.CONFIRMATION)) {
                if (hosting.isSelected()) {
                    new FileTransferThread(file, port).start();
                } else if (!ip.trim().isEmpty()) {
                    new FileTransferThread(file, port, ip).start();
                } else {
                    alertMsg("Error: Failed to establish connection",
                            "Please enter a valid IP Address or check your settings.",
                            Alert.AlertType.ERROR);
                }
            }
        } else {
            alertMsg(fileErrorMsgs[0], fileErrorMsgs[1], Alert.AlertType.ERROR);
        }
    }

    // ---------- Common Alert Utility ----------

    /** Displays alert dialogs (confirmation, error, info) */
    public static boolean alertMsg(String content, String subtext, Alert.AlertType type) {
        Alert alert = new Alert(type, subtext, ButtonType.OK);
        alert.setTitle("SwiftShare");
        alert.setHeaderText(content);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // ---------- Transfer History Window ----------

    /** Opens the Transfer History window showing all previous transfers */
    @FXML
    public void showHistory() {
        try {
            HistoryWindow.show();
        } catch (Exception e) {
            e.printStackTrace();
            alertMsg("Error", "Failed to open transfer history: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}
