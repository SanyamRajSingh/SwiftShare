package kanin.fileportal;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TransferRecord {
    private final StringProperty fileName;
    private final StringProperty sender;
    private final StringProperty receiver;
    private final StringProperty fileSize;
    private final StringProperty transferDate;
    private final StringProperty status;

    public TransferRecord(String fileName, String sender, String receiver, String fileSize, String transferDate, String status) {
        this.fileName = new SimpleStringProperty(fileName);
        this.sender = new SimpleStringProperty(sender);
        this.receiver = new SimpleStringProperty(receiver);
        this.fileSize = new SimpleStringProperty(fileSize);
        this.transferDate = new SimpleStringProperty(transferDate);
        this.status = new SimpleStringProperty(status);
    }

    public StringProperty fileNameProperty() { return fileName; }
    public StringProperty senderProperty() { return sender; }
    public StringProperty receiverProperty() { return receiver; }
    public StringProperty fileSizeProperty() { return fileSize; }
    public StringProperty transferDateProperty() { return transferDate; }
    public StringProperty statusProperty() { return status; }
}
