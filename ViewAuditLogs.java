import java.sql.*;
import java.util.Scanner;
import javax.swing.*;

public class ViewAuditLogs {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter User ID: ");
        String userId = scanner.nextLine();

        System.out.print("Enter Session Key: ");
        String sessionKey = scanner.nextLine();

        if (!isSessionKeyValid(userId, sessionKey)) {
            System.out.println("❌ Invalid session. Access denied.");
            return;
        }

        String role = getUserRole(userId);
        if (!"admin".equalsIgnoreCase(role)) {
            System.out.println("❌ Access denied. Only admins can view audit logs.");
            return;
        }

        displayAuditLogs();
    }

    private static boolean isSessionKeyValid(String userId, String sessionKey) {
        String sql = "SELECT COUNT(*) FROM sessions WHERE user_id = ? AND session_key = ?";

        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, sessionKey);

            ResultSet rs = pstmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

     public static void displayAuditLogsGUI() {
    JFrame frame = new JFrame("📋 Audit Logs");
    frame.setSize(600, 400);
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.setLocationRelativeTo(null);

    JTextArea textArea = new JTextArea();
    textArea.setEditable(false);
    JScrollPane scrollPane = new JScrollPane(textArea);
    frame.add(scrollPane);

    String sql = "SELECT * FROM audit_logs ORDER BY timestamp DESC";
    try (Connection conn = DatabaseHandler.connect();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

        while (rs.next()) {
            textArea.append("User ID: " + rs.getString("user_id") + "\n");
            textArea.append("Role: " + rs.getString("role") + "\n");
            textArea.append("Action: " + rs.getString("action") + "\n");
            textArea.append("Timestamp: " + rs.getString("timestamp") + "\n");
            textArea.append("------------------------------------\n");
        }
    } catch (SQLException e) {
        textArea.setText("❌ Failed to fetch audit logs.");
        e.printStackTrace();
    }

    frame.setVisible(true);
}

    
    private static String getUserRole(String userId) {
        String sql = "SELECT role FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void displayAuditLogs() {
        String sql = "SELECT * FROM audit_logs ORDER BY timestamp DESC";

        try (Connection conn = DatabaseHandler.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n📋 AUDIT LOGS:");
            System.out.println("------------------------------------------------------");
            while (rs.next()) {
                System.out.println("User ID: " + rs.getString("user_id"));
                System.out.println("Role: " + rs.getString("role"));
                System.out.println("Action: " + rs.getString("action"));
                System.out.println("Timestamp: " + rs.getString("timestamp"));
                System.out.println("------------------------------------------------------");
            }

        } catch (SQLException e) {
            System.out.println("❌ Failed to retrieve audit logs.");
            e.printStackTrace();
        }
    }
}
