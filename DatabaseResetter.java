import javax.swing.*;
import java.sql.Connection;
import java.sql.Statement;

public class DatabaseResetter {
    public static void wipeAllTablesGUI() {
        int confirm = JOptionPane.showConfirmDialog(null,
                "⚠️ This will delete ALL data in patients, users, sessions, and logs.\nAre you sure?",
                "Reset Database", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseHandler.connect();
                 Statement stmt = conn.createStatement()) {

                stmt.executeUpdate("DELETE FROM patients");
                stmt.executeUpdate("DELETE FROM users");
                stmt.executeUpdate("DELETE FROM sessions");
                stmt.executeUpdate("DELETE FROM audit_logs");

                JOptionPane.showMessageDialog(null, "✅ Database reset completed.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "❌ Error resetting DB: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
