# SwiftShare

**SwiftShare** is an open-source JavaFX desktop application designed for secure, direct peer-to-peer (P2P) file transfer across local area networks (LAN). It provides a private alternative to cloud storage and USB drives by streaming files directly between peer machines over TCP sockets with authenticated AES-GCM payload encryption and embedded SQLite transfer history auditing.

---

## 1. Project Overview

Traditional file exchange methods often introduce significant trade-offs:
- **Cloud-Based Sharing (Google Drive, WeTransfer, Dropbox):** Requires active internet connectivity, consumes external upload/download bandwidth, enforces file size quotas, and exposes private data to third-party cloud infrastructure.
- **Physical Media (Flash Drives, External SSDs):** Requires physical proximity, manual plugging/unplugging, and creates malware transmission risks.

SwiftShare eliminates intermediaries by establishing a direct TCP socket connection between two endpoints on a local network:
- **Direct LAN Streaming:** Data travels directly across the local subnet at native network speeds without touching an external server.
- **Constant Memory Footprint:** Streams files in 64 KB chunks, allowing transfer of small documents or multi-gigabyte files with predictable, low RAM utilization (~60-120 MB).
- **Authenticated Payload Encryption:** Every transfer encrypts the payload using AES in Galois/Counter Mode (AES-GCM) with an ephemeral 128-bit key and unique 12-byte initialization vector (IV).
- **Staged File Reception & Integrity Verification:** Decrypted incoming bytes are written to a temporary staging file; the destination file is only promoted upon successful authentication tag verification.
- **Local SQLite Audit Trail:** Detailed records of every inbound and outbound transfer are logged automatically to an embedded SQLite database.

---

## 2. Features

- **Peer-to-Peer TCP Transfer:** Connects sender and receiver directly via Java sockets (`ServerSocket` / `Socket`).
- **Flexible Host / Client Roles:** Either peer can act as the socket server (Host) or socket client (Client), enabling flexible firewall traversal on local networks.
- **JavaFX Desktop Interface:** Clean, responsive dark-themed GUI built with JavaFX FXML and CSS.
- **AES-GCM Payload Encryption:** End-to-end payload confidentiality and authenticity verification (`AES/GCM/NoPadding`, 128-bit key, 12-byte random nonce, 128-bit authentication tag).
- **Staged Inbound Decryption & Cleanup:** Incoming data writes to temporary hidden staging files (`.filename.timestamp.part`). Corrupted or tampered files fail authentication and are deleted immediately before touching destination storage.
- **Strict Destination Collision Safety:** Existing destination files are never overwritten. In case of name collisions, files are automatically resolved with a deterministic `copy_` prefix and atomic move guarantees.
- **Path Traversal Protection:** Received filenames are stripped of path separators and directory traversal sequences (`..`), ensuring files only land in the user-selected destination directory.
- **Real-Time Transfer Progress:** Live progress bars and status updates driven by asynchronous worker threads and the JavaFX application thread.
- **Transfer Pause, Resume & Cancellation:** Active transfers can be paused, resumed, or cleanly cancelled with automatic cleanup of partial files.
- **Embedded Transfer History:** Persistent SQLite database records file name, sender IP, receiver IP, size in bytes, timestamp, and final status (Success / Failed / Cancelled).
- **Isolated Automated Test Suite:** 11 JUnit 5 integration tests covering encryption, tampering detection, collision safety, persistence, streaming, and concurrency, running against an isolated test database.
- **One-Click Windows Launcher:** Self-contained `run-swiftshare.bat` script that verifies JDK 17, compiles via Maven on demand, and launches the application with zero manual setup.

---

## 3. Technology Stack

| Layer | Component | Specification / Version |
|---|---|---|
| **Runtime Environment** | Java Virtual Machine | OpenJDK 17 (LTS) |
| **User Interface** | JavaFX | OpenJFX 11.0.2 / FXML / CSS |
| **Networking** | Java Standard Library | TCP Sockets (`java.net.ServerSocket`, `java.net.Socket`) |
| **Cryptography** | Java Cryptography Extension (JCE) | AES-GCM (`AES/GCM/NoPadding`, 128-bit key, 12-byte IV, 128-bit auth tag) |
| **Database & Persistence** | SQLite | SQLite JDBC 3.45.3.0 (`org.xerial:sqlite-jdbc`) / WAL Journal Mode |
| **Build & Packaging** | Apache Maven | Maven Shade Plugin 3.4.1 (Monolithic executable uber-JAR) |
| **Testing Framework** | JUnit 5 Jupiter | JUnit Jupiter 5.8.1 / Maven Surefire Plugin 3.1.2 |

---

## 4. Project Structure

```text
SwiftShare/
├── run-swiftshare.bat                  # One-click Windows launcher (prereq checks + launch)
├── pom.xml                             # Maven build configuration and Shade packaging
├── README.md                           # Professional project documentation
├── .gitignore                          # Git rules excluding binaries, target, and personal DB
├── database/                           # Runtime SQLite database storage
│   └── swiftshare.db                   # Local application database (auto-created on launch)
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── kanin/fileportal/
│   │   │       ├── Launcher.java       # Standard JVM entry point (bypasses module checks)
│   │   │       ├── Main.java           # JavaFX Application lifecycle and DB bootstrapper
│   │   │       ├── Controller.java     # JavaFX UI event controller and validation
│   │   │       ├── FileTransferThread.java # Multi-threaded P2P streaming transfer worker
│   │   │       ├── DatabaseManager.java    # SQLite DAO with prepared statements
│   │   │       ├── JDBCConnectivity.java  # Connection pooling and WAL initialization
│   │   │       ├── HistoryWindow.java      # Transfer history viewer with auto-refresh
│   │   │       ├── TransferRecord.java     # Observable JavaFX data model
│   │   │       └── security/
│   │   │           └── EncryptionUtil.java # AES-GCM cipher initialization & key utilities
│   │   └── resources/
│   │       ├── fp.png                  # Project graphical asset
│   │       ├── fp2.png                 # Project graphical asset
│   │       ├── main.fxml               # Declarative JavaFX UI layout
│   │       └── main.css                # Dark theme stylesheet
│   └── test/
│       └── java/
│           └── kanin/fileportal/
│               └── ComprehensiveQATest.java # 11 JUnit 5 automated integration tests
└── target/                             # Generated Maven build artifacts (ignored by git)
    ├── SwiftShare.jar                  # Packaged executable shaded JAR (~24 MB)
    └── test-data/                      # Isolated SQLite test directory for QA
```

---

## 5. Prerequisites

Before running SwiftShare on Windows (or other operating systems), verify the following prerequisites:

1. **Java Development Kit (JDK):**
   - **Version:** Java 17 LTS or higher.
   - Verify by running in PowerShell or Command Prompt:
     ```powershell
     java -version
     ```
   - If missing, download and install Eclipse Temurin JDK 17 from [Adoptium](https://adoptium.net/temurin/releases/?version=17). Ensure the installer option **"Add to PATH"** and **"Set JAVA_HOME"** are enabled.

2. **Apache Maven:**
   - **Version:** Maven 3.8+ or higher (required for initial compilation from source).
   - Verify by running:
     ```powershell
     mvn -v
     ```
   - If Maven is not installed globally, you may run builds via your IDE (IntelliJ IDEA, Eclipse, VS Code) or install Maven from [maven.apache.org](https://maven.apache.org/download.cgi).

3. **Local Network & Firewall:**
   - Both devices must reside on the same local subnet or Wi-Fi network.
   - Windows Defender Firewall may prompt to allow network access for Java on first launch. Allow access on Private networks.
   - Default TCP port: `54000` (configurable directly in the UI).

---

## 6. Quick Start — One-Click Windows Launch

For Windows users, SwiftShare provides an automated, one-click launcher that inspects prerequisites, compiles the project if needed, and launches the application without manual terminal interaction.

### Step-by-Step:

1. **Clone the repository:**
   ```powershell
   git clone https://github.com/SanyamRajSingh/SwiftShare.git
   ```
2. **Open the project folder:**
   ```powershell
   cd SwiftShare
   ```
3. **Double-click `run-swiftshare.bat`:**
   - The launcher verifies your Java 17+ installation.
   - If `target\SwiftShare.jar` does not exist, it runs `mvn -B -DskipTests package` automatically.
   - Once packaged, it launches the desktop interface.
   - If an error occurs (such as missing Java), the console stays open with clear troubleshooting instructions.

*(To force a rebuild through the launcher, run `run-swiftshare.bat --build` from the terminal).*

---

## 7. Manual Build and Run

You can build and test SwiftShare directly using standard Maven commands:

### Compile Source Code:
```powershell
mvn clean compile
```

### Run Automated Integration Tests:
```powershell
mvn test
```

### Package Executable JAR:
```powershell
mvn clean package -DskipTests
```
The Maven Shade plugin generates a self-contained executable JAR at:
```text
target/SwiftShare.jar (~24 MB)
```

### Launch the Application:
```powershell
java -jar "target\SwiftShare.jar"
```

---

## 8. How to Use SwiftShare

SwiftShare supports flexible peer roles: either device can act as the **Host** (socket server listening on a port) while the other connects as the **Client**.

### Scenario A: Sending a File
1. **Choose File:** Click **Select** to choose the file you wish to send.
2. **If You Are the Host (Listener):**
   - Check the **Host** checkbox.
   - Enter your listening **Port** (default: `54000`).
   - Click **Send**. The status will display `Waiting for peer...`.
3. **If You Are the Client (Connector):**
   - Leave the **Host** checkbox unchecked.
   - Enter the receiving peer's local **IP Address** (e.g. `192.168.1.15`).
   - Enter the receiving peer's chosen **Port** (e.g. `54000`).
   - Click **Send** once the receiver is listening.

### Scenario B: Receiving a File
1. **Choose Destination Folder:** Click **Save** to select the directory where incoming files should be stored.
2. **If You Are the Host (Listener):**
   - Check the **Host** checkbox.
   - Enter your listening **Port** (default: `54000`).
   - Click **Receive**. SwiftShare begins listening on the chosen port.
3. **If You Are the Client (Connector):**
   - Leave the **Host** checkbox unchecked.
   - Enter the sending peer's **IP Address** and **Port**.
   - Click **Receive** to establish the connection.

### Transfer Controls:
- **Pause / Resume:** Right-click the transfer card in the right-hand panel and click **Pause** (toggles to **Resume**). The streaming thread waits safely without packet loss.
- **Cancel Transfer:** Right-click the transfer card and select **Cancel**. The socket connection closes immediately, the worker thread terminates, and any partially transferred staging file is deleted from disk.
- **Transfer History:** Click **View History** to inspect recorded transfer logs. The history table updates automatically every 5 seconds or via the manual **Refresh Now** button.

---

## 9. Architecture and Key Classes

SwiftShare uses a modular desktop architecture separating the JavaFX UI, asynchronous network streaming workers, cryptographic utilities, and SQLite persistence:

| Class | Package | Responsibility |
|---|---|---|
| `Launcher` | `kanin.fileportal` | Standard JVM bootstrap entry point (`public static void main`). Delegates to `Main.main()`, bypassing JavaFX module system restrictions when running from an executable shaded uber-JAR. |
| `Main` | `kanin.fileportal` | Extends `javafx.application.Application`. Coordinates stage initialization, loads `main.fxml`, sets application icons, and ensures SQLite schema initialization via `DatabaseManager`. |
| `Controller` | `kanin.fileportal` | Manages JavaFX UI components, input validation, folder/file choosers, transfer card UI lifecycle, and dispatches worker threads. |
| `FileTransferThread` | `kanin.fileportal` | Background worker extending `Thread`. Manages TCP sockets, executes 64 KB streaming I/O, performs AES-GCM encryption/decryption, monitors transfer progress, handles pause/resume, enforces destination collision safety, and logs results to SQLite. |
| `EncryptionUtil` | `kanin.fileportal.security` | Cryptographic helper providing 128-bit AES key generation (`KeyGenerator`), 12-byte random IV generation (`SecureRandom`), and configured `Cipher` instances (`AES/GCM/NoPadding`). |
| `DatabaseManager` | `kanin.fileportal` | Data Access Object (DAO) executing parameterized queries (`PreparedStatement`) to initialize schemas, insert transfer audit records, and retrieve transfer history. |
| `JDBCConnectivity` | `kanin.fileportal` | Manages SQLite connection pooling, ensures directory creation, and configures WAL journal mode (`PRAGMA journal_mode=WAL`) for concurrent access. |
| `HistoryWindow` | `kanin.fileportal` | Dedicated secondary JavaFX window displaying logged transfer history with column sorting, auto-refresh polling, and manual reload. |
| `TransferRecord` | `kanin.fileportal` | Observable JavaFX data model binding SQLite database columns (`id`, `fileName`, `sender`, `receiver`, `sizeBytes`, `transferDate`, `status`) to UI `TableView` rows. |
| `ComprehensiveQATest` | `kanin.fileportal` | JUnit 5 automated test suite containing 11 end-to-end integration tests verifying encryption, tampering rejection, destination collision safety, database isolation, streaming, and concurrency. |

---

## 10. Automated Testing

SwiftShare includes a comprehensive suite of 11 integration tests executed via Maven Surefire and JUnit 5:

```powershell
mvn test
```

### Verified Test Suite (11/11 Passing):
1. **`test01_DatabaseInitializationAndSchema`**: Verifies automated SQLite table creation and column layout.
2. **`test02_DatabaseInsertAndQuery`**: Verifies parameterized insert and query retrieval of transfer records.
3. **`test03_TransferStatusTransitions`**: Verifies record logging across success, failed, and cancelled states.
4. **`test04_BasicFileTransferEndToEnd`**: Verifies end-to-end transfer of sample files over loopback TCP sockets.
5. **`test05_StreamingFileTransfer_64KBChunking`**: Validates chunked streaming on multi-megabyte payloads without high heap allocation.
6. **`test06_PauseAndResumeTransfer`**: Verifies transfer suspension and resumption without stream corruption or data loss.
7. **`test07_CancellationAndCleanup`**: Verifies that cancelled transfers interrupt threads and purge partial `.part` staging files.
8. **`test08_PathTraversalSecuritySanitization`**: Verifies that malicious filenames (e.g. `../../evil.exe`) are sanitized to safe local basenames.
9. **`test09_ConcurrentTransfers`**: Verifies multiple simultaneous transfers across distinct socket ports without resource deadlocks.
10. **`test10_AESGCMTamperedDataRejection`**: Validates that tampered ciphertext bytes trigger `AEADBadTagException`, abort the transfer, and delete staging data.
11. **`test11_DestinationCollisionNoOverwriteGuarantee`**: Validates that sending a file with an identical name to an existing destination automatically creates collision-safe `copy_<filename>` files without overwriting existing data.

> **Database Isolation Guarantee:** Automated tests run strictly against an isolated temporary database located at `target/test-data/test-swiftshare.db`. Running `mvn test` will **never** alter or append test rows to the production database (`database/swiftshare.db`).

---

## 11. Data and Database Behaviour

SwiftShare embeds a serverless SQLite 3 database located at:
```text
database/swiftshare.db
```

### Database Schema:
```sql
CREATE TABLE IF NOT EXISTS transfer_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    file_name TEXT NOT NULL,
    sender TEXT,
    receiver TEXT,
    size_bytes INTEGER,
    transfer_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    status TEXT
);
```

### Persistence Characteristics:
- **Automatic Initialization:** On application startup, `Main.java` calls `DatabaseManager.initializeDatabase()`, creating the directory and schema if they do not exist.
- **Privacy & Git Exclusion:** The `database/` directory and `.db` files are strictly excluded via `.gitignore`. A fresh clone starts with a clean local database and will never inherit previous developers' transfer histories.
- **Concurrency & WAL Mode:** Connections utilize Write-Ahead Logging (`PRAGMA journal_mode=WAL`), allowing concurrent GUI history reads while background transfer threads write log records.

---

## 12. Security Architecture & Honest Limitations

SwiftShare implements core cryptographic principles for educational peer-to-peer file transfer. To ensure technical transparency, its strengths and known boundaries are documented below:

### Implemented Security Strengths:
1. **Authenticated AES-GCM Encryption:** Payloads are encrypted using AES in Galois/Counter Mode (`AES/GCM/NoPadding`) with a 128-bit key, 12-byte initialization vector, and 128-bit authentication tag.
2. **Explicit Authentication Tag Verification:** Inbound file chunks are decrypted using Java's `Cipher.update()`, followed by an explicit `Cipher.doFinal()` check. If any byte in the ciphertext, IV, or tag is tampered with or truncated, an `AEADBadTagException` is thrown.
3. **Staging File Isolation:** Incoming bytes are written to a hidden staging file (`.filename.timestamp.part`). The file is promoted to its final destination only after tag verification succeeds. Failed transfers purge the staging file immediately.
4. **Collision and Traversal Defenses:** File names are sanitized to prevent directory traversal attacks, and existing destination files are protected with deterministic collision renaming.

### Documented Limitations & Honest Boundary:
1. **Plaintext Symmetric Key Transmission:** The 128-bit AES symmetric key is sent across the TCP socket directly prior to the IV and ciphertext.
2. **No Eavesdropping / MITM Protection:** Because authenticated key agreement (such as TLS or Diffie-Hellman) is not implemented, an active attacker or network eavesdropper capturing packets on the local subnet could intercept the AES key and decrypt the payload.
3. **Environment Recommendation:** SwiftShare is an academic and educational desktop project intended for trusted local networks (home/office/lab LAN). It should **not** be deployed on untrusted public networks without external transport security (such as an encrypted VPN tunnel or SSH port forwarding).
4. **Future Work:** Production-grade hardening would integrate Transport Layer Security (TLS 1.3) with mutual certificate authentication (mTLS) or an ephemeral Diffie-Hellman key exchange with out-of-band fingerprint verification.

---

## 13. Troubleshooting

| Symptom | Probable Cause | Recommended Resolution |
|---|---|---|
| Launcher reports `Java is not installed or not available on your PATH` | No JDK installed or PATH variable not set | Download and install OpenJDK 17 from [Adoptium](https://adoptium.net/temurin/releases/?version=17). Check the option to add Java to system `PATH`. |
| Launcher reports `Incompatible Java version detected` | Older Java (e.g., Java 8 or 11) is active on PATH | Install JDK 17+ and update your `JAVA_HOME` and system `PATH` to point to the JDK 17 bin directory. |
| Launcher reports `Maven was not found on PATH` on fresh clone | Apache Maven is missing and no pre-built JAR exists | Install Apache Maven 3.8+ and add its `bin/` directory to `PATH`, or build the project once using your IDE. |
| Connection refused (`ConnectException`) | Receiver is not listening or IP address is wrong | Ensure the receiving peer clicks **Receive** (or **Send** in Host mode) first before the client connects. Verify the receiver's local IP address using `ipconfig`. |
| Connection times out | Windows Firewall or router blocking port | In Windows Firewall settings, allow incoming TCP connections for Java on port `54000`, or test with an alternate port above 1024. |
| File transfer fails with `AEADBadTagException` | Ciphertext was altered, corrupted, or truncated | Inspect network stability. The receiving peer's staging file is purged to protect storage integrity. |

---

## 14. Development and Contribution

Contributions, bug reports, and suggestions are welcome!

### Development Workflow:
1. Fork or clone the repository:
   ```powershell
   git clone https://github.com/SanyamRajSingh/SwiftShare.git
   ```
2. Open the project in IntelliJ IDEA, Eclipse, or VS Code (with Extension Pack for Java).
3. Ensure your IDE is configured with JDK 17.
4. Always execute the integration tests before committing changes:
   ```powershell
   mvn clean test
   ```
5. Commit clean changes with descriptive Git commit messages.
6. Submit a Pull Request targeting the `main` branch.

---

## 15. License

This repository currently has no explicit open-source license. All rights are reserved by the author. Reuse, modification, or redistribution permissions should not be assumed without prior written consent from the author.

---

## 16. Acknowledgements

- **JavaFX Project:** For modern cross-platform desktop UI components ([openjfx.io](https://openjfx.io/)).
- **Xerial SQLite JDBC:** For lightweight, embedded SQLite connectivity ([github.com/xerial/sqlite-jdbc](https://github.com/xerial/sqlite-jdbc)).
- **JUnit 5 Jupiter:** For standard unit and integration testing support ([junit.org/junit5/](https://junit.org/junit5/)).
- **Apache Maven:** For dependency management and shaded packaging plugins.