import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.*;
import java.util.Base64;

public class ECCLogin {
    private JTextField userIdField;
    private JTextField authTokenField;
    private JButton loginButton;
    private JTextArea resultArea;

    private static final String DB_URL = "jdbc:sqlite:eccpws.db";

    public ECCLogin() {
        
        // Title Header
        JLabel title = new JLabel("Login to Healthcare System", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        //add(title, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userIdLabel = new JLabel("User ID:");
        userIdField = new JTextField();

        JLabel authTokenLabel = new JLabel("Auth Token:");
        authTokenField = new JTextField();

        loginButton = new JButton("Login");
        loginButton.setFont(new Font("SansSerif", Font.PLAIN, 16));
        loginButton.addActionListener(e -> authenticateUser());

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(userIdLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(userIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(authTokenLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(authTokenField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        formPanel.add(loginButton, gbc);

        //add(formPanel, BorderLayout.CENTER);

        // Result Area
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        resultArea.setMargin(new Insets(10, 10, 10, 10));
        resultArea.setForeground(new Color(40, 40, 40));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Login Result"));
        //add(scrollPane, BorderLayout.SOUTH);

        //setVisible(true);
    }

    private void authenticateUser() {
        String userId = userIdField.getText().trim();
        String userToken = authTokenField.getText().trim();

        if (userId.isEmpty() || userToken.isEmpty()) {
            resultArea.setText("❌ Please enter both User ID and Auth Token.");
            return;
        }

        String sql = "SELECT public_key, auth_token, role FROM users WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                resultArea.setText("❌ User Not Found!");
                return;
            }

            String storedPublicKey = rs.getString("public_key");
            String storedToken = rs.getString("auth_token");
            String role = rs.getString("role");

            if (!storedToken.equals(userToken)) {
                resultArea.setText("❌ Invalid Authentication Token!");
                return;
            }

            // Generate session key
            String sessionKey = generateSessionKey(userId,storedPublicKey);

            if (sessionKey == null) {
                resultArea.setText("❌ Failed to generate session key!");
                return;
            }

            // Store session key in the database
            storeSessionKey(userId, sessionKey);

            // Log audit and show successful login message
            resultArea.setText("✅ Authentication Successful!\n" +
                               "🔐 Role: " + role + "\n" +
                               "🔑 Session Key: " + sessionKey);

            // Open next GUI or dashboard
            new DashboardGUI(userId, sessionKey).setVisible(true);
           // dispose();

        } catch (SQLException e) {
            resultArea.setText("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to generate the session key
    public String generateSessionKey(String userId,String storedPublicKey) {
        try {
            // ECC session key generation
            PublicKey publicKey = KeyFactory.getInstance("EC")
                    .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(storedPublicKey)));

            SecureSessionKey sessionKeyGen = new SecureSessionKey();
            sessionKeyGen.generateSharedSecret(publicKey);
            String sessionKey = sessionKeyGen.getSharedSecretBase64();

            if (sessionKey == null) {
                return null;  // Return null if the session key generation fails
            }

            return sessionKey;  // Return the generated session key

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    public static PrivateKey getPrivateKey(String userId) {
        String sql = "SELECT private_key FROM users WHERE user_id = ?";
    
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String privateKeyStr = rs.getString("private_key");
                if (privateKeyStr == null) {
                    System.err.println("Private key is null for user_id: " + userId);
                    return null;
                }
                byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyStr);
                KeyFactory keyFactory = KeyFactory.getInstance("EC");
                PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
                return keyFactory.generatePrivate(privateKeySpec);
            } else {
                System.err.println("No user found with user_id: " + userId);
                return null;
            }
        } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.err.println("Error retrieving private key for user_id: " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    


    // Method to store the session key in the database
    private void storeSessionKey(String userId, String sessionKey) {
        String sql = "INSERT INTO sessions (user_id, session_key) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, sessionKey);
            pstmt.executeUpdate();
            System.out.println("✅ Session key stored successfully.");

        } catch (SQLException e) {
            System.out.println("❌ Failed to store session key.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //SwingUtilities.invokeLater(ECCLogin::new);
    }
}
