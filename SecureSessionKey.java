
import javax.crypto.KeyAgreement;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

public class SecureSessionKey {
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private byte[] sharedSecret;

    public SecureSessionKey() throws Exception {
        generateKeyPair();
    }

    private void generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("EC");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
        keyPairGen.initialize(ecSpec, new SecureRandom());

        KeyPair keyPair = keyPairGen.generateKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void generateSharedSecret(PublicKey receivedPublicKey) throws Exception {
        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
        keyAgreement.init(this.privateKey);
        keyAgreement.doPhase(receivedPublicKey, true);
        this.sharedSecret = keyAgreement.generateSecret();
    }

    public String getSharedSecretBase64() {
        if (sharedSecret == null) {
            System.out.println("⚠ ERROR: Shared secret is NULL! Ensure `generateSharedSecret()` is called.");
            return null;
        }
        return Base64.getEncoder().encodeToString(sharedSecret);
    }
}
