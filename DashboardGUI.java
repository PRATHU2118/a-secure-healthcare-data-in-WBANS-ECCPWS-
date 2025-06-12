import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.security.PrivateKey;
import com.formdev.flatlaf.FlatIntelliJLaf;

public class DashboardGUI extends JFrame {
    private JTextField userIdField;
    private JTextField sessionKeyField;
    private JTextArea resultArea;
    private String userId;
    private String sessionKey;
    private String role;

    public DashboardGUI(String userId, String sessionKey) {
        this(); // Build GUI
        setLoginDetails(userId, sessionKey);
    }

    public DashboardGUI() {
        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
        } catch (Exception e) {
            System.err.println("FlatLaf load failed");
        }

        setTitle("🏥 Dashboard Login");
        setSize(520, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(42, 54, 71));
        headerPanel.setLayout(new BorderLayout());
        JLabel header = new JLabel("Session Validation", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 22));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 15, 0));
        headerPanel.add(header, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Main Card Panel
        JPanel card = new RoundedPanel(25, new Color(245, 247, 250));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));

        // Input Fields with Icons
        card.add(createInputRow("👤", "User ID:", userIdField = new JTextField(18)));
        card.add(Box.createVerticalStrut(12));
        card.add(createInputRow("🗝️", "Session Key:", sessionKeyField = new JTextField(18)));
        card.add(Box.createVerticalStrut(20));

        // Validate Button
        JButton submitBtn = createAccentButton("✅ Validate Session");
        submitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(submitBtn);

        add(card, BorderLayout.CENTER);

        // Result Area
        resultArea = new JTextArea(3, 30);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        resultArea.setMargin(new Insets(10, 15, 10, 15));
        resultArea.setBackground(new Color(245, 247, 250));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 0, 0, 0),
            BorderFactory.createLineBorder(new Color(220, 225, 230), 2)
        ));
        add(scrollPane, BorderLayout.SOUTH);

        // Button Action
        submitBtn.addActionListener(e -> validateSession());
    }

    private JPanel createInputRow(String icon, String label, JTextField field) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 20));
        row.add(iconLabel, BorderLayout.WEST);
        row.add(new JLabel(label), BorderLayout.CENTER);
        row.add(field, BorderLayout.EAST);
        return row;
    }

    private JButton createAccentButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 16));
        btn.setBackground(new Color(42, 54, 71));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // Rounded Panel Implementation
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


    public void setLoginDetails(String userId, String sessionKey) {
        this.userId = userId;
        this.sessionKey = sessionKey;
        userIdField.setText(userId);
        sessionKeyField.setText(sessionKey);
    }

    private void validateSession() {
        validateSession(userIdField.getText().trim(), sessionKeyField.getText().trim());
    }

    private void validateSessionDirect() {
        validateSession(this.userId, this.sessionKey);
    }

    private void validateSession(String userIdInput, String sessionKeyInput) {
        if (userIdInput.isEmpty() || sessionKeyInput.isEmpty()) {
            resultArea.setText("❌ Please enter both User ID and Session Key.");
            return;
        }

        String sessionSql = "SELECT session_key FROM sessions WHERE user_id = ?";
        String userSql = "SELECT role FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement sessionStmt = conn.prepareStatement(sessionSql);
             PreparedStatement userStmt = conn.prepareStatement(userSql)) {

            sessionStmt.setString(1, userIdInput);
            ResultSet sessionRs = sessionStmt.executeQuery();

            if (!sessionRs.next()) {
                resultArea.setText("❌ No session found for this user.");
                return;
            }

            String storedSessionKey = sessionRs.getString("session_key");

            if (!storedSessionKey.equals(sessionKeyInput)) {
                resultArea.setText("❌ Invalid session key.");
                return;
            }

            userStmt.setString(1, userIdInput);
            ResultSet userRs = userStmt.executeQuery();

            if (!userRs.next()) {
                resultArea.setText("❌ No user found with this ID.");
                return;
            }

            String role = userRs.getString("role");

            ECCLogin loginInstance = new ECCLogin();
            PrivateKey privateKey = loginInstance.getPrivateKey(userIdInput);

            switch (role) {
                case "doctor":
                    new DoctorDashboard(userIdInput, sessionKeyInput, privateKey).setVisible(true);
                    break;
                case "patient":
                    new PatientDashboard(userIdInput, sessionKeyInput, privateKey).setVisible(true);
                    break;
                case "admin":
                    new AdminDashboard(userIdInput, sessionKeyInput).setVisible(true);
                    break;
                default:
                    resultArea.setText("❌ Invalid role.");
                    return;
            }

            dispose(); // Close DashboardGUI

        } catch (SQLException e) {
            resultArea.setText("❌ SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
