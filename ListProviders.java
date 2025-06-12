import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.Provider;
import java.security.Security;

public class ListProviders {
    public static void main(String[] args) {
        System.out.println("🔍 Checking Installed Security Providers...");

        // ✅ Manually Add Bouncy Castle Provider
        Security.addProvider(new BouncyCastleProvider());

        for (Provider provider : Security.getProviders()) {
            System.out.println("✅ " + provider.getName() + ": " + provider.getInfo());
        }
    }
}
