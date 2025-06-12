import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.Base64;

public class UserAuth {

    // 🔐 Auth flow demo
    public static void main(String[] args) {
        try {
            ECCKeyGen user = new ECCKeyGen();
            ECCKeyGen applicationServer = new ECCKeyGen();

            SecureRandom random = new SecureRandom();
            int r = random.nextInt(1000);

            String timestamp = Instant.now().toString();
            String authRequest = "User123|" + r + "|" + timestamp;
            String encryptedRequest = Base64.getEncoder().encodeToString(authRequest.getBytes());

            System.out.println("📩 Sending Authentication Request to AS: " + encryptedRequest);

            // AS Verifies
            boolean isValid = timestampWithinLimit(timestamp);
            if (isValid) {
                System.out.println("✅ Authentication Approved!");
            } else {
                System.out.println("❌ Authentication Rejected (Timestamp Invalid)");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ⏱️ Timestamp validator
    private static boolean timestampWithinLimit(String timestamp) {
        Instant now = Instant.now();
        Instant received = Instant.parse(timestamp);
        return Math.abs(now.getEpochSecond() - received.getEpochSecond()) <= 5; // 5-second window
    }

    // ✅ Key fetcher for GUI authentication/decryption
    public static PrivateKey getPrivateKeyForUser(String userId) {
        String sql = "SELECT private_key FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String privateKeyStr = rs.getString("private_key");
                byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyStr);

                KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
                return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
            }
        } catch (Exception e) {
            System.out.println("❌ Error fetching private key for user: " + userId);
            e.printStackTrace();
        }

        return null;
    }
}
