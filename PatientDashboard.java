import javax.swing.*;
import java.awt.*;
import java.security.PrivateKey;
import java.sql.*;

public class PatientDashboard extends JFrame {
    private final String userId;
    private final String sessionKey;
    private final PrivateKey privateKey;
    private JTextArea textArea;

    public PatientDashboard(String userId, String sessionKey, PrivateKey privateKey) {
        this.userId = userId;
        this.sessionKey = sessionKey;
        this.privateKey = privateKey;

        setTitle("👤 Patient Dashboard");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));

        // Header
        JLabel header = new JLabel("Welcome, Patient: " + userId, SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 22));
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(header, BorderLayout.NORTH);

        // Main Card Panel
        JPanel card = new RoundedPanel(25, new Color(245, 247, 250));
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Text Area
        textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 15));
        textArea.setEditable(false);
        textArea.setBackground(new Color(245, 247, 250));
        JScrollPane scrollPane = new JScrollPane(textArea);
        card.add(scrollPane, BorderLayout.CENTER);

        // Bottom Panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnBack = createStyledButton("← Back");
        JButton refreshButton = createAccentButton("🔄 Refresh My Record");
        bottomPanel.setOpaque(false);
        bottomPanel.add(refreshButton);
        bottomPanel.add(btnBack);
        card.add(bottomPanel, BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);

        // Actions
        btnBack.addActionListener(e -> {
            dispose();
            new MainLauncher().setVisible(true);
        });
        refreshButton.addActionListener(e -> fetchPatientData());

        fetchPatientData();
    }

    private void fetchPatientData() {
        textArea.setText("🔍 Loading your encrypted medical record...\n\n");
        if (!isSessionKeyValid(userId, sessionKey)) {
            textArea.setText("❌ Session expired or invalid.");
            return;
        }
        String numericId = userId.replaceAll("\\D", "");
        if (numericId.isEmpty()) {
            textArea.setText("❌ Invalid patient ID format.");
            return;
        }
        int patientId;
        try {
            patientId = Integer.parseInt(numericId);
        } catch (NumberFormatException e) {
            textArea.setText("❌ Invalid patient ID format.");
            return;
        }
        String sql = "SELECT * FROM patients WHERE patient_id = ?";
        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                textArea.append("🧬 Patient ID: " + patientId + "\n\n");
            
                // Vitals
                textArea.append("❤️ Heart Rate: " + decryptField(rs.getString("heart_rate")) + "\n");
                textArea.append("💨 Respiratory Rate: " + decryptField(rs.getString("respiratory_rate")) + "\n");
                textArea.append("🌡️ Body Temperature: " + decryptField(rs.getString("body_temperature")) + "\n");
                textArea.append("🧪 Oxygen Saturation: " + decryptField(rs.getString("oxygen_saturation")) + "\n");
                textArea.append("💓 Blood Pressure: " +
                    decryptField(rs.getString("systolic_bp")) + "/" +
                    decryptField(rs.getString("diastolic_bp")) + " mmHg\n");
            
                // General Info
                textArea.append("\n🎂 Age: " + decryptField(rs.getString("age")) + "\n");
                textArea.append("⚖️ Weight: " + decryptField(rs.getString("weight_kg")) + " kg\n");
                textArea.append("📏 Height: " + decryptField(rs.getString("height_m")) + " m\n");
            
                // Derived
                textArea.append("\n📐 BMI: " + decryptField(rs.getString("derived_bmi")) + "\n");
                textArea.append("💓 HRV: " + decryptField(rs.getString("derived_hrv")) + "\n");
                textArea.append("📊 MAP: " + decryptField(rs.getString("derived_map")) + "\n");
            
                // Risk
                textArea.append("\n⚠️ Risk Category: " + decryptField(rs.getString("risk_category")) + "\n");
                textArea.append("⏱️ Timestamp: " + decryptField(rs.getString("timestamp")) + "\n");
            }else {
                textArea.setText("❌ No record found.");
            }
            DatabaseHandler.logAudit(userId, "patient", "Viewed Own Record");
        } catch (Exception e) {
            textArea.append("❌ Error loading data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String decryptField(String encryptedData) {
        try {
            return SecureDataTransfer.decryptData(encryptedData, privateKey);
        } catch (Exception e) {
            return "❌ Decryption error: " + e.getMessage();
        }
    }

    private boolean isSessionKeyValid(String userId, String sessionKey) {
        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM sessions WHERE user_id = ? AND session_key = ?")) {
            pstmt.setString(1, userId);
            pstmt.setString(2, sessionKey);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- UI Helpers ---
    private JButton createAccentButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 16));
        btn.setBackground(new Color(42, 54, 71));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 15));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(42, 54, 71));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 230), 2));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
    static class RoundedPanel extends JPanel {
        private final int cornerRadius;
        private final Color bgColor;
        public RoundedPanel(int radius, Color bgColor) {
            super();
            this.cornerRadius = radius;
            this.bgColor = bgColor;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            super.paintComponent(g);
        }
    }
}
