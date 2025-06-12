import java.io.*;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class ECCKeyGen {
    private static final String PUBLIC_KEY_FILE = "publicKey.pem";
    private static final String PRIVATE_KEY_FILE = "privateKey.pem";

    private PrivateKey privateKey;
    private PublicKey publicKey;

    // ✅ Protocol Parameters
    private static final String HASH_FUNCTION = "SHA-256";
    private static final String ELLIPTIC_CURVE_GROUP = "secp256r1";
    private static final String PRIME_P = "FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFF";
    private static final String PRIME_Q = "FFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2FC632551";
    private static final String BASE_POINT_B = "04" +
            "6B17D1F2E12C4247F8BCE6E563A440F277037D812DEB33A0F4A13945D898C296" +
            "4FE342E2FE1A7F9B8EE7EB4A7C0F9E162BCE33576B315ECECBB6406837BF51F5";

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public ECCKeyGen() throws Exception {
        File pubKeyFile = new File(PUBLIC_KEY_FILE);
        File privKeyFile = new File(PRIVATE_KEY_FILE);

        if (pubKeyFile.exists() && privKeyFile.exists()) {
            System.out.println("✅ Loading existing key pair...");
            this.publicKey = loadPublicKey();
            this.privateKey = loadPrivateKey();
        } else {
            System.out.println("🔑 Generating new key pair...");
            generateAndSaveKeys();
        }

        printProtocolParameters();
    }

    private void generateAndSaveKeys() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
        keyPairGenerator.initialize(256);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();

        saveKeyToFile(PUBLIC_KEY_FILE, publicKey.getEncoded());
        saveKeyToFile(PRIVATE_KEY_FILE, privateKey.getEncoded());

        System.out.println("✅ Key pair generated and saved!");
    }

    private void saveKeyToFile(String fileName, byte[] keyBytes) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(keyBytes);
        }
    }

    private PublicKey loadPublicKey() throws Exception {
        byte[] keyBytes = readKeyFromFile(PUBLIC_KEY_FILE);
        KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
        return keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
    }

    private PrivateKey loadPrivateKey() throws Exception {
        byte[] keyBytes = readKeyFromFile(PRIVATE_KEY_FILE);
        KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }

    private byte[] readKeyFromFile(String fileName) throws IOException {
        try (FileInputStream fis = new FileInputStream(fileName)) {
            return fis.readAllBytes();
        }
    }

    // ✅ Public Getter Methods
    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }

    public KeyPair getKeyPair() {
        return new KeyPair(publicKey, privateKey);
    }

    // ✅ Print Protocol Parameters
    public void printProtocolParameters() {
        System.out.println("\n🔷 **ECCPWS Protocol Parameters** 🔷");
        System.out.println("🔹 Hash Function: " + HASH_FUNCTION);
        System.out.println("🔹 Elliptic Curve Group (GE): " + ELLIPTIC_CURVE_GROUP);
        System.out.println("🔹 Prime p: " + PRIME_P);
        System.out.println("🔹 Prime q: " + PRIME_Q);
        System.out.println("🔹 Base Point (B): " + BASE_POINT_B);
        System.out.println("🔹 Public Key: " + Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        System.out.println("🔷 **Initialization Phase Complete!** 🔷\n");
    }

    public static void main(String[] args) {
        try {
            ECCKeyGen eccKeyGen = new ECCKeyGen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
