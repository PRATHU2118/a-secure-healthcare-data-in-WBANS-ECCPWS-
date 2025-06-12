import javax.swing.*;
import java.awt.*;
import com.formdev.flatlaf.FlatIntelliJLaf;

public class MainLauncher extends JFrame {

    public MainLauncher() {
        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
        } catch (Exception e) {
            System.err.println("FlatLaf not applied.");
        }

        setTitle("🚀 ECCPWS Launcher");
        setSize(480, 540);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(0,0,0,0));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(42, 54, 71));
        headerPanel.setLayout(new BorderLayout());
        JLabel header = new JLabel("Secure Healthcare Portal", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 26));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(36, 0, 18, 0));
        headerPanel.add(header, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Main Content Panel (with rounded corners)
        JPanel contentPanel = new RoundedPanel(30, new Color(245, 247, 250));
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Buttons
        JButton btnLogin = createStyledButton("🔐  ECC GUI Login");
        JButton btnRegister = createStyledButton("👤  Register User");
        JButton btnLoadData = createStyledButton("📄  Load Patient Data");
        JButton btnClearDB = createStyledButton("🧹  Clear All Data (Manual)");
        JButton btnExit = createStyledButton("🚪  Exit");
        JButton themeToggle = createAccentButton("🌓  Toggle Theme");

        // Add buttons with spacing
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(btnLogin);
        contentPanel.add(Box.createVerticalStrut(12));
        contentPanel.add(btnRegister);
        contentPanel.add(Box.createVerticalStrut(12));
        contentPanel.add(btnLoadData);
        contentPanel.add(Box.createVerticalStrut(12));
        contentPanel.add(btnClearDB);
        contentPanel.add(Box.createVerticalStrut(12));
        contentPanel.add(themeToggle);
        contentPanel.add(Box.createVerticalStrut(18));
        contentPanel.add(btnExit);
        contentPanel.add(Box.createVerticalGlue());

        add(contentPanel, BorderLayout.CENTER);

        // Button Actions
        btnLogin.addActionListener(e -> {
            dispose();
            new ECCLoginGUI().setVisible(true);
        });
        btnRegister.addActionListener(e -> {
            dispose();
            new UserRegistrationGUI().setVisible(true);
        });
        btnLoadData.addActionListener(e -> DataUtils.loadEncryptedDataGUI());
        btnClearDB.addActionListener(e -> DatabaseResetter.wipeAllTablesGUI());
        btnExit.addActionListener(e -> System.exit(0));
        themeToggle.addActionListener(e -> ThemeManager.toggleTheme(this));
    }

    // Helper to create styled buttons
    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 17));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(42, 54, 71));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 230), 2),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }

    // Helper to create accent (theme) button
    private JButton createAccentButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(42, 54, 71));
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(42, 54, 71), 2),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }

    // Rounded panel for modern look
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

    public static void main(String[] args) {
        ThemeManager.applyInitialTheme();
        SplashScreen splash = new SplashScreen();
        splash.showSplash(1800); // 1.8 seconds
        SwingUtilities.invokeLater(() -> new MainLauncher().setVisible(true));
    }
}
