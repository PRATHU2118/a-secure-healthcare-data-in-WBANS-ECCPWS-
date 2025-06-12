import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;
import java.security.PrivateKey;


public class ECCLoginRouter {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JTextField userField = new JTextField();
            JTextField sessionField = new JTextField();

            Object[] fields = {
                "Enter User ID:", userField,
                "Enter Session Key:", sessionField
            };

            int option = JOptionPane.showConfirmDialog(null, fields, "ECCPWS Login Router", JOptionPane.OK_CANCEL_OPTION);

            if (option == JOptionPane.OK_OPTION) {
                String userId = userField.getText().trim();
                String sessionKey = sessionField.getText().trim();

                if (userId.isEmpty() || sessionKey.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "❌ Please enter both fields.");
                    return;
                }

                if (!isSessionKeyValid(userId, sessionKey)) {
                    JOptionPane.showMessageDialog(null, "❌ Invalid session key or user ID!");
                    return;
                }

                String role = getUserRole(userId);
                if (role == null) {
                    JOptionPane.showMessageDialog(null, "❌ User role not found!");
                    return;
                }

                // Load ECC Private Key
                PrivateKey privateKey = null;
                try {
                    ECCKeyGen keyGen = new ECCKeyGen();
                    privateKey = keyGen.getPrivateKey();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "❌ Failed to load private key.");
                    return;
                }

                // Route based on role
                switch (role.toLowerCase()) {
                    case "admin":
                        new AdminDashboard(userId, sessionKey);
                        break;
                    case "doctor":
                        new DoctorDashboard(userId, sessionKey, privateKey);
                        break;
                    case "patient":
                        new PatientDashboard(userId, sessionKey, privateKey);
                        break;
                    default:
                        JOptionPane.showMessageDialog(null, "❌ Invalid role: " + role);
                }
            }
        });
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
        } catch (Exception e) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
