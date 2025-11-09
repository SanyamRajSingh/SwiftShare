package kanin.fileportal;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import kanin.fileportal.security.EncryptionUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static kanin.fileportal.Controller.alertMsg;

/**
 * FileTransferThread manages the entire process of file sending and receiving.
 * Each file transfer runs in its own thread to ensure non-blocking parallel operations.
 */
public class FileTransferThread extends Thread {

    // ---------- Connection and File Variables ----------
    private Socket client;
    private ServerSocket host;
    private final File transferFile;
    private final int port;
    private boolean pauseFlag = false;
    private String ip;
    private File inboundFile;

    // ---------- UI Components for Transfer Progress ----------
    private final ProgressBar bar = new ProgressBar(0);
    private final TitledPane infoCard = new TitledPane();
    private final Label status = new Label("Status: Created");

    // ---------- Constructor for Host (Sender) ----------
    public FileTransferThread(File file, int port) {
        this.transferFile = file;
        this.port = port;

        // Create UI Card for ongoing transfer
        this.infoCard.getStyleClass().add("transferThread");
        VBox container = new VBox();
        bar.prefWidthProperty().bind(container.widthProperty());
        container.getChildren().addAll(
                this.status,
                new Label(String.format("Path: '%s'", file.getAbsolutePath())),
                this.bar
        );

        // Context menu for pausing or cancelling transfer
        ContextMenu menu = new ContextMenu();
        MenuItem pause = new MenuItem("Pause"),
                cancel = new MenuItem("Cancel");
        pause.setOnAction(e -> {
            if (this.client != null)
                if (pause.getText().equalsIgnoreCase("Pause")) {
                    this.pauseFlag = true;
                    pause.setText("Resume");
                    statusUpdate("[PAUSED]");
                } else {
                    this.pauseFlag = false;
                    pause.setText("Pause");
                    statusUpdate("[RESUMED]");
                }
        });
        cancel.setOnAction(e -> {
            Controller.transferList.getPanes().remove(this.infoCard);
            if (this.isAlive()) this.interrupt();
            disconnect();
        });
        menu.getItems().addAll(pause, cancel);
        this.infoCard.setContextMenu(menu);
        this.infoCard.setContent(container);
    }

    // ---------- Constructor for Client (Receiver) ----------
    public FileTransferThread(File file, int port, String ip) {
        this(file, port);
        this.ip = ip;
    }

    // ---------- Main Thread Logic ----------
    @Override
    public void run() {
        try {
            // 🔌 Establish Connection between Host and Client
            if (this.ip == null) { // Host Mode
                this.host = new ServerSocket(this.port);
                Platform.runLater(() -> {
                    this.infoCard.setText("Host Connection @localhost:" + this.port);
                    statusUpdate("Waiting...");
                    Controller.transferList.getPanes().add(this.infoCard);
                });
                this.client = this.host.accept(); // Wait for client connection
            } else { // Client Mode
                this.client = new Socket(this.ip, this.port);
                Platform.runLater(() -> {
                    this.infoCard.setText(String.format("Remote Connection @%s:%d", this.ip, this.port));
                    statusUpdate("Connected");
                    Controller.transferList.getPanes().add(this.infoCard);
                });
            }

            long start = System.currentTimeMillis();

            // 🚀 Execute File Transfer (Send or Receive)
            if (this.transferFile.isDirectory())
                incomingTransfer();
            else
                outgoingTransfer();

            // ✅ Log transfer success in database
            String sender = (ip == null) ? "Host" : "Client";
            String receiver = (ip == null) ? "Receiver" : "Host";
            DatabaseManager.insertTransfer(
                    (inboundFile != null ? inboundFile.getName() : transferFile.getName()),
                    sender,
                    receiver,
                    (inboundFile != null ? inboundFile.length() : transferFile.length()),
                    "Success"
            );

            // ✅ Notify User of Successful Transfer
            Platform.runLater(() -> {
                String fileName = (inboundFile != null) ? inboundFile.getName() : transferFile.getName();
                String subtext = "Elapsed time: " + (System.currentTimeMillis() - start) / 1000 + " seconds";
                if (inboundFile != null)
                    subtext += String.format("\nSaved to '%s'", inboundFile.getAbsolutePath());
                Controller.transferList.getPanes().remove(this.infoCard);
                alertMsg(fileName + " successfully transferred.", subtext, Alert.AlertType.INFORMATION);
            });

        } catch (Exception e) {
            e.printStackTrace();

            // ❌ Log transfer failure
            String fileName = (transferFile != null) ? transferFile.getName() : "Unknown";
            long fileSize = (transferFile != null) ? transferFile.length() : 0;

            DatabaseManager.insertTransfer(
                    fileName,
                    (ip == null) ? "Host" : "Client",
                    (ip == null) ? "Receiver" : ip,
                    fileSize,
                    "Failed: " + e.getMessage()
            );

            // Display error alert in UI
            Platform.runLater(() -> alertMsg(
                    "Error: " + e.getMessage(),
                    "File transfer failed.",
                    Alert.AlertType.ERROR
            ));
        } finally {
            // Disconnect sockets and cleanup UI
            disconnect();
            Platform.runLater(() -> Controller.transferList.getPanes().remove(this.infoCard));
        }
    }

    // ---------- UI Helper for Status Update ----------
    private void statusUpdate(String s) {
        Platform.runLater(() -> this.status.setText("Status: " + s));
    }

    // ---------- Outgoing (Send) File Transfer ----------
    private void outgoingTransfer() throws IOException {
        try {
            PrintWriter writer = new PrintWriter(client.getOutputStream(), true);
            writer.println(transferFile.getName() + (char) 28 + transferFile.length());

            statusUpdate("Encrypting and sending '" + transferFile.getName() + "'...");

            byte[] fileData = readFileBytes(transferFile);
            byte[] encryptedData = EncryptionUtil.encrypt(fileData);

            transfer(new ByteArrayInputStream(encryptedData),
                    new BufferedOutputStream(client.getOutputStream()),
                    encryptedData.length);

            statusUpdate("File sent successfully.");

        } catch (Exception e) {
            throw new IOException("Outgoing transfer error: " + e.getMessage(), e);
        }
    }

    // ---------- Incoming (Receive) File Transfer ----------
    private void incomingTransfer() throws IOException {
        String directory = transferFile.getAbsolutePath();
        BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
        String[] info = input.readLine().split("" + (char) 28);
        this.inboundFile = new File(directory + "/" + info[0]);
        while (!this.inboundFile.createNewFile()) {
            this.inboundFile = new File(directory + "/copy_" + info[0]);
        }

        statusUpdate("Receiving and decrypting '" + inboundFile.getName() + "'...");

        ByteArrayOutputStream receivedData = new ByteArrayOutputStream();
        transfer(new BufferedInputStream(this.client.getInputStream()),
                new BufferedOutputStream(receivedData),
                Long.parseLong(info[1]));

        try {
            byte[] decryptedData = EncryptionUtil.decrypt(receivedData.toByteArray());
            writeFileBytes(inboundFile, decryptedData);
            statusUpdate("Decryption complete.");
        } catch (Exception e) {
            throw new IOException("Incoming transfer error: " + e.getMessage(), e);
        }
    }

    // ---------- Core Byte Transfer Logic ----------
    private void transfer(InputStream in, OutputStream out, long size) throws IOException {
        try {
            int amt;
            double total = 0;
            byte[] buffer = new byte[1024 * 8];
            while ((amt = in.read(buffer)) != -1) {
                synchronized (this) {
                    if (!pauseFlag) {
                        out.write(buffer, 0, amt);
                        out.flush();
                        final double progress = (total += amt) / size;
                        Platform.runLater(() -> bar.setProgress(progress));
                    }
                }
            }
        } finally {
            in.close();
            out.close();
        }
    }

    // ---------- Disconnect and Cleanup ----------
    private void disconnect() {
        try {
            if (this.client != null)
                this.client.close();
            if (this.host != null)
                this.host.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ---------- Utility: Read and Write File Bytes ----------
    private static byte[] readFileBytes(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return fis.readAllBytes();
        }
    }

    private static void writeFileBytes(File file, byte[] data) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        }
    }
}
