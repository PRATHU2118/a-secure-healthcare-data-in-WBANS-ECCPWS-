import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;

import javax.swing.*;

public class ThemeManager {
    private static boolean isDark = false;

    public static void applyInitialTheme() {
        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
            isDark = false;
        } catch (Exception e) {
            System.err.println("Failed to apply initial theme");
        }
    }

    public static void toggleTheme(JFrame frame) {
        try {
            if (isDark) {
                UIManager.setLookAndFeel(new FlatIntelliJLaf());
            } else {
                UIManager.setLookAndFeel(new FlatDarculaLaf());
            }
            isDark = !isDark;

            // Refresh all components
            SwingUtilities.updateComponentTreeUI(frame);
        } catch (Exception e) {
            System.err.println("Failed to toggle theme");
        }
    }
}
