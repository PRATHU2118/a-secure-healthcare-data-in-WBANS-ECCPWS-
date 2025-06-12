import javax.swing.*;
import java.awt.*;
import com.formdev.flatlaf.FlatIntelliJLaf;

public class AdminDashboard extends JFrame {
    private final String userId;
    private final String sessionKey;

    public AdminDashboard(String userId, String sessionKey) {
        this.userId = userId;
        this.sessionKey = sessionKey;

        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
        } catch (Exception ex) {
            System.err.println("FlatLaf setup failed");
        }

        setTitle("🛡️ ECCPWS - Admin Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Layout: Sidebar + Content
        JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerLocation(220);
        splitPane.setDividerSize(2);
        splitPane.setEnabled(false);

        // Sidebar
        JPanel sidebar = new RoundedPanel(25, new Color(245, 247, 250));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(30, 15, 30, 15));

        JLabel logo = new JLabel("🔐 ECCPWS");
        logo.setFont(new Font("SansSerif", Font.BOLD, 22));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnAudit = createAccentButton("📋 View Audit Logs");
        JButton btnUsers = createStyledButton("👥 View Users");
        JButton btnReload = createStyledButton("🔄 Reload Patient Data");
        JButton btnBack = createStyledButton("← Back");
        JButton btnExit = createStyledButton("🚪 Exit");

        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(40));
        sidebar.add(btnAudit);
        sidebar.add(Box.createVerticalStrut(15));
        sidebar.add(btnUsers);
        sidebar.add(Box.createVerticalStrut(15));
        sidebar.add(btnReload);
        sidebar.add(Box.createVerticalStrut(40));
        sidebar.add(btnBack);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(btnExit);

        // Main Content Panel
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.white);

        JLabel welcomeLabel = new JLabel("Welcome, Admin: " + userId);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextArea displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 15));
        JScrollPane scrollPane = new JScrollPane(displayArea);

        content.add(welcomeLabel, BorderLayout.NORTH);
        content.add(scrollPane, BorderLayout.CENTER);

        // Event Listeners
        btnAudit.addActionListener(e -> {
            displayArea.setText("🔍 Fetching audit logs...\n\n");
            ViewAuditLogs.displayAuditLogsGUI();
        });
        btnUsers.addActionListener(e -> AdminUtils.viewUsersGUI());
        btnReload.addActionListener(e -> AdminUtils.reloadPatientData());
        btnBack.addActionListener(e -> {
            dispose();
            new MainLauncher().setVisible(true);
        });
        btnExit.addActionListener(e -> System.exit(0));

        // Assemble
        splitPane.setLeftComponent(sidebar);
        splitPane.setRightComponent(content);
        add(splitPane);
    }

    // --- UI Helpers ---
    private JButton createAccentButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 16));
        btn.setBackground(new Color(42, 54, 71));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
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
