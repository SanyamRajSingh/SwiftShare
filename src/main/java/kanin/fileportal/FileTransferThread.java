package kanin.fileportal;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import kanin.fileportal.security.EncryptionUtil;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;

import static kanin.fileportal.Controller.alertMsg;

/**
 * FileTransferThread
 *
 * Worker thread responsible for managing a single peer-to-peer (P2P) file transfer.
 * Handles socket connectivity (Host/Server or Client/Connector), streaming encryption (AES-GCM),
 * UI progress visualization via JavaFX TitledPane, pause/resume signaling, and SQLite database logging.
 *
 * Core Highlights:
 * - 64 KB chunked streaming I/O prevents OutOfMemory errors on arbitrary file sizes.
 * - Authenticated AES-GCM encryption with 128-bit dynamic key and 12-byte IV per transfer.
 * - Staging incoming files in temporary .part files to prevent corrupted/unauthenticated output on disk.
 * - Thread-safe pause/resume coordination using wait() and notifyAll().
 * - Cleans up partial or incomplete files upon cancellation or transfer failure.
 */
public class FileTransferThread extends Thread {

    // ==================================================
    // Networking & State Variables
    // ==================================================
    private Socket client;
    private ServerSocket host;
    private final File transferFile;
    private final int port;
    private String ip;
    private File inboundFile;
    private File tempInboundFile;
    private String resolvedFileName;

    private volatile boolean pauseFlag = false;
    private volatile boolean cancelled = false;

    // Default socket read timeout (30 seconds)
    private static final int SOCKET_TIMEOUT_MS = 30000;

    // ==================================================
    // UI Components for Live Progress Tracking
    // ==================================================
    private final ProgressBar bar = new ProgressBar(0);
    private final TitledPane infoCard = new TitledPane();
    private final Label status = new Label("Status: Created");

    // ==================================================
    // Constructors
    // ==================================================

    /**
     * Constructor for Host mode (waits for an incoming peer connection).
     *
     * @param file the source file to send, or target directory to receive into
     * @param port the TCP port to listen on
     */
    public FileTransferThread(File file, int port) {
        this.transferFile = file;
        this.port = port;

        // Configure UI Card for this transfer
        this.infoCard.getStyleClass().add("transferThread");
        VBox container = new VBox();
        bar.prefWidthProperty().bind(container.widthProperty());
        container.getChildren().addAll(
                this.status,
                new Label(String.format("Path: '%s'", file.getAbsolutePath())),
                this.bar
        );

        // Context menu for pausing or cancelling this transfer
        ContextMenu menu = new ContextMenu();
        MenuItem pauseItem = new MenuItem("Pause");
        MenuItem cancelItem = new MenuItem("Cancel");

        pauseItem.setOnAction(e -> {
            if (pauseItem.getText().equalsIgnoreCase("Pause")) {
                pauseTransfer();
                pauseItem.setText("Resume");
            } else {
                resumeTransfer();
                pauseItem.setText("Pause");
            }
        });

        cancelItem.setOnAction(e -> {
            cancelTransfer();
        });

        menu.getItems().addAll(pauseItem, cancelItem);
        this.infoCard.setContextMenu(menu);
        this.infoCard.setContent(container);
    }

    /**
     * Constructor for Client mode (connects to a remote peer).
     *
     * @param file the source file to send, or target directory to receive into
     * @param port the remote TCP port
     * @param ip   the remote host IP address
     */
    public FileTransferThread(File file, int port, String ip) {
        this(file, port);
        this.ip = ip;
    }

    // ==================================================
    // Thread Execution Lifecycle
    // ==================================================

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        long expectedSize = (transferFile != null && !transferFile.isDirectory()) ? transferFile.length() : 0;

        try {
            // Step 1: Establish TCP connection (Host listens, Client connects)
            if (this.ip == null) {
                // Host Mode: bind ServerSocket and await peer
                this.host = new ServerSocket(this.port);
                Platform.runLater(() -> {
                    this.infoCard.setText("Host Connection @localhost:" + this.port);
                    statusUpdate("Waiting for peer...");
                    Controller.transferList.getPanes().add(this.infoCard);
                });
                this.client = this.host.accept();
            } else {
                // Client Mode: connect to remote host
                Platform.runLater(() -> {
                    this.infoCard.setText(String.format("Remote Connection @%s:%d", this.ip, this.port));
                    statusUpdate("Connecting...");
                    Controller.transferList.getPanes().add(this.infoCard);
                });
                this.client = new Socket(this.ip, this.port);
            }

            // Set socket read timeout to prevent infinite blocking on silent disconnects
            if (this.client != null) {
                this.client.setSoTimeout(SOCKET_TIMEOUT_MS);
            }

            Platform.runLater(() -> statusUpdate("Connected. Starting transfer..."));

            // Step 2: Execute file transfer (Send if file, Receive if directory)
            if (this.transferFile.isDirectory()) {
                incomingTransfer();
            } else {
                outgoingTransfer();
            }

            // Step 3: Compute transfer metadata for database logging
            String role = (ip == null) ? "Host" : "Client";
            String peer = (ip == null) ?
                    (client != null && client.getInetAddress() != null ? client.getInetAddress().getHostAddress() : "Peer") : ip;
            String sender = transferFile.isDirectory() ? peer : role;
            String receiver = transferFile.isDirectory() ? role : peer;
            long loggedSize = (inboundFile != null) ? inboundFile.length() : transferFile.length();
            String loggedFileName = (inboundFile != null) ? inboundFile.getName() : transferFile.getName();

            // Step 4: Record success in SQLite database
            DatabaseManager.insertTransfer(loggedFileName, sender, receiver, loggedSize, "Success");

            // Step 5: Notify user via UI alert
            Platform.runLater(() -> {
                long elapsedSeconds = Math.max(0, (System.currentTimeMillis() - start) / 1000);
                String subtext = "Elapsed time: " + elapsedSeconds + " seconds";
                if (inboundFile != null) {
                    subtext += String.format("\nSaved to '%s'", inboundFile.getAbsolutePath());
                }
                Controller.transferList.getPanes().remove(this.infoCard);
                alertMsg(loggedFileName + " successfully transferred.", subtext, Alert.AlertType.INFORMATION);
            });

        } catch (Exception e) {
            boolean wasCancelled = this.cancelled || this.isInterrupted() || 
                    (e.getMessage() != null && e.getMessage().contains("Socket closed"));

            // Clean up temporary staging file on error, tampering, or cancellation
            if (tempInboundFile != null && tempInboundFile.exists()) {
                try {
                    tempInboundFile.delete();
                    System.out.println("🧹 Cleaned up temporary staging file: " + tempInboundFile.getName());
                } catch (Exception ignored) {}
            }

            // Clean up partial received file on error or cancellation
            if (inboundFile != null && inboundFile.exists()) {
                try {
                    inboundFile.delete();
                    System.out.println("🧹 Cleaned up partial file: " + inboundFile.getName());
                } catch (Exception ignored) {}
            }

            String role = (ip == null) ? "Host" : "Client";
            String peer = (ip == null) ? "Peer" : ip;
            String sender = (transferFile != null && transferFile.isDirectory()) ? peer : role;
            String receiver = (transferFile != null && transferFile.isDirectory()) ? role : peer;
            String fileName = (resolvedFileName != null) ? resolvedFileName :
                    ((inboundFile != null) ? inboundFile.getName() : 
                    (transferFile != null ? transferFile.getName() : "Unknown"));
            long fileSize = (inboundFile != null) ? inboundFile.length() : 
                    (transferFile != null && !transferFile.isDirectory() ? transferFile.length() : expectedSize);

            String statusLabel = wasCancelled ? "Cancelled" : "Failed: " + e.getMessage();

            // Log failure or cancellation in SQLite database
            DatabaseManager.insertTransfer(fileName, sender, receiver, fileSize, statusLabel);

            // Display UI alert (warning for user cancel, error for unexpected failure)
            if (wasCancelled) {
                Platform.runLater(() -> statusUpdate("[CANCELLED]"));
            } else {
                e.printStackTrace();
                Platform.runLater(() -> alertMsg(
                        "Error: " + e.getMessage(),
                        "File transfer failed.",
                        Alert.AlertType.ERROR
                ));
            }
        } finally {
            // Clean up sockets and remove progress card
            disconnect();
            Platform.runLater(() -> Controller.transferList.getPanes().remove(this.infoCard));
        }
    }

    // ==================================================
    // Outgoing Transfer Pipeline (Send)
    // ==================================================

    private void outgoingTransfer() throws IOException {
        try {
            statusUpdate("Encrypting and sending '" + transferFile.getName() + "'...");

            OutputStream rawOut = client.getOutputStream();
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(rawOut));

            // Generate fresh dynamic key and IV per transfer
            SecretKey key = EncryptionUtil.generateKey();
            byte[] keyBytes = key.getEncoded();
            byte[] iv = EncryptionUtil.generateIv();

            // 1. Send binary header
            dos.writeUTF(transferFile.getName());
            dos.writeLong(transferFile.length());
            dos.writeInt(keyBytes.length);
            dos.write(keyBytes);
            dos.writeInt(iv.length);
            dos.write(iv);
            dos.flush();

            // 2. Encrypt and stream file content in 64 KB chunks
            CipherOutputStream cos = EncryptionUtil.createCipherOutputStream(dos, key, iv);
            try (FileInputStream fis = new FileInputStream(transferFile)) {
                transfer(fis, cos, transferFile.length());
            }
            cos.close(); // Flushes trailing GCM authentication tag and finalizes ciphertext

            statusUpdate("File sent successfully.");

        } catch (Exception e) {
            throw new IOException("Outgoing transfer error: " + e.getMessage(), e);
        }
    }

    // ==================================================
    // Incoming Transfer Pipeline (Receive)
    // ==================================================

    private void incomingTransfer() throws IOException {
        try {
            InputStream rawIn = client.getInputStream();
            DataInputStream dis = new DataInputStream(new BufferedInputStream(rawIn));

            // 1. Read binary header
            String remoteFileName = dis.readUTF();
            long fileSize = dis.readLong();

            int keyLen = dis.readInt();
            byte[] keyBytes = new byte[keyLen];
            dis.readFully(keyBytes);
            SecretKey key = EncryptionUtil.getKeyFromBytes(keyBytes);

            int ivLen = dis.readInt();
            byte[] iv = new byte[ivLen];
            dis.readFully(iv);

            // 2. Sanitize filename to prevent directory traversal
            String safeFileName = new File(remoteFileName).getName();
            if (safeFileName.trim().isEmpty() || safeFileName.contains("..")) {
                safeFileName = "downloaded_file";
            }

            // Ensure unique local destination filename
            String directory = transferFile.getAbsolutePath();
            File targetDestination = new File(directory, safeFileName);
            while (targetDestination.exists()) {
                targetDestination = new File(directory, "copy_" + targetDestination.getName());
            }
            this.resolvedFileName = targetDestination.getName();

            // Create uniquely named temporary staging file in the target directory (.filename.timestamp.part)
            // Decrypted bytes are written only to this staging file to prevent unauthenticated/tampered data from polluting destination
            File tempStagingFile = new File(directory, "." + targetDestination.getName() + "." + System.currentTimeMillis() + ".part");
            this.tempInboundFile = tempStagingFile;

            statusUpdate("Receiving and decrypting '" + targetDestination.getName() + "'...");

            // 3. Direct AES-GCM decryption with explicit cipher.update() and cipher.doFinal()
            // This guarantees that AEADBadTagException is never swallowed or concealed by stream boundaries.
            Cipher cipher = EncryptionUtil.createDecryptCipher(key, iv);
            long receivedBytes = 0;
            try (FileOutputStream fos = new FileOutputStream(tempStagingFile)) {
                byte[] buffer = new byte[1024 * 64];
                long lastUpdate = 0;

                while (true) {
                    synchronized (this) {
                        while (pauseFlag) {
                            try {
                                this.wait();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new IOException("Transfer interrupted / cancelled");
                            }
                        }
                    }

                    int amt = dis.read(buffer);
                    if (amt == -1) {
                        break;
                    }

                    byte[] plainChunk = cipher.update(buffer, 0, amt);
                    if (plainChunk != null && plainChunk.length > 0) {
                        fos.write(plainChunk);
                        receivedBytes += plainChunk.length;
                    }

                    long now = System.currentTimeMillis();
                    if (now - lastUpdate > 50 || (fileSize > 0 && receivedBytes >= fileSize)) {
                        lastUpdate = now;
                        final double progress = (fileSize > 0) ? (double) receivedBytes / fileSize : 0.0;
                        Platform.runLater(() -> bar.setProgress(Math.min(1.0, progress)));
                    }
                }

                // Explicit final authentication tag verification.
                // Throws AEADBadTagException immediately if data was tampered, corrupted, or truncated.
                byte[] finalBlock = cipher.doFinal();
                if (finalBlock != null && finalBlock.length > 0) {
                    fos.write(finalBlock);
                    receivedBytes += finalBlock.length;
                }
                fos.flush();
            }

            // Verify transferred size matches header metadata
            if (fileSize >= 0 && receivedBytes != fileSize) {
                throw new IOException("Incomplete transfer: expected " + fileSize + " bytes, received " + receivedBytes + " bytes.");
            }

            // 4. Promote temporary staging file to final destination only after complete decryption and tag verification succeed.
            // Strict No-Overwrite Guarantee: Files.move(source, target) without copy options guarantees that an existing
            // destination file is never replaced. If a FileAlreadyExistsException occurs, we advance to the next 'copy_' prefix and retry.
            boolean promoted = false;
            int maxAttempts = 100;
            while (!promoted && maxAttempts-- > 0) {
                while (targetDestination.exists()) {
                    targetDestination = new File(directory, "copy_" + targetDestination.getName());
                }
                this.resolvedFileName = targetDestination.getName();

                try {
                    Files.move(tempStagingFile.toPath(), targetDestination.toPath());
                    promoted = true;
                } catch (FileAlreadyExistsException e) {
                    // Collision occurred between exists() check and move(): advance name and retry safely
                    targetDestination = new File(directory, "copy_" + targetDestination.getName());
                }
            }

            if (!promoted) {
                throw new IOException("Failed to promote staging file: unable to find an available non-conflicting filename after multiple attempts.");
            }

            this.inboundFile = targetDestination;
            this.tempInboundFile = null;

            statusUpdate("Decryption complete.");

        } catch (Exception e) {
            throw new IOException("Incoming transfer error: " + e.getMessage(), e);
        }
    }

    // ==================================================
    // Streaming I/O with Pause / Resume Support
    // ==================================================

    private long transfer(InputStream in, OutputStream out, long size) throws IOException {
        int amt;
        long total = 0;
        long lastUpdate = 0;
        byte[] buffer = new byte[1024 * 64]; // 64 KB buffer for high throughput

        while (true) {
            // Check pause condition without busy-waiting or discarding data
            synchronized (this) {
                while (pauseFlag) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Transfer interrupted / cancelled");
                    }
                }
            }

            amt = in.read(buffer);
            if (amt == -1) {
                break;
            }

            out.write(buffer, 0, amt);
            out.flush();
            total += amt;

            // Throttle UI progress bar updates to max 20 times per second
            long now = System.currentTimeMillis();
            if (now - lastUpdate > 50 || total == size) {
                lastUpdate = now;
                final double progress = (size > 0) ? (double) total / size : 0.0;
                Platform.runLater(() -> bar.setProgress(Math.min(1.0, progress)));
            }
        }
        return total;
    }

    // ==================================================
    // Control Operations (Pause, Resume, Cancel)
    // ==================================================

    /** Pauses active data transmission */
    public void pauseTransfer() {
        this.pauseFlag = true;
        statusUpdate("[PAUSED]");
    }

    /** Resumes paused data transmission */
    public void resumeTransfer() {
        synchronized (this) {
            this.pauseFlag = false;
            this.notifyAll();
        }
        statusUpdate("[RESUMED]");
    }

    /** Cancels the ongoing transfer and disconnects sockets */
    public void cancelTransfer() {
        this.cancelled = true;
        Controller.transferList.getPanes().remove(this.infoCard);
        this.interrupt();
        disconnect();
    }

    // ==================================================
    // Helper Methods & Socket Cleanup
    // ==================================================

    private void statusUpdate(String s) {
        Platform.runLater(() -> this.status.setText("Status: " + s));
    }

    /**
     * Safely closes client and server sockets.
     */
    private void disconnect() {
        try {
            if (this.client != null && !this.client.isClosed()) {
                this.client.close();
            }
        } catch (IOException ignored) {}
        try {
            if (this.host != null && !this.host.isClosed()) {
                this.host.close();
            }
        } catch (IOException ignored) {}
    }
}

