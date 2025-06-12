import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class RoleBasedDashboard extends JFrame {
    private final String userId;
    private final String role;

    public RoleBasedDashboard(String userId, String role) {
        this.userId = userId;
        this.role = role;

        setTitle("🏥 Role-Based Dashboard");
        setSize(500, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Create a welcome label
        JLabel welcomeLabel = new JLabel("Welcome, " + role + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        add(welcomeLabel, BorderLayout.NORTH);

        // Create a text area for user info
        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setText("🆔 User ID: " + userId + "\n🔐 Role: " + role);
        infoArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        infoArea.setMargin(new Insets(10, 10, 10, 10));
        add(infoArea, BorderLayout.CENTER);

        // Create button panel with "Display Details" and "Back" buttons
        JPanel buttonPanel = new JPanel();
        JButton btnDetails = new JButton("📋 Display Details");
        btnDetails.addActionListener(this::displayDetails);

        JButton btnBack = new JButton("← Back");
        btnBack.addActionListener(e -> {
            dispose();
            new MainLauncher().setVisible(true); // Return to the main launcher
        });

        buttonPanel.add(btnDetails);
        buttonPanel.add(btnBack);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // Action to display details when the button is clicked
    private void displayDetails(ActionEvent e) {
        DataRetrieval.showDetails(userId, role); // Call the showDetails method
    }

    // Main method for testing (This can be removed when integrating into the system)
    public static void main(String[] args) {
        new RoleBasedDashboard("demoUser", "doctor").setVisible(true); // For testing with demo user
    }
}
