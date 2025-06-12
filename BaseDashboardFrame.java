import javax.swing.*;
import java.awt.*;
import com.formdev.flatlaf.FlatIntelliJLaf;

public abstract class BaseDashboardFrame extends JFrame {

    protected JPanel contentPanel;

    public BaseDashboardFrame(String title) {
        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
        } catch (Exception e) {
            System.err.println("Failed to apply FlatLaf");
        }

        setTitle("ECCPWS - " + title);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ✅ Header Panel
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(33, 150, 243)); // Material blue
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        JLabel headerLabel = new JLabel("🔐 ECCPWS - " + title);
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        header.add(headerLabel, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // ✅ Content Panel (to override)
        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(contentPanel, BorderLayout.CENTER);
    }
}
