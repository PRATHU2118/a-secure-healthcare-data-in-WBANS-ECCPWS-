import javax.swing.*;
import java.awt.*;

public class SplashScreen extends JWindow {

    public SplashScreen() {
        JPanel content = new JPanel();
        content.setBackground(new Color(33, 150, 243)); // Material blue
        content.setLayout(new BorderLayout());

        JLabel title = new JLabel("🔐 ECCPWS", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        content.add(title, BorderLayout.CENTER);

        JLabel loading = new JLabel("Loading secure modules...", SwingConstants.CENTER);
        loading.setFont(new Font("SansSerif", Font.PLAIN, 14));
        loading.setForeground(Color.WHITE);
        content.add(loading, BorderLayout.SOUTH);

        setContentPane(content);
        setSize(400, 200);
        setLocationRelativeTo(null);
    }

    public void showSplash(int durationMillis) {
        setVisible(true);
        try {
            Thread.sleep(durationMillis);
        } catch (InterruptedException ignored) {}
        setVisible(false);
    }
}
