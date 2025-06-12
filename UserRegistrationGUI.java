import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import com.formdev.flatlaf.FlatIntelliJLaf;

public class UserRegistrationGUI extends JFrame {
    private JTextField userIdField;
    private JComboBox<String> roleComboBox;
    private JTextArea resultArea;

    public UserRegistrationGUI() {
        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
        } catch (Exception e) {
            System.err.println("FlatLaf load failed");
        }

        setTitle("👤 ECCPWS - Register New User");
        setSize(480, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        JLabel header = new JLabel("Register New User", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 22));
        header.setBorder(BorderFactory.createEmptyBorder(28, 0, 10, 0));
        headerPanel.add(header);
        add(headerPanel, BorderLayout.NORTH);

        // Card Panel
        JPanel card = new RoundedPanel(25, new Color(245, 247, 250));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // User ID
        card.add(createInputRow("👤", "User ID:", userIdField = new JTextField(16)));
        card.add(Box.createVerticalStrut(12));

        // Role
        JPanel rolePanel = new JPanel(new BorderLayout(8, 0));
        rolePanel.setOpaque(false);
        JLabel roleIcon = new JLabel("🎭");
        roleIcon.setFont(new Font("SansSerif", Font.PLAIN, 20));
        rolePanel.add(roleIcon, BorderLayout.WEST);
        rolePanel.add(new JLabel("Role:"), BorderLayout.CENTER);
        roleComboBox = new JComboBox<>(new String[]{"admin", "doctor", "patient"});
        roleComboBox.setFont(new Font("SansSerif", Font.PLAIN, 15));
        rolePanel.add(roleComboBox, BorderLayout.EAST);
        card.add(rolePanel);
        card.add(Box.createVerticalStrut(18));

        // Buttons
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 10, 0));
        btnRow.setOpaque(false);
        JButton registerButton = createAccentButton("➕ Register");
        JButton btnBack = createStyledButton("← Back");
        btnRow.add(registerButton);
        btnRow.add(btnBack);
        card.add(btnRow);

        add(card, BorderLayout.CENTER);

        // Result Area
        resultArea = new JTextArea(3, 30);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        resultArea.setMargin(new Insets(10, 10, 10, 10));
        resultArea.setBackground(new Color(245, 247, 250));
        add(new JScrollPane(resultArea), BorderLayout.SOUTH);

        // Actions
        registerButton.addActionListener(this::registerUser);
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

    private void registerUser(ActionEvent e) {
        String userId = userIdField.getText().trim();
        String role = (String) roleComboBox.getSelectedItem();
        if (userId.isEmpty()) {
            resultArea.setText("❌ Please enter a User ID.");
            return;
        }
        try {
            ECCKeyGen keyGen = new ECCKeyGen();
            PublicKey publicKey = keyGen.getPublicKey();
            PrivateKey privateKey = keyGen.getPrivateKey();
            String publicKeyStr = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            String privateKeyStr = Base64.getEncoder().encodeToString(privateKey.getEncoded());
            String token = Base64.getEncoder().encodeToString((userId + "_" + System.currentTimeMillis()).getBytes());
            String sql = "INSERT INTO users (user_id, role, public_key, private_key, auth_token) VALUES (?, ?, ?, ?, ?)";
            try (var conn = DatabaseHandler.connect();
                 var pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, userId);
                pstmt.setString(2, role);
                pstmt.setString(3, publicKeyStr);
                pstmt.setString(4, privateKeyStr);
                pstmt.setString(5, token);
                pstmt.executeUpdate();
                DatabaseHandler.logAudit(userId, role, "User registered");
                resultArea.setText("✅ User Registered Successfully!\n" +
                        "👤 User ID: " + userId + "\n" +
                        "🔑 Auth Token: " + token + "\n\n" +
                        "➡ Please save this token for login.");
            }
        } catch (Exception ex) {
            resultArea.setText("❌ Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserRegistrationGUI().setVisible(true));
    }
}
