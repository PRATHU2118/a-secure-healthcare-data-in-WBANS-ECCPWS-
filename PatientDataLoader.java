import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PatientDataLoader {
    private static final String CSV_FILE = "dataset/human_vital_signs_dataset_2024.csv";

    public static void main(String[] args) {
        try {
            // Load and insert dataset without encryption
            loadPatientData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadPatientData() {
        String checkSql = "SELECT COUNT(*) FROM patients WHERE patient_id = ?";
        String insertSql = "INSERT INTO patients (patient_id, heart_rate, respiratory_rate, timestamp, " +
                           "body_temperature, oxygen_saturation, systolic_bp, diastolic_bp, age, gender, " +
                           "weight_kg, height_m, derived_hrv, derived_pulse_pressure, derived_bmi, derived_map, risk_category) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql);
             BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {

            String line;
            int rowCount = 0;
            br.readLine(); // Skip the header row

            while ((line = br.readLine()) != null && rowCount < 500) {
                String[] data = line.split(",");
                int patientId = Integer.parseInt(data[0].trim());

                // Check for duplicate patient IDs
                checkStmt.setInt(1, patientId);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                if (rs.getInt(1) > 0) {
                    System.out.println("⚠ Skipping duplicate Patient ID: " + patientId);
                    continue;
                }

                // Insert raw data (without encryption) into the database
                insertStmt.setInt(1, patientId);
                insertStmt.setString(2, data[1].trim()); // heart_rate
                insertStmt.setString(3, data[2].trim()); // respiratory_rate
                insertStmt.setString(4, data[3].trim()); // timestamp
                insertStmt.setString(5, data[4].trim()); // body_temperature
                insertStmt.setString(6, data[5].trim()); // oxygen_saturation
                insertStmt.setString(7, data[6].trim()); // systolic_bp
                insertStmt.setString(8, data[7].trim()); // diastolic_bp
                insertStmt.setString(9, data[8].trim()); // age
                insertStmt.setString(10, data[9].trim()); // gender
                insertStmt.setString(11, data[10].trim()); // weight_kg
                insertStmt.setString(12, data[11].trim()); // height_m
                insertStmt.setString(13, data[12].trim()); // derived_hrv
                insertStmt.setString(14, data[13].trim()); // derived_pulse_pressure
                insertStmt.setString(15, data[14].trim()); // derived_bmi
                insertStmt.setString(16, data[15].trim()); // derived_map
                insertStmt.setString(17, data[16].trim()); // risk_category

                insertStmt.executeUpdate();
                rowCount++;

                System.out.println("✅ Inserted Row Without Encryption: " + rowCount);
            }
            System.out.println("🎉 Successfully loaded " + rowCount + " rows into SQLite without encryption!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
