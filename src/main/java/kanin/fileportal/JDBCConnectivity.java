package kanin.fileportal;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * JDBCConnectivity
 *
 * Dedicated JDBC connection provider for SwiftShare.
 * Explicitly demonstrates the standard 4-step JDBC lifecycle for viva/evaluation:
 * 1. Loading the JDBC driver (Class.forName)
 * 2. Creating the database connection (DriverManager.getConnection)
 * 3. Configuring SQLite concurrency settings (WAL mode & busy timeout)
 * 4. Safe connection cleanup utility
 */
public class JDBCConnectivity {

    // ==================================================
    // Constants & Configuration
    // ==================================================
    private static final String DEFAULT_DB_DIRECTORY = "database";
    private static final String DEFAULT_DB_FILE_PATH = "database/swiftshare.db";
    private static final String DRIVER_CLASS = "org.sqlite.JDBC";

    private static volatile String dbDirectory = DEFAULT_DB_DIRECTORY;
    private static volatile String dbFilePath = DEFAULT_DB_FILE_PATH;
    private static volatile String dbUrl = "jdbc:sqlite:" + DEFAULT_DB_FILE_PATH;

    /**
     * Sets a custom database file path.
     * Used by automated tests to isolate test records from the production database.
     *
     * @param path custom database file path (e.g. "target/test-data/test-swiftshare.db")
     */
    public static synchronized void setDatabasePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            dbDirectory = DEFAULT_DB_DIRECTORY;
            dbFilePath = DEFAULT_DB_FILE_PATH;
        } else {
            dbFilePath = path.trim();
            File file = new File(dbFilePath);
            File parent = file.getParentFile();
            dbDirectory = (parent != null) ? parent.getPath() : ".";
        }
        dbUrl = "jdbc:sqlite:" + dbFilePath;
    }

    /**
     * Resets the database path to the default production path (database/swiftshare.db).
     */
    public static synchronized void resetDatabasePath() {
        setDatabasePath(DEFAULT_DB_FILE_PATH);
    }

    /**
     * Returns the currently active database file path.
     *
     * @return active SQLite database path
     */
    public static String getDatabasePath() {
        return dbFilePath;
    }

    // ==================================================
    // Connection Management
    // ==================================================

    /**
     * Loads the SQLite JDBC Driver and creates an active database Connection.
     * Also configures WAL (Write-Ahead Logging) and a busy timeout to prevent
     * SQLite database locking during concurrent transfers and UI history polling.
     *
     * @return an open Connection to the active database
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        // Step 1: Explicitly load and register the JDBC Driver
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC Driver not found on classpath: " + e.getMessage(), e);
        }

        // Ensure database directory exists before connecting
        File dbDir = new File(dbDirectory);
        if (!dbDir.exists()) {
            boolean created = dbDir.mkdirs();
            if (created) {
                System.out.println("📁 Created database directory: " + dbDir.getAbsolutePath());
            }
        }

        // Step 2: Establish connection via DriverManager
        Connection con = DriverManager.getConnection(dbUrl);

        // Step 3: Optimize SQLite for concurrent reading & writing
        try (Statement stmt = con.createStatement()) {
            // WAL mode allows concurrent readers while a transfer is writing
            stmt.execute("PRAGMA journal_mode = WAL;");
            // Wait up to 5000ms if the database is temporarily locked
            stmt.execute("PRAGMA busy_timeout = 5000;");
        } catch (SQLException ignored) {
            // Non-fatal if PRAGMA cannot be applied on in-memory or read-only modes
        }

        return con;
    }

    /**
     * Utility method to safely close a JDBC Connection.
     *
     * @param con the connection to close, safely handles null
     */
    public static void closeConnection(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                System.err.println("⚠️ Warning: Failed to close JDBC Connection: " + e.getMessage());
            }
        }
    }
}

