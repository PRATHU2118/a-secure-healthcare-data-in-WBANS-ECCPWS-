import java.sql.*;
import java.util.Scanner;

public class Logout {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter User ID: ");
        String userId = scanner.nextLine();

        System.out.print("Enter Session Key: ");
        String sessionKey = scanner.nextLine();

        if (!isSessionKeyValid(userId, sessionKey)) {
            System.out.println("❌ Invalid session. Logout failed.");
            return;
        }

        if (logoutUser(userId, sessionKey)) {
            String role = getUserRole(userId);
            DatabaseHandler.logAudit(userId, role, "User logged out");
            System.out.println("✅ Successfully logged out!");
        } else {
            System.out.println("❌ Logout failed!");
        }
    }

    private static boolean isSessionKeyValid(String userId, String sessionKey) {
        String sql = "SELECT COUNT(*) FROM sessions WHERE user_id = ? AND session_key = ?";

        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, sessionKey);
            ResultSet rs = pstmt.executeQuery();

            return rs.next() && rs.getInt(1) > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean logoutUser(String userId, String sessionKey) {
        String sql = "DELETE FROM sessions WHERE user_id = ? AND session_key = ?";

        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, sessionKey);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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
        return "unknown";
    }
}
