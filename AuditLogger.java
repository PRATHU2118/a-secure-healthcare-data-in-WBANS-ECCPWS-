import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditLogger {

    public static void log(String userId, String role, String action) {
        String sql = "INSERT INTO audit_logs (user_id, role, action, timestamp) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, role);
            pstmt.setString(3, action);
            pstmt.setString(4, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            pstmt.executeUpdate();
            System.out.println("📄 Audit Log Inserted: " + userId + " - " + action);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
