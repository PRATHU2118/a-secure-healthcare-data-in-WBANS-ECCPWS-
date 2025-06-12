import javax.swing.*;
import java.awt.*;
import java.security.PrivateKey;
import java.sql.*;

public class DoctorDashboard extends JFrame {
    private final String userId;
    private final String sessionKey;
    private final PrivateKey privateKey;
    private JTextArea textArea;

    public DoctorDashboard(String userId, String sessionKey, PrivateKey privateKey) {
        this.userId = userId;
        this.sessionKey = sessionKey;
        this.privateKey = privateKey;

        setTitle("👨‍⚕️ Doctor Dashboard");
        setSize(700, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));

        // Header
        JLabel header = new JLabel("Welcome, Doctor: " + userId, SwingConstants.CENTER);
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
        JButton refreshButton = createAccentButton("🔄 Refresh Patient Records");
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
        refreshButton.addActionListener(e -> fetchAllPatientData());

        fetchAllPatientData();
    }

    private void fetchAllPatientData() {
        textArea.setText("🔍 Fetching encrypted patient records...\n\n");
        if (!isSessionKeyValid(userId, sessionKey)) {
            textArea.setText("❌ Invalid session. Access denied.");
            return;
        }
        String role = getUserRole(userId);
        if (!"doctor".equalsIgnoreCase(role)) {
            textArea.setText("❌ Only doctors can access this data.");
            return;
        }
        DatabaseHandler.logAudit(userId, role, "Viewed patient records");
        String sql = "SELECT * FROM patients LIMIT 10";
        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            StringBuilder sb = new StringBuilder();
            int recordCount = 0;
            while (rs.next()) {
                sb.append("🩺 Patient ID: ").append(rs.getInt("patient_id")).append("\n");
                sb.append("❤️ Heart Rate: ").append(decryptField(rs.getString("heart_rate"))).append("\n");
                sb.append("💨 Respiratory Rate: ").append(decryptField(rs.getString("respiratory_rate"))).append("\n");
                sb.append("⏱️ Timestamp: ").append(decryptField(rs.getString("timestamp"))).append("\n");
                sb.append("⚠️ Risk Category: ").append(decryptField(rs.getString("risk_category"))).append("\n");
                sb.append("-----------------------------------------------------\n");
                recordCount++;
            }
            sb.append("\n✅ ").append(recordCount).append(" patient records retrieved.\n");
            textArea.setText(sb.toString());
        } catch (Exception e) {
            textArea.setText("❌ Error while fetching records: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String decryptField(String encrypted) {
        try {
            return SecureDataTransfer.decryptData(encrypted, privateKey);
        } catch (Exception e) {
            return "❌ Error decrypting field: " + e.getMessage();
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

    private String getUserRole(String userId) {
        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement("SELECT role FROM users WHERE user_id = ?")) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("role");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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
