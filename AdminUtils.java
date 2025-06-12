import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class AdminUtils {

    public static void viewUsersGUI() {
        String[] columns = {"User ID", "Role"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        try (Connection conn = DatabaseHandler.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT user_id, role FROM users")) {

            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("user_id"), rs.getString("role")});
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);

        JFrame frame = new JFrame("👥 Registered Users");
        frame.setSize(400, 300);
        frame.add(scroll);
        frame.setVisible(true);
    }

    public static void reloadPatientData() {
        try {
            PatientDataLoader.loadPatientData(); // Ensure this is static
            JOptionPane.showMessageDialog(null, "✅ Patient data reloaded successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "❌ Failed to reload patient data!");
            e.printStackTrace();
        }
    }
}
