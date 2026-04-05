package sn.symmetry.cadoobi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service for encrypting and decrypting sensitive data using AES-GCM encryption.
 * Used for encrypting API keys and webhook secrets before storing in database.
 */
@Service
@Slf4j
public class EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 128; // 128 bits

    private final SecretKey secretKey;
    private final SecureRandom secureRandom;
    private static final int AES_256_KEY_SIZE = 32; // 256 bits / 8

    public EncryptionService(@Value("${app.security.encryption.key}") String encryptionKey) {
        // Decode the base64-encoded encryption key
        byte[] decodedKey;
        try {
            decodedKey = Base64.getDecoder().decode(encryptionKey);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid encryption key format. Key must be base64-encoded. Generate with: openssl rand -base64 32", e);
        }

        // Validate key size (must be 16, 24, or 32 bytes for AES)
        if (decodedKey.length != 16 && decodedKey.length != 24 && decodedKey.length != 32) {
            throw new IllegalArgumentException(
                String.format("Invalid AES key length: %d bytes. Must be 16 (AES-128), 24 (AES-192), or 32 (AES-256) bytes. " +
                    "Generate a valid key with: openssl rand -base64 32", decodedKey.length));
        }

        if (decodedKey.length != AES_256_KEY_SIZE) {
            log.warn("Using AES-{} encryption. For maximum security, use AES-256 (32 bytes). Generate with: openssl rand -base64 32",
                decodedKey.length * 8);
        }

        this.secretKey = new SecretKeySpec(decodedKey, "AES");
        this.secureRandom = new SecureRandom();
        log.info("EncryptionService initialized with AES-{} encryption", decodedKey.length * 8);
    }

    /**
     * Encrypts a plaintext string using AES-GCM.
     * Returns a base64-encoded string containing the IV and ciphertext.
     */
    public String encrypt(String plaintext) {
        try {
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            // Encrypt
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes());

            // Combine IV and ciphertext
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertext);

            // Encode to base64
            return Base64.getEncoder().encodeToString(byteBuffer.array());

        } catch (Exception e) {
            log.error("Error encrypting data", e);
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }

    /**
     * Decrypts a base64-encoded encrypted string using AES-GCM.
     * The encrypted string should contain both the IV and ciphertext.
     */
    public String decrypt(String encryptedData) {
        try {
            // Decode from base64
            byte[] decoded = Base64.getDecoder().decode(encryptedData);

            // Extract IV and ciphertext
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] ciphertext = new byte[byteBuffer.remaining()];
            byteBuffer.get(ciphertext);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            // Decrypt
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext);

        } catch (Exception e) {
            log.error("Error decrypting data", e);
            throw new RuntimeException("Failed to decrypt data", e);
        }
    }

    /**
     * Validates that an encrypted value matches the expected plaintext.
     * Useful for API key validation without exposing the plaintext.
     */
    public boolean matches(String plaintext, String encryptedData) {
        try {
            String decrypted = decrypt(encryptedData);
            return decrypted.equals(plaintext);
        } catch (Exception e) {
            log.error("Error validating encrypted data", e);
            return false;
        }
    }
}
