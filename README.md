# SwiftShare

**SwiftShare** is a full-stack, distributed peer-to-peer file transfer and metadata management ecosystem. It combines an authentic **JavaFX desktop application** for high-speed, direct LAN file streaming over TCP sockets with authenticated AES-GCM encryption, a modern **React + Vite web console** for telemetry and transfer tracking, a **Spring Boot core service** providing REST APIs and Spring Data JPA persistence, and an independent **Spring Boot audit microservice** demonstrating decoupled service-to-service communication.

---

## 1. Project Overview & Architecture

SwiftShare addresses local file sharing through a hybrid architecture clearly distinguishing between payload transfer and metadata management:

1. **Desktop Peer-to-Peer File Streaming (LAN):**
   - Direct peer-to-peer byte transmission over raw TCP sockets (`ServerSocket` and `Socket`) without routing multi-gigabyte files through external web servers.
   - 64 KB chunked streaming I/O providing constant, low RAM usage (~60–120 MB) regardless of file size.
   - End-to-end payload confidentiality and authenticity via authenticated **AES-GCM encryption** (`AES/GCM/NoPadding`).
   - Staged reception into temporary hidden files (`.filename.timestamp.part`) with atomic promotion only after successful authentication tag verification.
   - Local persistence in an embedded serverless **SQLite** database (`database/swiftshare.db`) via direct JDBC.

2. **Web Management Console (Browser):**
   - Single-Page Application (SPA) built with **React 18** and **Vite**.
   - Provides browser-based monitoring of transfer records, metrics calculation, and audit telemetry querying.
   - Connects to the backend REST APIs over HTTP.

3. **Backend Core Service (Spring Boot — Port 8080):**
   - Central REST API controller managing transfer entities, validation, and metadata CRUD operations.
   - Persistent storage using **Spring Data JPA** with an embedded **H2** database engine.
   - Inter-service client communicating synchronously with the independent audit service via Spring's `RestClient`.

4. **Independent Audit Microservice (Spring Boot — Port 8081):**
   - Decoupled microservice with its own dedicated H2 database (Database-per-Service pattern).
   - Records immutable audit events generated across the system and exposes audit query endpoints.

```text
┌────────────────────────────────────────────────────────────────────────┐
│                        SWIFTSHARE SYSTEM ARCHITECTURE                  │
└────────────────────────────────────────────────────────────────────────┘

  [ Desktop Peer A (JavaFX) ]  <======= Direct TCP Socket =======>  [ Desktop Peer B (JavaFX) ]
  • Port: 54000 (Configurable)          (64 KB Chunk Streaming)       • Port: 54000 (Configurable)
  • AES-GCM Encrypted Payload           (Staged Atomic Verification)  • AES-GCM Decryption & Tag Check
  • SQLite Persistence (JDBC)                                         • SQLite Persistence (JDBC)

                                         ▲
                                         │ Metadata sync / tracking
                                         ▼
  [ React 18 + Vite Web Console ]
  • Port: 5173
  • Real-time Transfer Dashboard & Audit Trail Viewer
         │
         │ HTTP REST (JSON)
         ▼
  [ SwiftShare Core Service ] ──── HTTP POST /events ────► [ SwiftShare Audit Microservice ]
  • Spring Boot 3.3.4 (Port 8080)                            • Spring Boot 3.3.4 (Port 8081)
  • Spring Data JPA & Hibernate                              • Independent H2 Database
  • Persistent H2 Store (core_swiftshare_db)                 • Dedicated Store (audit_swiftshare_db)
  • Service-to-Service RestClient                            • Immutable Audit Event Log
```

---

## 2. Key Features

- **Direct P2P LAN Transfer:** Transfer files directly between computers on the same network at full line speed.
- **Flexible Peer Roles:** Either peer can act as the **Host** (socket server listening on a port) or **Client** (connecting to a host IP and port).
- **Authenticated Encryption:** AES-GCM with 128-bit key, 12-byte initialization vector, and 128-bit authentication tag. Any altered byte aborts the transfer before disk commit.
- **Strict Destination Protection:** Destination files are never overwritten. Deterministic `copy_` naming guarantees zero data destruction.
- **Path Traversal Sanitization:** Filenames containing `../` or path separators are automatically sanitized to safe local basenames.
- **Transfer Controls:** Pause, resume, and cleanly cancel active transfers with automatic staging file cleanup.
- **Dual Persistence:**
  - Embedded **SQLite 3** (`database/swiftshare.db`) via JDBC for standalone desktop operation.
  - **Spring Data JPA** with H2 databases for web console and enterprise audit management.
- **Autonomous Microservice:** Independent audit microservice demonstrating decoupled database persistence and live HTTP service-to-service communication.
- **Automated Verification:** Comprehensive automated test suites across JavaFX desktop (11 integration tests), Core service (5 tests), Audit service (2 tests), and frontend production builds.

---

## 3. Technology Stack

| Layer | Component | Specification / Version |
|---|---|---|
| **Runtime Environment** | Java Virtual Machine | OpenJDK 17 (LTS) |
| **Frontend Web Console** | React / Vite | React 18.3.1 / Vite 5.4.8 (ES Modules) |
| **Desktop UI** | JavaFX | OpenJFX 11.0.2 / FXML / CSS |
| **Core Backend Service** | Spring Boot | Spring Boot 3.3.4 / Spring MVC / Validation |
| **Audit Microservice** | Spring Boot | Spring Boot 3.3.4 / Microservice Architecture |
| **ORM & Data Access** | Spring Data JPA / Hibernate | Hibernate ORM 6.5.3 / HikariCP Connection Pool |
| **Databases** | SQLite & H2 | SQLite JDBC 3.45.3.0 & Persistent H2 Engine |
| **Build & Packaging** | Apache Maven & npm | Maven 3.8+ / Maven Shade 3.4.1 / Spring Boot Maven Plugin |
| **Testing** | JUnit 5 & MockMvc | JUnit Jupiter 5.8.1 / Surefire / Spring Boot Test |

---

## 4. Project Structure

```text
SwiftShare/
├── run-swiftshare.bat                  # One-click Windows launcher for desktop app
├── pom.xml                             # Desktop Maven configuration and Shade packaging
├── README.md                           # System documentation and setup guide
├── .gitignore                          # Excludes targets, node_modules, and local databases
├── database/                           # Runtime SQLite database storage (ignored by git)
│   └── swiftshare.db                   # Desktop SQLite database (auto-created on launch)
├── src/                                # Desktop JavaFX Application Source
│   ├── main/
│   │   ├── java/kanin/fileportal/
│   │   │   ├── Launcher.java           # JVM bootstrap entry point
│   │   │   ├── Main.java               # JavaFX Application lifecycle and DB bootstrapper
│   │   │   ├── Controller.java         # JavaFX UI event controller
│   │   │   ├── FileTransferThread.java # Streaming socket transfer worker (AES-GCM)
│   │   │   ├── DatabaseManager.java    # SQLite DAO with prepared statements
│   │   │   ├── JDBCConnectivity.java  # Connection pooling and WAL initialization
│   │   │   ├── HistoryWindow.java      # Transfer history viewer
│   │   │   ├── TransferRecord.java     # Observable JavaFX data model
│   │   │   └── security/
│   │   │       └── EncryptionUtil.java # AES-GCM cipher and key utilities
│   │   └── resources/
│   │       ├── main.fxml               # Declarative JavaFX UI layout
│   │       └── main.css                # Dark theme stylesheet
│   └── test/java/kanin/fileportal/
│       └── ComprehensiveQATest.java    # 11 JUnit 5 desktop integration tests
├── backend/                            # Spring Boot Microservices
│   ├── core-service/                   # Port 8080: REST API & JPA Persistence
│   │   ├── pom.xml
│   │   ├── src/main/java/kanin/swiftshare/core/
│   │   │   ├── SwiftShareCoreApplication.java
│   │   │   ├── controller/TransferController.java
│   │   │   ├── model/TransferEntity.java, PeerNode.java
│   │   │   ├── repository/TransferRepository.java, PeerNodeRepository.java
│   │   │   └── service/TransferService.java
│   │   ├── src/main/resources/
│   │   │   ├── application.properties
│   │   │   └── sql/unit3_syllabus_demonstration.sql
│   │   └── src/test/java/kanin/swiftshare/core/
│   │       ├── TransferControllerTest.java
│   │       └── SqlSyllabusDemonstrationTest.java
│   ├── audit-service/                  # Port 8081: Independent Audit Microservice
│   │   ├── pom.xml
│   │   ├── src/main/java/kanin/swiftshare/audit/
│   │   │   ├── SwiftShareAuditApplication.java
│   │   │   ├── controller/AuditController.java
│   │   │   ├── model/AuditLogEntity.java
│   │   │   └── repository/AuditLogRepository.java
│   │   └── src/test/java/kanin/swiftshare/audit/
│   │       └── AuditControllerTest.java
│   └── verify_e2e_flow.js              # Live service-to-service E2E verification script
└── frontend/                           # Port 5173: React + Vite Web Management Console
    ├── package.json
    ├── vite.config.js
    ├── index.html
    └── src/
        ├── main.jsx, App.jsx
        ├── components/
        │   ├── Header.jsx, Footer.jsx
        │   ├── TransferConsole.jsx
        │   ├── TransferTable.jsx
        │   ├── AuditTrailView.jsx
        │   ├── DashboardMetrics.jsx
        │   └── SyllabusShowcase.jsx
        └── context/AppContext.jsx
```

---

## 5. Prerequisites

Ensure the following tools are installed:

1. **Java Development Kit (JDK):**
   - OpenJDK 17 LTS or higher.
   - Check with: `java -version`
2. **Apache Maven:**
   - Version 3.8 or higher.
   - Check with: `mvn -v`
3. **Node.js & npm (for Web Console):**
   - Node.js 18+ and npm 9+.
   - Check with: `node -v` and `npm -v`

---

## 6. How to Run SwiftShare

To run the complete ecosystem, start the services across separate terminals:

### Terminal 1: JavaFX Desktop Application (P2P File Transfer)
```powershell
# Method A: Using the Windows one-click launcher
.\run-swiftshare.bat

# Method B: Using Maven and the packaged executable JAR
mvn clean package -DskipTests
java -jar "target\SwiftShare.jar"
```

### Terminal 2: Audit Microservice (Port 8081)
```powershell
cd backend/audit-service
mvn spring-boot:run
# Or launch the packaged JAR:
# java -jar target/audit-service-1.0.0.jar
```

### Terminal 3: Core Backend Service (Port 8080)
```powershell
cd backend/core-service
mvn spring-boot:run
# Or launch the packaged JAR:
# java -jar target/core-service-1.0.0.jar
```

### Terminal 4: React Web Management Console (Port 5173)
```powershell
cd frontend
npm install
npm run dev
```
Open your browser at `http://localhost:5173` to view the web console.

---

## 7. Configuration Properties & Default Ports

| Component | Default Port | Storage Location / Target | Configuration Key |
|---|---|---|---|
| **Desktop Socket** | `54000` | Local network socket | User-configurable in UI |
| **Desktop SQLite** | N/A | `database/swiftshare.db` | Handled via `JDBCConnectivity.java` |
| **Core Service** | `8080` | `target/data/core_swiftshare_db` | `server.port=8080` |
| **Audit Service** | `8081` | `target/data/audit_swiftshare_db` | `server.port=8081` |
| **Core → Audit URL** | `8081` | `http://localhost:8081/api/audit` | `audit.service.url` |
| **Web Console** | `5173` | Proxies to `:8080` | `http://localhost:5173` |

---

## 8. Automated Testing & Syllabus Verification

### Run Desktop Integration Tests (11 Tests)
```powershell
mvn test
```
Tests AES-GCM encryption, tampering rejection, streaming 64 KB chunks, destination collision protection, path traversal sanitization, and isolated SQLite database operations.

### Run Core Service Tests (5 Tests, including SQL Demonstration)
```powershell
cd backend/core-service
mvn test
```
Verifies REST controllers, JPA repositories, Bean validation, and executes relational SQL queries (`SELECT`, `JOIN`, `GROUP BY`, `COUNT`) via `SqlSyllabusDemonstrationTest`.

### Run Audit Service Tests (2 Tests)
```powershell
cd backend/audit-service
mvn test
```
Verifies independent persistence, event generation, and audit querying.

### Run Live Service-to-Service Integration Script
With both Core Service (`:8080`) and Audit Service (`:8081`) running:
```powershell
node backend/verify_e2e_flow.js
```
Validates the end-to-end chain: Core Service persists a transfer -> calls Audit Microservice over HTTP -> Audit Microservice persists the event -> Core Service proxies audit telemetry.

### Build Production Frontend Bundle
```powershell
cd frontend
npm run build
```

---

## 9. Security Architecture & Technical Boundaries

To maintain technical transparency, implemented protections and known boundaries are documented below:

### Implemented Security Strengths
1. **Authenticated AES-GCM Encryption:** Payloads are encrypted using AES in Galois/Counter Mode (`AES/GCM/NoPadding`) with a 128-bit key, 12-byte initialization vector, and 128-bit authentication tag.
2. **Explicit Authentication Tag Verification:** File chunks are decrypted using Java's `Cipher.update()` followed by `Cipher.doFinal()`. If ciphertext or metadata is altered, an `AEADBadTagException` aborts the operation.
3. **Staging File Isolation:** Incoming bytes are written to a hidden staging file (`.filename.timestamp.part`). The file is promoted to its destination only after tag verification succeeds. Failed transfers purge staging files immediately.
4. **Collision and Traversal Defenses:** Incoming filenames are sanitized of path separators and `../` sequences. Existing files are never overwritten; collision renaming (`copy_<filename>`) is applied deterministically.

### Known Boundaries
1. **Plaintext Symmetric Key Transmission:** The 128-bit AES symmetric key is sent across the TCP socket directly prior to the IV and ciphertext.
2. **No Eavesdropping / MITM Protection:** Because authenticated key agreement (such as TLS or Diffie-Hellman) is not implemented on the desktop socket, an attacker capturing packets on the local subnet could intercept the AES key and decrypt the payload.
3. **Environment Recommendation:** SwiftShare is an academic and educational project intended for trusted local networks (home/office/lab LAN). It should **not** be deployed on untrusted public networks without external transport encryption (such as an encrypted VPN or SSH tunnel).

---

## 10. Acknowledgements

- **JavaFX Project:** Cross-platform desktop UI components ([openjfx.io](https://openjfx.io/)).
- **Spring Boot:** Enterprise backend framework ([spring.io](https://spring.io/)).
- **React & Vite:** Fast frontend tooling and component architecture ([vitejs.dev](https://vitejs.dev/)).
- **Xerial SQLite JDBC:** Embedded SQLite connectivity ([github.com/xerial/sqlite-jdbc](https://github.com/xerial/sqlite-jdbc)).
- **JUnit 5:** Automated unit and integration testing framework ([junit.org/junit5/](https://junit.org/junit5/)).