import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Scanner;

public class UserRegistration {
    private String userId;
    private ECCKeyGen userKeys;
    private String authToken;
    private String role;

    public UserRegistration(String userId, String role) throws Exception {
        this.userId = userId;
        this.role = role;
        this.userKeys = new ECCKeyGen(); // Generate ECC key pair
        generateAuthToken();
    }

    private void generateAuthToken() {
        SecureRandom random = new SecureRandom();
        int randomNum = random.nextInt(1000);
        this.authToken = Base64.getEncoder().encodeToString((randomNum + userId).getBytes());
    }

    public void registerUser() {
        String sql = "INSERT INTO users (user_id, public_key, auth_token, role) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, Base64.getEncoder().encodeToString(userKeys.getPublicKey().getEncoded()));
            pstmt.setString(3, authToken);
            pstmt.setString(4, role);

            pstmt.executeUpdate();

            // ✅ Log the registration
            DatabaseHandler.logAudit(userId, role, "User Registered");

            System.out.println("✅ User Registered Successfully!");
            System.out.println("👤 User ID: " + userId);
            System.out.println("🔐 Role: " + role);
            System.out.println("🔑 Public Key: " + Base64.getEncoder().encodeToString(userKeys.getPublicKey().getEncoded()));
            System.out.println("🪪 Auth Token: " + authToken);

        } catch (SQLException e) {
            System.out.println("❌ Failed to register user!");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter User ID: ");
        String userId = scanner.nextLine();

        System.out.print("Enter Role (admin / doctor / patient): ");
        String role = scanner.nextLine().toLowerCase();

        if (!role.equals("admin") && !role.equals("doctor") && !role.equals("patient")) {
            System.out.println("⚠ Invalid role entered. Defaulting to 'patient'.");
            role = "patient";
        }

        try {
            UserRegistration reg = new UserRegistration(userId, role);
            reg.registerUser();
        } catch (Exception e) {
            e.printStackTrace();
        }

        scanner.close();
    }
}
