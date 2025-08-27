package com.stegocam;

import com.stegocam.crypto.CryptoEngine;
import com.stegocam.crypto.KeyGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.security.Key;

/**
 * Unit tests for crypto components
 */
public class CryptoTests {
    
    private CryptoEngine cryptoEngine;
    private KeyGenerator keyGenerator;
    
    @BeforeEach
    void setUp() {
        cryptoEngine = new CryptoEngine();
        keyGenerator = new KeyGenerator();
    }
    
    @Test
    void testKeyGenerationFromPassword() {
        String password = "testPassword123";
        Key key = keyGenerator.generateKeyFromPassword(password);
        
        assertNotNull(key, "Key should not be null");
        assertEquals("AES", key.getAlgorithm(), "Key algorithm should be AES");
    }
    
    @Test
    void testEncryptionDecryption() {
        try {
            String originalMessage = "This is a secret message for testing!";
            String password = "securePassword";
            
            Key key = keyGenerator.generateKeyFromPassword(password);
            byte[] encrypted = cryptoEngine.encrypt(originalMessage.getBytes(), key);
            byte[] decrypted = cryptoEngine.decrypt(encrypted, key);
            
            String decryptedMessage = new String(decrypted);
            
            assertEquals(originalMessage, decryptedMessage, 
                        "Decrypted message should match original");
            assertNotEquals(originalMessage, new String(encrypted),
                        "Encrypted data should not match original");
            
        } catch (Exception e) {
            fail("Encryption/decryption should not throw exception: " + e.getMessage());
        }
    }
    
    @Test
    void testEmptyMessageEncryption() {
        try {
            String originalMessage = "";
            String password = "test";
            
            Key key = keyGenerator.generateKeyFromPassword(password);
            byte[] encrypted = cryptoEngine.encrypt(originalMessage.getBytes(), key);
            byte[] decrypted = cryptoEngine.decrypt(encrypted, key);
            
            String decryptedMessage = new String(decrypted);
            
            assertEquals(originalMessage, decryptedMessage,
                        "Empty message should be preserved");
            
        } catch (Exception e) {
            fail("Empty message encryption should not throw exception: " + e.getMessage());
        }
    }
    
    @Test
    void testDifferentPasswords() {
        try {
            String message = "Test message";
            
            Key key1 = keyGenerator.generateKeyFromPassword("password1");
            Key key2 = keyGenerator.generateKeyFromPassword("password2");
            
            byte[] encrypted = cryptoEngine.encrypt(message.getBytes(), key1);
            
            // Should fail to decrypt with different key
            assertThrows(Exception.class, () -> cryptoEngine.decrypt(encrypted, key2), "Decryption with wrong key should fail");
            
        } catch (Exception e) {
            fail("Encryption should not throw exception: " + e.getMessage());
        }
    }
    
    @Test
    void testRandomKeyGeneration() {
        try {
            Key key = keyGenerator.generateRandomKey();
            
            assertNotNull(key, "Random key should not be null");
            assertEquals("AES", key.getAlgorithm(), "Key algorithm should be AES");
            assertEquals(32, key.getEncoded().length, "Key should be 256 bits (32 bytes)");
            
        } catch (Exception e) {
            fail("Random key generation should not throw exception: " + e.getMessage());
        }
    }
    
    @Test
    void testKeyFromRGB() {
        try {
            Key key = keyGenerator.generateKeyFromRGB(255, 128, 64);
            
            assertNotNull(key, "RGB key should not be null");
            assertEquals("AES", key.getAlgorithm(), "Key algorithm should be AES");
            
        } catch (Exception e) {
            fail("RGB key generation should not throw exception: " + e.getMessage());
        }
    }
}
