package kanin.fileportal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseManager
 *
 * Handles SQLite database operations for SwiftShare.
 * Manages table initialization, inserting transfer history records, and fetching transfer logs.
 *
 * Key Architecture Highlights:
 * - Delegates JDBC connection lifecycle directly to JDBCConnectivity.
 * - Enforces try-with-resources on every Connection, Statement, and ResultSet to eliminate database locks.
 * - Uses parameterized PreparedStatement for all inserts to guarantee SQL injection safety.
 */
public class DatabaseManager {

    // ==================================================
    // SQL Statements & Schema Definitions
    // ==================================================
    private static final String CREATE_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS transfer_history (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            file_name TEXT NOT NULL,
            sender TEXT,
            receiver TEXT,
            size_bytes INTEGER,
            transfer_date DATETIME DEFAULT CURRENT_TIMESTAMP,
            status TEXT
        )
    """;

    private static final String INSERT_TRANSFER_SQL = """
        INSERT INTO transfer_history (file_name, sender, receiver, size_bytes, status)
        VALUES (?, ?, ?, ?, ?)
    """;

    private static final String FETCH_TRANSFERS_SQL = """
        SELECT file_name, sender, receiver, size_bytes, transfer_date, status
        FROM transfer_history
        ORDER BY transfer_date DESC
    """;

    // ==================================================
    // Database Initialization
    // ==================================================

    /**
     * Initializes the SQLite database schema if not already present.
     * Called during application startup in Main.start().
     */
    public static void initializeDatabase() {
        try (Connection conn = JDBCConnectivity.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(CREATE_TABLE_SQL);
            System.out.println("✅ Database schema initialized successfully (" + JDBCConnectivity.getDatabasePath() + ")");

        } catch (SQLException e) {
            System.err.println("❌ Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================================================
    // Insert Transfer Record
    // ==================================================

    /**
     * Inserts a new transfer record into the transfer_history table using a PreparedStatement.
     *
     * @param fileName the name of the transferred file
     * @param sender   the sender peer description (e.g., "Host", "Client", or IP)
     * @param receiver the receiver peer description (e.g., "Host", "Client", or IP)
     * @param size     the size of the transferred file in bytes
     * @param status   the outcome status (e.g., "Success", "Failed: ...", "Cancelled")
     * @return true if the record was inserted successfully, false otherwise
     */
    public static boolean insertTransfer(String fileName, String sender, String receiver, long size, String status) {
        try (Connection conn = JDBCConnectivity.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_TRANSFER_SQL)) {

            pstmt.setString(1, fileName != null ? fileName : "Unknown");
            pstmt.setString(2, sender != null ? sender : "Unknown");
            pstmt.setString(3, receiver != null ? receiver : "Unknown");
            pstmt.setLong(4, size);
            pstmt.setString(5, status != null ? status : "Unknown");

            int rowsAffected = pstmt.executeUpdate();
            System.out.println("📦 Logged transfer: " + fileName + " (" + status + ")");
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("⚠️ Failed to log transfer: " + e.getMessage());
            return false;
        }
    }

    // ==================================================
    // Fetch Transfer Records
    // ==================================================

    /**
     * Retrieves all transfer records from the database, ordered by latest transfer date first.
     *
     * @return a List of TransferRecord objects populated from the SQLite database
     */
    public static List<TransferRecord> fetchTransfers() {
        List<TransferRecord> records = new ArrayList<>();

        try (Connection conn = JDBCConnectivity.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(FETCH_TRANSFERS_SQL)) {

            while (rs.next()) {
                records.add(new TransferRecord(
                        rs.getString("file_name"),
                        rs.getString("sender"),
                        rs.getString("receiver"),
                        String.valueOf(rs.getLong("size_bytes")),
                        rs.getString("transfer_date"),
                        rs.getString("status")
                ));
            }

        } catch (SQLException e) {
            System.err.println("⚠️ Failed to fetch transfer history: " + e.getMessage());
        }

        return records;
    }
}

