import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.Security;
import javax.crypto.Cipher;

public class CheckECIES {
    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());

        System.out.println("🔍 Checking Available Cipher Algorithms...");
        try {
            Cipher cipher = Cipher.getInstance("ECIES", "BC");
            System.out.println("✅ ECIES Algorithm is supported!");
        } catch (Exception e) {
            System.out.println("❌ ECIES Algorithm is NOT supported!");
            e.printStackTrace();
        }
    }
}
