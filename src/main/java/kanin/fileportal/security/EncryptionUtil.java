package kanin.fileportal.security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

public class EncryptionUtil {

    // ✅ Exactly 16 bytes for AES-128
    private static final String SECRET_KEY = "SwiftShareAES128";

    private static SecretKeySpec getKey() {
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static byte[] encrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, getKey());
        return cipher.doFinal(data);
    }

    public static byte[] decrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, getKey());
        return cipher.doFinal(data);
    }

    public static String encryptText(String input) throws Exception {
        return Base64.getEncoder().encodeToString(encrypt(input.getBytes(StandardCharsets.UTF_8)));
    }

    public static String decryptText(String input) throws Exception {
        return new String(decrypt(Base64.getDecoder().decode(input)), StandardCharsets.UTF_8);
    }
}
