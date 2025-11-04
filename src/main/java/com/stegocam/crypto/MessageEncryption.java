package com.stegocam.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Message encryption/decryption utility using AES-256.
 * 
 * Note: While the historic Enigma cipher is fascinating, it's cryptographically weak by modern standards
 * and was famously broken during WWII. This implementation uses AES (Advanced Encryption Standard),
 * which provides military-grade security for your hidden messages.
 */
public class MessageEncryption {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int KEY_LENGTH = 256;
    private static final int ITERATION_COUNT = 65536;
    private static final int IV_LENGTH = 16;

    /**
     * Encrypts a message using AES-256 encryption.
     * 
     * @param message The plaintext message to encrypt
     * @param password The encryption key/password
     * @return Base64-encoded encrypted message with IV prepended, or null on error
     */
    public static String encrypt(String message, String password) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Encryption password cannot be empty");
        }

        try {
            // Generate random IV
            byte[] iv = new byte[IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // Derive key from password
            SecretKey key = deriveKey(password, iv);

            // Encrypt
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            byte[] encrypted = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));

            // Prepend IV to encrypted data
            byte[] combined = new byte[IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
            System.arraycopy(encrypted, 0, combined, IV_LENGTH, encrypted.length);

            // Encode to Base64 for safe text storage
            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            throw new RuntimeException("Encryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypts a message that was encrypted with the encrypt method.
     * 
     * @param encryptedMessage Base64-encoded encrypted message with IV prepended
     * @param password The decryption key/password (must match encryption password)
     * @return Decrypted plaintext message, or null on error
     */
    public static String decrypt(String encryptedMessage, String password) {
        if (encryptedMessage == null || encryptedMessage.isEmpty()) {
            return encryptedMessage;
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Decryption password cannot be empty");
        }

        try {
            // Decode from Base64
            byte[] combined = Base64.getDecoder().decode(encryptedMessage);

            if (combined.length < IV_LENGTH) {
                throw new IllegalArgumentException("Invalid encrypted message format");
            }

            // Extract IV and encrypted data
            byte[] iv = new byte[IV_LENGTH];
            byte[] encrypted = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            System.arraycopy(combined, IV_LENGTH, encrypted, 0, encrypted.length);

            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // Derive key from password
            SecretKey key = deriveKey(password, iv);

            // Decrypt
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Decryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Derives a cryptographic key from a password using PBKDF2.
     */
    private static SecretKey deriveKey(String password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), KEY_ALGORITHM);
    }

    /**
     * Checks if a message appears to be encrypted (Base64 format check).
     */
    public static boolean isEncrypted(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(message);
            return decoded.length >= IV_LENGTH;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
