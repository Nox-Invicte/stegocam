package com.stegocam;

import java.awt.image.BufferedImage;
import java.security.Key;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.stegocam.crypto.CryptoEngine;
import com.stegocam.crypto.KeyGenerator;
import com.stegocam.stego.StegoEngine;

/**
 * Unit tests for steganography components
 */
public class StegoTests {
    
    private StegoEngine stegoEngine;
    
    @BeforeEach
    void setUp() {
        stegoEngine = new StegoEngine();
    }
    
    @Test
    void testBitsToBytesConversion() {
        // Test empty array
        boolean[] emptyBits = new boolean[0];
        byte[] emptyBytes = stegoEngine.bitsToBytes(emptyBits);
        assertEquals(0, emptyBytes.length, "Empty bits should produce empty bytes");
        
        // Test single byte
        boolean[] singleByteBits = {true, false, true, false, true, false, true, false}; // 170 = 0xAA
        byte[] singleByte = stegoEngine.bitsToBytes(singleByteBits);
        assertEquals(1, singleByte.length, "8 bits should produce 1 byte");
        assertEquals((byte) 0xAA, singleByte[0], "Bit pattern should match expected byte");
        
        // Test multiple bytes
        boolean[] multiByteBits = new boolean[16];
        for (int i = 0; i < 16; i++) {
            multiByteBits[i] = (i % 2 == 0); // Alternating pattern
        }
        byte[] multiByte = stegoEngine.bitsToBytes(multiByteBits);
        assertEquals(2, multiByte.length, "16 bits should produce 2 bytes");
        assertEquals((byte) 0xAA, multiByte[0], "First byte should match pattern");
        assertEquals((byte) 0xAA, multiByte[1], "Second byte should match pattern");
    }
    
    @Test
    void testBytesToBitsConversion() {
        // Test empty array
        byte[] emptyBytes = new byte[0];
        boolean[] emptyBits = stegoEngine.bytesToBits(emptyBytes);
        assertEquals(0, emptyBits.length, "Empty bytes should produce empty bits");
        
        // Test single byte
        byte singleByte = (byte) 0xAA; // 10101010
        boolean[] singleByteBits = stegoEngine.bytesToBits(new byte[]{singleByte});
        assertEquals(8, singleByteBits.length, "1 byte should produce 8 bits");
        assertArrayEquals(
            new boolean[]{true, false, true, false, true, false, true, false},
            singleByteBits,
            "Bit pattern should match expected"
        );
        
        // Test multiple bytes
        byte[] multiByte = {(byte) 0xAA, (byte) 0x55}; // 10101010 01010101
        boolean[] multiByteBits = stegoEngine.bytesToBits(multiByte);
        assertEquals(16, multiByteBits.length, "2 bytes should produce 16 bits");
        boolean[] expected = {
            true, false, true, false, true, false, true, false,  // 0xAA
            false, true, false, true, false, true, false, true   // 0x55
        };
        assertArrayEquals(expected, multiByteBits, "Bit pattern should match expected");
    }
    
    @Test
    void testEmbedExtractBits() {
        int colorValue = 0b11110000; // 240
        
        // Test embedding bits
        boolean[] bitsToEmbed = {true, false}; // Embed 10 in LSB
        int embedded = stegoEngine.embedBits(colorValue, bitsToEmbed, 0);
        
        // Should clear LSB bits and set to 10
        assertEquals(0b11110010, embedded, "Embedded value should match expected");
        
        // Test extracting bits
        boolean[] extractedBits = new boolean[2];
        stegoEngine.extractBits(embedded, extractedBits, 0);
        assertArrayEquals(bitsToEmbed, extractedBits, "Extracted bits should match embedded");
    }
    
    @Test
    void testMessageCapacityCalculation() {
        // Create a small test image
        BufferedImage testImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        
        // Calculate expected capacity: 10x10 pixels * 3 channels * 2 bits/channel / 8 bits/byte
        int expectedCapacity = (10 * 10 * 3 * 2) / 8; // 75 bytes
        
        boolean canHold = stegoEngine.canHoldMessage(testImage, expectedCapacity);
        assertTrue(canHold, "Image should be able to hold expected capacity");
        
        boolean cannotHold = stegoEngine.canHoldMessage(testImage, expectedCapacity + 1);
        assertFalse(cannotHold, "Image should not be able to hold more than capacity");
    }
    
    @Test
    void testEmptyMessageEmbedExtract() {
        BufferedImage testImage = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        
        // Fill with test pattern
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                int rgb = (x * 64) << 16 | (y * 64) << 8 | ((x + y) * 32);
                testImage.setRGB(x, y, rgb);
            }
        }
        
        // Test with empty message
        byte[] emptyMessage = new byte[0];
        BufferedImage stegoImage = stegoEngine.embedMessage(testImage, emptyMessage);
        byte[] extractedMessage = stegoEngine.extractMessage(stegoImage);
        
        assertEquals(0, extractedMessage.length, "Extracted empty message should be empty");
    }
    
    @Test
    void testSmallMessageEmbedExtract() {
        BufferedImage testImage = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        
        // Fill with gradient
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int rgb = (x * 32) << 16 | (y * 32) << 8 | ((x + y) * 16);
                testImage.setRGB(x, y, rgb);
            }
        }
        
        String testMessage = "Hello Stego!";
        byte[] messageBytes = testMessage.getBytes();
        
        BufferedImage stegoImage = stegoEngine.embedMessage(testImage, messageBytes);
        byte[] extractedBytes = stegoEngine.extractMessage(stegoImage);
        String extractedMessage = new String(extractedBytes);
        
        assertEquals(testMessage, extractedMessage, "Extracted message should match original");
    }

    @Test
    void testEncryptedMessageExtraction() {
        try {
            BufferedImage testImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
            
            // Fill with test pattern
            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    int rgb = (x * 16) << 16 | (y * 16) << 8 | ((x + y) * 8);
                    testImage.setRGB(x, y, rgb);
                }
            }
            
            String originalMessage = "heyo";
            String password = "testPassword";
            
            // Encrypt the message
            CryptoEngine cryptoEngine = new CryptoEngine();
            KeyGenerator keyGenerator = new KeyGenerator();
            Key key = keyGenerator.generateKeyFromPassword(password);
            byte[] encryptedMessage = cryptoEngine.encrypt(originalMessage.getBytes(), key);
            
            // Embed the encrypted message
            BufferedImage stegoImage = stegoEngine.embedMessage(testImage, encryptedMessage);
            
            // Extract and decrypt the message
            byte[] extractedEncryptedMessage = stegoEngine.extractMessage(stegoImage);
            byte[] decryptedMessage = cryptoEngine.decrypt(extractedEncryptedMessage, key);
            String extractedMessage = new String(decryptedMessage);
            
            assertEquals(originalMessage, extractedMessage, "Extracted encrypted message should match original");
        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }
}
