package kanin.fileportal.security;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;

/**
 * EncryptionUtil
 *
 * Provides authenticated AES-GCM (Galois/Counter Mode) encryption for SwiftShare file transfers.
 *
 * Cryptographic Highlights:
 * - Cipher Transformation: AES/GCM/NoPadding
 * - Key Size: 128-bit (16 bytes) generated dynamically per transfer via SecureRandom
 * - Nonce/IV Size: 96-bit (12 bytes) recommended standard for GCM
 * - Authentication Tag: 128-bit (16 bytes) appended at the end of the ciphertext stream
 *
 * In AES-GCM:
 * - Confidentiality is achieved via CTR mode encryption.
 * - Integrity and Authenticity are verified via the GHASH authentication tag.
 * - If any ciphertext bytes are modified during transmission, CipherInputStream throws
 *   an AEADBadTagException at stream completion, signaling data tampering.
 */
public class EncryptionUtil {

    // ==================================================
    // Cryptographic Constants
    // ==================================================
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_SIZE_BITS = 128;         // 16 bytes
    private static final int GCM_TAG_LENGTH_BITS = 128;   // 16 bytes authentication tag
    public static final int IV_SIZE_BYTES = 12;           // 96 bits standard for GCM

    private static final SecureRandom secureRandom = new SecureRandom();

    // ==================================================
    // Key and IV Generation
    // ==================================================

    /**
     * Generates a new random 128-bit AES secret key using cryptographically secure PRNG.
     *
     * @return a freshly generated SecretKey
     */
    public static SecretKey generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(KEY_SIZE_BITS, secureRandom);
            return keyGen.generateKey();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate AES secret key: " + e.getMessage(), e);
        }
    }

    /**
     * Reconstitutes a SecretKey instance from raw key bytes received over the network.
     *
     * @param keyBytes the 16-byte raw key material
     * @return SecretKeySpec compatible with AES ciphers
     */
    public static SecretKey getKeyFromBytes(byte[] keyBytes) {
        if (keyBytes == null || keyBytes.length != KEY_SIZE_BITS / 8) {
            throw new IllegalArgumentException("Invalid AES key length: expected 16 bytes");
        }
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /**
     * Generates a random 12-byte (96-bit) Initialization Vector (IV/Nonce) for GCM.
     *
     * @return 12-byte random IV
     */
    public static byte[] generateIv() {
        byte[] iv = new byte[IV_SIZE_BYTES];
        secureRandom.nextBytes(iv);
        return iv;
    }

    // ==================================================
    // Streaming Cipher Constructors
    // ==================================================

    /**
     * Wraps an OutputStream with a CipherOutputStream initialized in AES-GCM ENCRYPT_MODE.
     * When closed, the CipherOutputStream automatically calculates and flushes the 128-bit
     * GCM authentication tag.
     *
     * @param out the underlying raw network output stream
     * @param key the 128-bit AES secret key
     * @param iv  the 12-byte initialization vector
     * @return CipherOutputStream encrypting data on the fly
     * @throws Exception if cipher initialization fails
     */
    public static CipherOutputStream createCipherOutputStream(OutputStream out, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
        return new CipherOutputStream(out, cipher);
    }

    /**
     * Wraps an InputStream with a CipherInputStream initialized in AES-GCM DECRYPT_MODE.
     * When reaching EOF, it validates the trailing 128-bit authentication tag. If the tag
     * does not match, an AEADBadTagException is thrown.
     *
     * @param in  the underlying raw network input stream
     * @param key the 128-bit AES secret key
     * @param iv  the 12-byte initialization vector
     * @return CipherInputStream decrypting data on the fly
     * @throws Exception if cipher initialization fails
     */
    public static CipherInputStream createCipherInputStream(InputStream in, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = createDecryptCipher(key, iv);
        return new CipherInputStream(in, cipher);
    }

    /**
     * Initializes and returns an explicit Cipher in AES-GCM DECRYPT_MODE.
     * Allows stream receivers to call cipher.update() and cipher.doFinal() directly,
     * ensuring explicit detection of AEADBadTagException without relying on CipherInputStream semantics.
     *
     * @param key the 128-bit AES secret key
     * @param iv  the 12-byte initialization vector
     * @return initialized Cipher in DECRYPT_MODE
     * @throws Exception if cipher initialization fails
     */
    public static Cipher createDecryptCipher(SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        return cipher;
    }
}

