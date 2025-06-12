import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.security.PrivateKey;
import java.sql.*;

public class DataRetrievalGUI extends JFrame {
    private JTextField userIdField;
    private JTextField sessionKeyField;
    private JTextArea outputArea;

    public DataRetrievalGUI() {
        setTitle("ECCPWS - Data Retrieval");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);

        JLabel userIdLabel = new JLabel("User ID:");
        userIdField = new JTextField(20);

        JLabel sessionKeyLabel = new JLabel("Session Key:");
        sessionKeyField = new JTextField(30);

        JButton fetchButton = new JButton("Fetch Records");
        fetchButton.addActionListener(this::fetchData);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(outputArea);

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.add(userIdLabel);
        inputPanel.add(userIdField);
        inputPanel.add(sessionKeyLabel);
        inputPanel.add(sessionKeyField);
        inputPanel.add(new JLabel());
        inputPanel.add(fetchButton);

        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void fetchData(ActionEvent event) {
        String userId = userIdField.getText().trim();
        String sessionKey = sessionKeyField.getText().trim();

        if (userId.isEmpty() || sessionKey.isEmpty()) {
            outputArea.setText("❌ Please enter both User ID and Session Key.");
            return;
        }

        try {
            ECCKeyGen keyGen = new ECCKeyGen();
            PrivateKey privateKey = keyGen.getPrivateKey();

            if (!isSessionKeyValid(userId, sessionKey)) {
                outputArea.setText("❌ Invalid session key.");
                return;
            }

            String role = getUserRole(userId);
            boolean isAdmin = role != null && role.equalsIgnoreCase("admin");
            boolean isPatient = role != null && role.equalsIgnoreCase("patient");

            String sql = isPatient
                ? "SELECT * FROM patients WHERE patient_id = ?"
                : "SELECT * FROM patients LIMIT 5";

            try (Connection conn = DatabaseHandler.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                if (isPatient) pstmt.setInt(1, Integer.parseInt(userId.replaceAll("\\D", "")));
                ResultSet rs = pstmt.executeQuery();

                StringBuilder sb = new StringBuilder();
                while (rs.next()) {
                    sb.append("Patient ID: ").append(rs.getInt("patient_id")).append("\n")
                      .append("Heart Rate: ").append(SecureDataTransfer.decryptData(rs.getString("heart_rate"), privateKey)).append("\n")
                      .append("Respiratory Rate: ").append(SecureDataTransfer.decryptData(rs.getString("respiratory_rate"), privateKey)).append("\n")
                      .append("Timestamp: ").append(SecureDataTransfer.decryptData(rs.getString("timestamp"), privateKey)).append("\n")
                      .append("Risk Category: ").append(SecureDataTransfer.decryptData(rs.getString("risk_category"), privateKey)).append("\n")
                      .append("----------------------------------------\n");
                }
                outputArea.setText(sb.toString().isEmpty() ? "❌ No records found." : sb.toString());

                DatabaseHandler.logAudit(userId, role, "Viewed patient data");
            }
        } catch (Exception e) {
            outputArea.setText("❌ Error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isSessionKeyValid(String userId, String sessionKey) throws SQLException {
        String sql = "SELECT COUNT(*) FROM sessions WHERE user_id = ? AND session_key = ?";
        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, sessionKey);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    private String getUserRole(String userId) throws SQLException {
        String sql = "SELECT role FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getString("role") : null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DataRetrievalGUI().setVisible(true));
    }
}
