import javax.swing.*;

public class DataUtils {
    public static void loadEncryptedDataGUI() {
        try {
            DataLoader.main(null); // uses your existing loader
            JOptionPane.showMessageDialog(null, "✅ Patient data loaded successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "❌ Failed to load data: " + e.getMessage());
        }
    }
}
