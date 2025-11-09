package kanin.fileportal.security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

public class EncryptionUtil {

    // ✅ Exactly 16 bytes for AES-128
    // This is the secret encryption key used for both encryption and decryption.
    // AES-128 requires a key size of exactly 16 bytes (128 bits).
    private static final String SECRET_KEY = "SwiftShareAES128";

    // This method converts the secret key string into a byte array and then
    // wraps it inside a SecretKeySpec object, which is used by the Cipher class.
    private static SecretKeySpec getKey() {
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "AES");
    }

    // Encrypts a byte array using AES algorithm with ECB mode and PKCS5 padding.
    // It initializes the cipher in ENCRYPT_MODE using the secret key.
    // Returns the encrypted byte array (ciphertext).
    public static byte[] encrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, getKey());
        return cipher.doFinal(data);
    }

    // Decrypts a previously encrypted byte array using the same key and AES configuration.
    // It initializes the cipher in DECRYPT_MODE and returns the original (decrypted) data.
    public static byte[] decrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, getKey());
        return cipher.doFinal(data);
    }

    // Encrypts a plain text string and encodes the result into Base64 format.
    // This makes it easier to store or transmit the encrypted data as a readable string.
    public static String encryptText(String input) throws Exception {
        return Base64.getEncoder().encodeToString(encrypt(input.getBytes(StandardCharsets.UTF_8)));
    }

    // Takes a Base64 encoded encrypted string, decodes it, decrypts it back to bytes,
    // and finally converts it into a readable UTF-8 text string.
    public static String decryptText(String input) throws Exception {
        return new String(decrypt(Base64.getDecoder().decode(input)), StandardCharsets.UTF_8);
    }
}
