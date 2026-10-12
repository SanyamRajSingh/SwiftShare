package kanin.fileportal;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * TransferRecord represents a single file transfer record in SwiftShare.
 *
 * It serves as the data model for the transfer history table in the UI.
 * Each instance stores details about a completed, failed, or cancelled transfer:
 * - File name
 * - Sender (role or IP)
 * - Receiver (role or IP)
 * - File size in bytes
 * - Timestamp of the transfer
 * - Transfer status (e.g., "Success", "Failed: ...")
 *
 * It exposes both JavaFX StringProperty accessors for TableView data binding
 * and standard JavaBean getters for direct value access.
 */
public class TransferRecord {

    // ==================================================
    // Model Properties (Observable for JavaFX TableView)
    // ==================================================
    private final StringProperty fileName;
    private final StringProperty sender;
    private final StringProperty receiver;
    private final StringProperty fileSize;
    private final StringProperty transferDate;
    private final StringProperty status;

    // ==================================================
    // Constructor
    // ==================================================

    /**
     * Constructs a new TransferRecord with string values for display.
     *
     * @param fileName     the name of the file
     * @param sender       the sender identity (e.g., "Host", "Client", or IP)
     * @param receiver     the receiver identity (e.g., "Host", "Client", or IP)
     * @param fileSize     the size of the file in bytes as a string
     * @param transferDate the formatted timestamp of the transfer
     * @param status       the final status of the transfer
     */
    public TransferRecord(String fileName, String sender, String receiver, 
                          String fileSize, String transferDate, String status) {
        this.fileName = new SimpleStringProperty(fileName != null ? fileName : "Unknown");
        this.sender = new SimpleStringProperty(sender != null ? sender : "Unknown");
        this.receiver = new SimpleStringProperty(receiver != null ? receiver : "Unknown");
        this.fileSize = new SimpleStringProperty(fileSize != null ? fileSize : "0");
        this.transferDate = new SimpleStringProperty(transferDate != null ? transferDate : "");
        this.status = new SimpleStringProperty(status != null ? status : "Unknown");
    }

    // ==================================================
    // Property Accessors (Used by JavaFX TableView binding)
    // ==================================================

    public StringProperty fileNameProperty() {
        return fileName;
    }

    public StringProperty senderProperty() {
        return sender;
    }

    public StringProperty receiverProperty() {
        return receiver;
    }

    public StringProperty fileSizeProperty() {
        return fileSize;
    }

    public StringProperty transferDateProperty() {
        return transferDate;
    }

    public StringProperty statusProperty() {
        return status;
    }

    // ==================================================
    // Standard JavaBean Getters
    // ==================================================

    public String getFileName() {
        return fileName.get();
    }

    public String getSender() {
        return sender.get();
    }

    public String getReceiver() {
        return receiver.get();
    }

    public String getFileSize() {
        return fileSize.get();
    }

    public String getTransferDate() {
        return transferDate.get();
    }

    public String getStatus() {
        return status.get();
    }

    // ==================================================
    // Object Overrides
    // ==================================================

    @Override
    public String toString() {
        return String.format("TransferRecord[file=%s, from=%s, to=%s, size=%s bytes, date=%s, status=%s]",
                getFileName(), getSender(), getReceiver(), getFileSize(), getTransferDate(), getStatus());
    }
}

