 import javax.swing.*;
import java.awt.*;
import java.sql.*;
import com.formdev.flatlaf.FlatIntelliJLaf;

public class ECCLoginGUI extends JFrame {
    private JTextField userIdField;
    private JTextField authTokenField;
    private JTextField sessionKeyField;
    private JButton loginButton;
    private JButton continueButton;
    private JTextArea resultArea;
    private String userId = "";
    private String sessionKey = "";
    private ECCLogin eccLogin;

    public ECCLoginGUI() {
        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
        } catch (Exception e) {
            System.err.println("FlatLaf load failed");
        }

        setTitle("🔐 ECCPWS - Secure Login");
        setSize(480, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));

        // Branding/logo panel
        JPanel logoPanel = new JPanel();
        logoPanel.setOpaque(false);
        JLabel logo = new JLabel("🔐");
        logo.setFont(new Font("SansSerif", Font.BOLD, 44));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logoPanel.add(logo);
        add(logoPanel, BorderLayout.NORTH);

        eccLogin = new ECCLogin();

        // Main card panel
        JPanel card = new RoundedPanel(25, new Color(245, 247, 250));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel title = new JLabel("Secure Login", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(18));

        // Input fields with icons
        card.add(createInputRow("👤", "User ID:", userIdField = new JTextField(18)));
        card.add(Box.createVerticalStrut(10));
        card.add(createInputRow("🔑", "Auth Token:", authTokenField = new JTextField(18)));
        card.add(Box.createVerticalStrut(10));
        card.add(createInputRow("🗝️", "Session Key:", sessionKeyField = new JTextField(18)));
        sessionKeyField.setEditable(false);
        card.add(Box.createVerticalStrut(18));

        // Buttons
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 10, 0));
        btnRow.setOpaque(false);
        loginButton = createAccentButton("🔓 Login");
        continueButton = createStyledButton("➡️ Continue");
        continueButton.setEnabled(false);
        btnRow.add(loginButton);
        btnRow.add(continueButton);
        card.add(btnRow);
        card.add(Box.createVerticalStrut(10));
        JButton btnBack = createStyledButton("← Back");
        btnBack.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(btnBack);

        add(card, BorderLayout.CENTER);

        // Result area
        resultArea = new JTextArea(3, 30);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        resultArea.setMargin(new Insets(10, 10, 10, 10));
        resultArea.setBackground(new Color(245, 247, 250));
        add(new JScrollPane(resultArea), BorderLayout.SOUTH);

        // Button actions
        loginButton.addActionListener(e -> authenticateUser());
        continueButton.addActionListener(e -> verifySessionKey());
        btnBack.addActionListener(e -> {
            dispose();
            new MainLauncher().setVisible(true);
        });
    }

    private JPanel createInputRow(String icon, String label, JTextField field) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
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
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return btn;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 15));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(42, 54, 71));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 230), 2));
        return btn;
    }

    // RoundedPanel for modern look
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

    // --- Logic methods (same as your original, but refactored for clarity) ---

    private void authenticateUser() {
        loginButton.setEnabled(false);
        userIdField.setEnabled(false);
        authTokenField.setEnabled(false);

        userId = userIdField.getText().trim();
        String userToken = authTokenField.getText().trim();

        if (userId.isEmpty() || userToken.isEmpty()) {
            resultArea.setText("❌ Please enter both User ID and Auth Token.");
            resetInputFields();
            return;
        }

        String sql = "SELECT public_key, auth_token, role FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                resultArea.setText("❌ User not found.");
                resetInputFields();
                return;
            }

            String storedPublicKey = rs.getString("public_key");
            String storedToken = rs.getString("auth_token");
            String role = rs.getString("role");

            if (!storedToken.equals(userToken)) {
                resultArea.setText("❌ Invalid Auth Token.");
                resetInputFields();
                return;
            }

            resultArea.setText("✅ Token verified.\n");
            sessionKey = fetchSessionKeyFromDB(userId);
            if (sessionKey == null) {
                sessionKey = eccLogin.generateSessionKey(userId, storedPublicKey);
                storeSessionKey(userId, sessionKey);
                resultArea.append("✅ New Session Key generated and stored.\n");
            } else {
                resultArea.append("ℹ️ Existing Session Key fetched from DB.\n");
            }

            sessionKeyField.setText(sessionKey);
            DatabaseHandler.logAudit(userId, role, "Login Token Validated");
            resultArea.append("➡️ Please click Continue to proceed.");
            continueButton.setEnabled(true);

        } catch (SQLException e) {
            resultArea.setText("❌ Database Error: " + e.getMessage());
            e.printStackTrace();
            resetInputFields();
        }
    }

    private void verifySessionKey() {
        continueButton.setEnabled(false);
        String inputSessionKey = sessionKeyField.getText().trim();
        if (inputSessionKey.isEmpty()) {
            resultArea.setText("❌ Please enter the session key.");
            continueButton.setEnabled(true);
            return;
        }

        String sql = "SELECT * FROM sessions WHERE user_id = ? AND session_key = ?";
        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, inputSessionKey);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                resultArea.setText("❌ Invalid session key.");
                continueButton.setEnabled(true);
                return;
            }

            resultArea.setText("✅ Session Key verified. Redirecting...");
            this.dispose();
            DashboardGUI dashboard = new DashboardGUI();
            dashboard.setLoginDetails(userId, inputSessionKey);
            dashboard.setVisible(true);

        } catch (SQLException e) {
            resultArea.setText("❌ Error verifying session key: " + e.getMessage());
            e.printStackTrace();
            continueButton.setEnabled(true);
        }
    }

    private String fetchSessionKeyFromDB(String userId) {
        String sql = "SELECT session_key FROM sessions WHERE user_id = ?";
        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("session_key");
            }
        } catch (SQLException e) {
            resultArea.append("\n❌ Error fetching session key: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private void storeSessionKey(String userId, String sessionKey) {
        String sql = "INSERT INTO sessions (user_id, session_key) VALUES (?, ?)";
        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, sessionKey);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            resultArea.append("\n❌ Failed to store session key: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void resetInputFields() {
        loginButton.setEnabled(true);
        userIdField.setEnabled(true);
        authTokenField.setEnabled(true);
    }
}
