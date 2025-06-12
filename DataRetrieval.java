import java.security.PrivateKey;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;

public class DataRetrieval {

    private PrivateKey decryptionKey;

    public DataRetrieval(PrivateKey privateKey) {
        this.decryptionKey = privateKey;
    }

    // ✅ Fetch and decrypt patient data
    public void fetchDecryptedData(String userId, String sessionKey) {
        if (!isSessionKeyValid(userId, sessionKey)) {
            JOptionPane.showMessageDialog(null, "❌ Invalid Session Key! Access Denied.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String role = getUserRole(userId);
        if (role == null) {
            JOptionPane.showMessageDialog(null, "❌ Unable to determine user role. Access Denied.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = role.equalsIgnoreCase("patient")
                ? "SELECT * FROM patients WHERE patient_id = ?"
                : "SELECT * FROM patients LIMIT 5";

        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (role.equalsIgnoreCase("patient")) {
                pstmt.setInt(1, Integer.parseInt(userId));
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String decryptedData = "\n🔓 Decrypting Patient Record:\n";
                decryptedData += "Patient ID: " + rs.getInt("patient_id") + "\n";
                decryptedData += "Heart Rate: " + SecureDataTransfer.decryptData(rs.getString("heart_rate"), decryptionKey) + "\n";
                decryptedData += "Respiratory Rate: " + SecureDataTransfer.decryptData(rs.getString("respiratory_rate"), decryptionKey) + "\n";
                decryptedData += "Timestamp: " + SecureDataTransfer.decryptData(rs.getString("timestamp"), decryptionKey) + "\n";
                decryptedData += "Risk Category: " + SecureDataTransfer.decryptData(rs.getString("risk_category"), decryptionKey) + "\n";
                decryptedData += "-----------------------------------";

                // Display data in a JOptionPane
                JOptionPane.showMessageDialog(null, decryptedData, "Patient Data", JOptionPane.INFORMATION_MESSAGE);
            }

            DatabaseHandler.logAudit(userId, role, "Viewed patient data");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "❌ Error fetching data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ✅ Check session validity
    private boolean isSessionKeyValid(String userId, String sessionKey) {
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

    // ✅ Get user role from users table
    private String getUserRole(String userId) {
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

    // ✅ GUI handler for role-based detail display
    public static void showDetails(String userId, String role) {
        PrivateKey privateKey = UserAuth.getPrivateKeyForUser(userId);
        if (privateKey == null) {
            JOptionPane.showMessageDialog(null,
                "❌ Could not load private key for user.",
                "Decryption Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        switch (role.toLowerCase()) {
            case "doctor":
                new DoctorDashboard(userId, role, privateKey).setVisible(true);
                break;

            case "patient":
                // No need to check for digit-only IDs — trust users table
                new PatientDashboard(userId, role, privateKey).setVisible(true);
                break;

            case "admin":
                JOptionPane.showMessageDialog(null,
                    "🔍 Admin dashboard already includes data views.\nCheck logs or system summary.");
                break;

            default:
                JOptionPane.showMessageDialog(null,
                    "❌ Unknown role: " + role);
        }
    }

    // ✅ GUI-based main method
    public static void main(String[] args) {
        try {
            // Get User ID and Session Key via JOptionPane input dialogs
            String userId = JOptionPane.showInputDialog("Enter User ID:");
            String sessionKey = JOptionPane.showInputDialog("Enter Session Key:");

            if (userId == null || sessionKey == null) {
                JOptionPane.showMessageDialog(null, "❌ Invalid Input. Exiting.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            ECCKeyGen keyGen = new ECCKeyGen(); // Simulated key pair
            PrivateKey privateKey = keyGen.getPrivateKey();

            DataRetrieval retriever = new DataRetrieval(privateKey);
            retriever.fetchDecryptedData(userId, sessionKey);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "❌ Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
