import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.IESParameterSpec;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import java.util.Arrays;

public class SecureDataTransfer {

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
            System.out.println("✅ Bouncy Castle Security Provider Added!");
        }
    }

    public static String encryptData(String data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("ECIESwithAES-CBC", "BC");

        SecureRandom random = new SecureRandom();

        byte[] nonce = new byte[16];
        byte[] derivation = new byte[16];
        byte[] encoding = new byte[16];
        random.nextBytes(nonce);
        random.nextBytes(derivation);
        random.nextBytes(encoding);

        IESParameterSpec params = new IESParameterSpec(derivation, encoding, 128, 128, nonce);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, params);

        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        // Combine nonce + derivation + encoding + encrypted data
        byte[] output = new byte[16 + 16 + 16 + encryptedBytes.length];
        System.arraycopy(nonce, 0, output, 0, 16);
        System.arraycopy(derivation, 0, output, 16, 16);
        System.arraycopy(encoding, 0, output, 32, 16);
        System.arraycopy(encryptedBytes, 0, output, 48, encryptedBytes.length);

        return Base64.getEncoder().encodeToString(output);
    }

    public static String decryptData(String encryptedData, PrivateKey privateKey) throws Exception {
        byte[] allBytes = Base64.getDecoder().decode(encryptedData);

        if (allBytes.length < 48) {
            throw new IllegalArgumentException("Invalid encrypted data: too short.");
        }

        byte[] nonce = Arrays.copyOfRange(allBytes, 0, 16);
        byte[] derivation = Arrays.copyOfRange(allBytes, 16, 32);
        byte[] encoding = Arrays.copyOfRange(allBytes, 32, 48);
        byte[] encryptedBytes = Arrays.copyOfRange(allBytes, 48, allBytes.length);

        Cipher cipher = Cipher.getInstance("ECIESwithAES-CBC", "BC");

        IESParameterSpec params = new IESParameterSpec(derivation, encoding, 128, 128, nonce);
        cipher.init(Cipher.DECRYPT_MODE, privateKey, params);

        // Added error handling for decryption
        try {
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new Exception("Decryption failed: " + e.getMessage(), e);
        }
    }
}