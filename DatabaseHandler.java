import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHandler {
    private static final String DB_URL = "jdbc:sqlite:eccpws.db";

    // ✅ Global DB Connector with WAL mode
    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);

            // 🛡️ Enable Write-Ahead Logging to prevent file locks
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL;");
            }

            System.out.println("✅ SQLite Database connected successfully!");
        } catch (SQLException e) {
            System.out.println("❌ SQLite Connection Error: " + e.getMessage());
        }
        return conn;
    }

    // ✅ Create Patients Table
    public static void createPatientsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS patients (" +
                     "patient_id INTEGER PRIMARY KEY, " +
                     "heart_rate TEXT, " +
                     "respiratory_rate TEXT, " +
                     "timestamp TEXT, " +
                     "body_temperature TEXT, " +
                     "oxygen_saturation TEXT, " +
                     "systolic_bp TEXT, " +
                     "diastolic_bp TEXT, " +
                     "age TEXT, " +
                     "gender TEXT, " +
                     "weight_kg TEXT, " +  
                     "height_m TEXT, " +   
                     "derived_hrv TEXT, " +
                     "derived_pulse_pressure TEXT, " +
                     "derived_bmi TEXT, " +
                     "derived_map TEXT, " +
                     "risk_category TEXT)";
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Table 'patients' created successfully!");
        } catch (SQLException e) {
            System.out.println("❌ Error creating 'patients' table!");
            e.printStackTrace();
        }
    }

    // ✅ Create Users Table (includes private_key now)
    public static void createUsersTable() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                     "user_id TEXT PRIMARY KEY, " +
                     "public_key TEXT, " +
                     "private_key TEXT, " +
                     "auth_token TEXT, " +
                     "role TEXT CHECK(role IN ('admin', 'doctor', 'patient')) NOT NULL DEFAULT 'patient')";
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Table 'users' created successfully!");
        } catch (SQLException e) {
            System.out.println("❌ Error creating 'users' table!");
            e.printStackTrace();
        }
    }

    // ✅ Create Audit Logs Table
    public static void createAuditLogsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS audit_logs (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "user_id TEXT, " +
                     "role TEXT, " +
                     "action TEXT, " +
                     "timestamp TEXT)";
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Table 'audit_logs' created successfully!");
        } catch (SQLException e) {
            System.out.println("❌ Error creating 'audit_logs' table!");
            e.printStackTrace();
        }
    }

    // ✅ Create Sessions Table
    public static void createSessionsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS sessions (" +
                     "session_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "user_id TEXT, " +
                     "session_key TEXT, " +
                     "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                     "FOREIGN KEY(user_id) REFERENCES users(user_id))";
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Table 'sessions' created successfully!");
        } catch (SQLException e) {
            System.out.println("❌ Error creating 'sessions' table!");
            e.printStackTrace();
        }
    }

    // ✅ Insert audit log entry
    public static void logAudit(String userId, String role, String action) {
        String sql = "INSERT INTO audit_logs (user_id, role, action, timestamp) VALUES (?, ?, ?, datetime('now'))";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, role);
            pstmt.setString(3, action);
            pstmt.executeUpdate();
            System.out.println("✅ Audit log added: [" + userId + ", " + role + ", " + action + "]");
        } catch (SQLException e) {
            System.out.println("❌ Failed to insert audit log:");
            e.printStackTrace();
        }
    }

    // ✅ Optional: create all tables in one go
    public static void createAllTables() {
        createPatientsTable();
        createUsersTable();
        createSessionsTable();
        createAuditLogsTable();
    }

    // 🧪 Run this manually to create tables
    public static void main(String[] args) {
        createAllTables();
    }
}
