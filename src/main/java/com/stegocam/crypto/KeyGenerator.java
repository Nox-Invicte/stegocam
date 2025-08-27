package com.stegocam.crypto;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.Key;

/**
 * RGB-based key derivation for encryption
 */
public class KeyGenerator {
    
    private static final String ALGORITHM = "AES";
    private static final String PBKDF_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH = 256;
    
    /**
     * Generate encryption key from password using PBKDF2
     */
    public Key generateKeyFromPassword(String password) {
        try {
            // Use a fixed salt for simplicity (in production, use a random salt)
            byte[] salt = "StegoCamSalt123".getBytes();
            
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF_ALGORITHM);
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
            SecretKey tmpKey = factory.generateSecret(spec);
            
            return new SecretKeySpec(tmpKey.getEncoded(), ALGORITHM);
            
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Failed to generate key from password", e);
        }
    }
    
    /**
     * Generate key from RGB values (for steganography-based key derivation)
     */
    public Key generateKeyFromRGB(int red, int green, int blue) {
        try {
            // Combine RGB values to create key material
            byte[] keyMaterial = new byte[32]; // 256 bits
            
            // Simple key derivation from RGB values
            for (int i = 0; i < keyMaterial.length; i++) {
                int index = i % 3;
                if (index == 0) keyMaterial[i] = (byte) red;
                else if (index == 1) keyMaterial[i] = (byte) green;
                else keyMaterial[i] = (byte) blue;
            }
            
            return new SecretKeySpec(keyMaterial, ALGORITHM);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate key from RGB values", e);
        }
    }
    
    /**
     * Generate a random key
     */
    public Key generateRandomKey() {
        try {
            javax.crypto.KeyGenerator keyGen = javax.crypto.KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(KEY_LENGTH);
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate random key", e);
        }
    }
}
