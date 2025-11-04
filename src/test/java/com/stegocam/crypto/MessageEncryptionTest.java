package com.stegocam.crypto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MessageEncryption class
 */
public class MessageEncryptionTest {

    @Test
    public void testBasicEncryptionDecryption() {
        String message = "This is a secret message!";
        String password = "mySecretKey123";
        
        String encrypted = MessageEncryption.encrypt(message, password);
        assertNotNull(encrypted);
        assertNotEquals(message, encrypted);
        
        String decrypted = MessageEncryption.decrypt(encrypted, password);
        assertEquals(message, decrypted);
    }

    @Test
    public void testEmptyMessage() {
        String message = "";
        String password = "password";
        
        String encrypted = MessageEncryption.encrypt(message, password);
        assertEquals("", encrypted);
        
        String decrypted = MessageEncryption.decrypt(encrypted, password);
        assertEquals("", decrypted);
    }

    @Test
    public void testNullMessage() {
        String password = "password";
        String encrypted = MessageEncryption.encrypt(null, password);
        assertNull(encrypted);
    }

    @Test
    public void testEmptyPassword() {
        String message = "Secret message";
        assertThrows(IllegalArgumentException.class, () -> {
            MessageEncryption.encrypt(message, "");
        });
    }

    @Test
    public void testNullPassword() {
        String message = "Secret message";
        assertThrows(IllegalArgumentException.class, () -> {
            MessageEncryption.encrypt(message, null);
        });
    }

    @Test
    public void testWrongPassword() {
        String message = "Secret message";
        String correctPassword = "correct";
        String wrongPassword = "wrong";
        
        String encrypted = MessageEncryption.encrypt(message, correctPassword);
        
        assertThrows(RuntimeException.class, () -> {
            MessageEncryption.decrypt(encrypted, wrongPassword);
        });
    }

    @Test
    public void testLongMessage() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("This is a very long message that spans multiple lines. ");
        }
        String message = sb.toString();
        String password = "longMessagePassword";
        
        String encrypted = MessageEncryption.encrypt(message, password);
        String decrypted = MessageEncryption.decrypt(encrypted, password);
        
        assertEquals(message, decrypted);
    }

    @Test
    public void testSpecialCharacters() {
        String message = "Special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?`~\nNew line\tTab\r\nWindows newline";
        String password = "specialPassword!@#";
        
        String encrypted = MessageEncryption.encrypt(message, password);
        String decrypted = MessageEncryption.decrypt(encrypted, password);
        
        assertEquals(message, decrypted);
    }

    @Test
    public void testUnicodeCharacters() {
        String message = "Unicode test: 你好世界 🌍 🔐 Привет мир مرحبا بالعالم";
        String password = "unicodePassword";
        
        String encrypted = MessageEncryption.encrypt(message, password);
        String decrypted = MessageEncryption.decrypt(encrypted, password);
        
        assertEquals(message, decrypted);
    }

    @Test
    public void testIsEncrypted() {
        String message = "Plain message";
        String password = "password";
        
        assertFalse(MessageEncryption.isEncrypted(message));
        
        String encrypted = MessageEncryption.encrypt(message, password);
        assertTrue(MessageEncryption.isEncrypted(encrypted));
    }

    @Test
    public void testIsEncryptedWithInvalidBase64() {
        assertFalse(MessageEncryption.isEncrypted("This is not base64!@#"));
        assertFalse(MessageEncryption.isEncrypted(""));
        assertFalse(MessageEncryption.isEncrypted(null));
    }

    @Test
    public void testDifferentPasswordsProduceDifferentCiphertext() {
        String message = "Same message";
        String password1 = "password1";
        String password2 = "password2";
        
        String encrypted1 = MessageEncryption.encrypt(message, password1);
        String encrypted2 = MessageEncryption.encrypt(message, password2);
        
        assertNotEquals(encrypted1, encrypted2);
    }

    @Test
    public void testSameMessageEncryptedTwiceProducesDifferentCiphertext() {
        // Due to random IV, same message encrypted twice should produce different ciphertext
        String message = "Same message";
        String password = "password";
        
        String encrypted1 = MessageEncryption.encrypt(message, password);
        String encrypted2 = MessageEncryption.encrypt(message, password);
        
        assertNotEquals(encrypted1, encrypted2);
        
        // But both should decrypt to the same plaintext
        assertEquals(message, MessageEncryption.decrypt(encrypted1, password));
        assertEquals(message, MessageEncryption.decrypt(encrypted2, password));
    }
}
