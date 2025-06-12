import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class TestECIES {
    public static void main(String[] args) {
        try {
            // ✅ Ensure Bouncy Castle is registered
            Security.addProvider(new BouncyCastleProvider());

            // ✅ Generate ECC Key Pair
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
            keyPairGenerator.initialize(256);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            String originalMessage = "Hello ECC!";
            System.out.println("🔹 Original Message: " + originalMessage);

            // ✅ Encrypt Message
            String encryptedMessage = SecureDataTransfer.encryptData(originalMessage, keyPair.getPublic());
            System.out.println("🔒 Encrypted: " + encryptedMessage);

            // ✅ Decrypt Message
            String decryptedMessage = SecureDataTransfer.decryptData(encryptedMessage, keyPair.getPrivate());
            System.out.println("🔓 Decrypted: " + decryptedMessage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
