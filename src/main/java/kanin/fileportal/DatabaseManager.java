package kanin.fileportal;

import java.sql.*;

/**
 * DatabaseManager handles all SQLite database operations for SwiftShare.
 * It manages creating tables, inserting transfer logs, and fetching history.
 */
public class DatabaseManager {

    // ---------- Database Configuration ----------
    private static final String DB_URL = "jdbc:sqlite:swiftshare.db"; // SQLite database file

    // ---------- Database Initialization ----------

    /** Creates the database and transfer_history table if not already present */
    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // Create table to store transfer logs (file name, sender, receiver, size, date, and status)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS transfer_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    file_name TEXT NOT NULL,
                    sender TEXT,
                    receiver TEXT,
                    size_bytes INTEGER,
                    transfer_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                    status TEXT
                )
            """);

            System.out.println("✅ Database initialized successfully (swiftshare.db)");

        } catch (SQLException e) {
            System.err.println("❌ Database initialization error: " + e.getMessage());
        }
    }

    // ---------- Insert Operation ----------

    /** Inserts a new record into the transfer_history table */
    public static void insertTransfer(String fileName, String sender, String receiver, long size, String status) {
        String sql = """
            INSERT INTO transfer_history (file_name, sender, receiver, size_bytes, status)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Bind parameters to prevent SQL injection
            pstmt.setString(1, fileName);
            pstmt.setString(2, sender);
            pstmt.setString(3, receiver);
            pstmt.setLong(4, size);
            pstmt.setString(5, status);
            pstmt.executeUpdate();

            System.out.println("📦 Logged transfer: " + fileName + " (" + status + ")");

        } catch (SQLException e) {
            System.err.println("⚠️ Failed to log transfer: " + e.getMessage());
        }
    }

    // ---------- Fetch Operation ----------

    /** Retrieves all previous transfer records from the database, ordered by date (latest first) */
    public static ResultSet fetchTransfers() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        Statement stmt = conn.createStatement();
        return stmt.executeQuery("SELECT * FROM transfer_history ORDER BY transfer_date DESC");
    }
}
