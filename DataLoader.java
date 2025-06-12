import java.io.BufferedReader;
import java.io.FileReader;
import java.security.PublicKey;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;

public class DataLoader {
    private static final String CSV_FILE = "dataset/human_vital_signs_dataset_2024.csv";
    private static PublicKey encryptionKey;

    public static void main(String[] args) {
        try {
            // Load existing ECC public key
            ECCKeyGen keyGen = new ECCKeyGen();
            encryptionKey = keyGen.getPublicKey();

            if (encryptionKey == null) {
                System.out.println("❌ Error: Public Key is NULL. Encryption cannot proceed.");
                return;
            }

            System.out.println("🔑 Using Saved Public Key: " + Base64.getEncoder().encodeToString(encryptionKey.getEncoded()));

            // Load and encrypt dataset
            loadCSVData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadCSVData() {
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

            // Skip the header row
            br.readLine();

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

                // Encrypt data and verify
                try {
                    String encryptedHeartRate = SecureDataTransfer.encryptData(data[1].trim(), encryptionKey);
                    String encryptedRespiratoryRate = SecureDataTransfer.encryptData(data[2].trim(), encryptionKey);
                    String encryptedTimestamp = SecureDataTransfer.encryptData(data[3].trim(), encryptionKey);
                    String encryptedBodyTemperature = SecureDataTransfer.encryptData(data[4].trim(), encryptionKey);
                    String encryptedOxygenSaturation = SecureDataTransfer.encryptData(data[5].trim(), encryptionKey);
                    String encryptedSystolicBP = SecureDataTransfer.encryptData(data[6].trim(), encryptionKey);
                    String encryptedDiastolicBP = SecureDataTransfer.encryptData(data[7].trim(), encryptionKey);
                    String encryptedAge = SecureDataTransfer.encryptData(data[8].trim(), encryptionKey);
                    String encryptedGender = SecureDataTransfer.encryptData(data[9].trim(), encryptionKey);
                    String encryptedWeightKG = SecureDataTransfer.encryptData(data[10].trim(), encryptionKey);
                    String encryptedHeightM = SecureDataTransfer.encryptData(data[11].trim(), encryptionKey);
                    String encryptedHRV = SecureDataTransfer.encryptData(data[12].trim(), encryptionKey);
                    String encryptedPulsePressure = SecureDataTransfer.encryptData(data[13].trim(), encryptionKey);
                    String encryptedBMI = SecureDataTransfer.encryptData(data[14].trim(), encryptionKey);
                    String encryptedMAP = SecureDataTransfer.encryptData(data[15].trim(), encryptionKey);
                    String encryptedRiskCategory = SecureDataTransfer.encryptData(data[16].trim(), encryptionKey);

                    // Debugging: Print Encrypted Values
                    System.out.println("🔓 Patient ID: " + patientId);
                    System.out.println("💡 Plain Heart Rate: " + data[1].trim());
                    System.out.println("🔒 Encrypted Heart Rate: " + encryptedHeartRate);

                    // Insert encrypted data into database
                    insertStmt.setInt(1, patientId);
                    insertStmt.setString(2, encryptedHeartRate);
                    insertStmt.setString(3, encryptedRespiratoryRate);
                    insertStmt.setString(4, encryptedTimestamp);
                    insertStmt.setString(5, encryptedBodyTemperature);
                    insertStmt.setString(6, encryptedOxygenSaturation);
                    insertStmt.setString(7, encryptedSystolicBP);
                    insertStmt.setString(8, encryptedDiastolicBP);
                    insertStmt.setString(9, encryptedAge);
                    insertStmt.setString(10, encryptedGender);
                    insertStmt.setString(11, encryptedWeightKG);
                    insertStmt.setString(12, encryptedHeightM);
                    insertStmt.setString(13, encryptedHRV);
                    insertStmt.setString(14, encryptedPulsePressure);
                    insertStmt.setString(15, encryptedBMI);
                    insertStmt.setString(16, encryptedMAP);
                    insertStmt.setString(17, encryptedRiskCategory);

                    insertStmt.executeUpdate();
                    rowCount++;

                    System.out.println("✅ Inserted Encrypted Row: " + rowCount);
                } catch (Exception encryptionError) {
                    System.out.println("❌ Error Encrypting Patient ID: " + patientId);
                    encryptionError.printStackTrace();
                }
            }

            System.out.println("🎉 Successfully loaded " + rowCount + " encrypted rows into SQLite!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
