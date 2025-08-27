package com.stegocam.stego;

import java.awt.image.BufferedImage;

/**
 * LSB (The Least Significant Bit) steganography engine
 * Handles embedding and extraction of messages in images
 */
public class StegoEngine {
    
    private static final int LSB_BITS = 2; // Number of LSB bits to use per color channel
    
    /**
     * Embed a message into an image using LSB steganography
     */
    public BufferedImage embedMessage(BufferedImage image, byte[] message) {
        System.out.println("Embedding message: " + new String(message));
        int width = image.getWidth();
        int height = image.getHeight();
        
        // Create a copy of the image to modify
        BufferedImage stegoImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        // Convert message to bit array
        boolean[] messageBits = bytesToBits(message);
        int messageLength = messageBits.length;
        
        // Store message length in the first 32 bits (4 bytes) of the image
        boolean[] lengthBits = intToBits(messageLength);
        
        int bitIndex = 0;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                
                // Extract RGB components
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                
                // First, embed message length in the first 32 bits
                if (bitIndex < 32) {
                    red = embedBits(red, lengthBits, bitIndex);
                    bitIndex += LSB_BITS;
                    if (bitIndex < 32) {
                        green = embedBits(green, lengthBits, bitIndex);
                        bitIndex += LSB_BITS;
                    }
                    if (bitIndex < 32) {
                        blue = embedBits(blue, lengthBits, bitIndex);
                        bitIndex += LSB_BITS;
                    }
                } else {
                    // Then embed the actual message bits
                    int messageBitIndex = bitIndex - 32;
                    if (messageBitIndex < messageLength) {
                        red = embedBits(red, messageBits, messageBitIndex);
                        messageBitIndex += LSB_BITS;
                    }
                    if (messageBitIndex < messageLength) {
                        green = embedBits(green, messageBits, messageBitIndex);
                        messageBitIndex += LSB_BITS;
                    }
                    if (messageBitIndex < messageLength) {
                        blue = embedBits(blue, messageBits, messageBitIndex);
                        messageBitIndex += LSB_BITS;
                    }
                    bitIndex += (messageBitIndex - (bitIndex - 32));
                }
                
                // Reconstruct RGB value
                int newRgb = (red << 16) | (green << 8) | blue;
                stegoImage.setRGB(x, y, newRgb);
            }
        }
        
        return stegoImage;
    }
    
    /**
     * Extract a message from a stego image
     */
    public byte[] extractMessage(BufferedImage stegoImage) {
        System.out.println("Extracting message from stego image...");
        int width = stegoImage.getWidth();
        int height = stegoImage.getHeight();
        
        // First, extract the message length from the first 32 bits
        boolean[] lengthBits = new boolean[32];
        int bitIndex = 0;
        
        // Extract length from first pixels
        outer:
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (bitIndex >= 32) break outer;
                
                int rgb = stegoImage.getRGB(x, y);
                
                // Extract RGB components
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                
                // Extract bits from each color channel for length
                if (bitIndex < 32) {
                    extractBits(red, lengthBits, bitIndex);
                    bitIndex += LSB_BITS;
                }
                if (bitIndex < 32) {
                    extractBits(green, lengthBits, bitIndex);
                    bitIndex += LSB_BITS;
                }
                if (bitIndex < 32) {
                    extractBits(blue, lengthBits, bitIndex);
                    bitIndex += LSB_BITS;
                }
            }
        }
        
        // Convert length bits to integer
        int messageLengthBits = bitsToInt(lengthBits);
        System.out.println("Extracted message length in bits: " + messageLengthBits);
        System.out.println("Length bits: " + java.util.Arrays.toString(lengthBits));
        
        // Calculate maximum possible message size
        int maxBits = width * height * 3 * LSB_BITS;
        boolean[] messageBits = new boolean[messageLengthBits];
        bitIndex = 0;
        
        // Reset bit index and extract the actual message
        int pixelIndex = 0;
        outer2:
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (bitIndex >= messageLengthBits) break outer2;
                
                int rgb = stegoImage.getRGB(x, y);
                
                // Extract RGB components
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                
                // Skip the first 32 bits (already used for length)
                if (pixelIndex < 32 / LSB_BITS) {
                    pixelIndex++;
                    continue;
                }
                
                // Extract message bits from each color channel
                if (bitIndex < messageLengthBits) {
                    extractBits(red, messageBits, bitIndex);
                    bitIndex += LSB_BITS;
                }
                if (bitIndex < messageLengthBits) {
                    extractBits(green, messageBits, bitIndex);
                    bitIndex += LSB_BITS;
                }
                if (bitIndex < messageLengthBits) {
                    extractBits(blue, messageBits, bitIndex);
                    bitIndex += LSB_BITS;
                }
            }
        }
        
        // Convert bits back to bytes
        byte[] extractedBytes = bitsToBytes(messageBits);
        System.out.println("Extracted message: '" + new String(extractedBytes) + "' (length: " + extractedBytes.length + ")");
        return extractedBytes;
    }
    
    /**
     * Check if an image can hold a message of given size
     */
    public boolean canHoldMessage(BufferedImage image, int messageSizeBytes) {
        int width = image.getWidth();
        int height = image.getHeight();
        int maxBits = width * height * 3 * LSB_BITS;
        int requiredBits = messageSizeBytes * 8;
        
        return maxBits >= requiredBits;
    }
    
    /**
     * Embed bits into a color value
     * Package-private for testing
     */
    public int embedBits(int colorValue, boolean[] bits, int startIndex) {
        System.out.println("Embedding bits into color value: " + colorValue);
        int mask = (1 << LSB_BITS) - 1;
        int clearedValue = colorValue & ~mask;
        
        int embeddedBits = 0;
        for (int i = 0; i < LSB_BITS; i++) {
            if (startIndex + i < bits.length && bits[startIndex + i]) {
                embeddedBits |= (1 << (LSB_BITS - 1 - i));
            }
        }
        
        int result = clearedValue | embeddedBits;
        System.out.println("Result after embedding: " + result + " (binary: " + Integer.toBinaryString(result) + ")");
        return result;
    }
    
    /**
     * Extract bits from a color value
     * Package-private for testing
     */
    public void extractBits(int colorValue, boolean[] bits, int startIndex) {
        System.out.println("Extracting bits from color value: " + colorValue);
        int mask = (1 << LSB_BITS) - 1;
        int extractedBits = colorValue & mask;
        
        for (int i = 0; i < LSB_BITS; i++) {
            if (startIndex + i < bits.length) {
                bits[startIndex + i] = ((extractedBits >> (LSB_BITS - 1 - i)) & 1) == 1;
            }
        }
        System.out.println("Extracted bits: " + extractedBits);
    }
    
    /**
     * Convert integer to bit array (32 bits)
     * Package-private for testing
     */
    public boolean[] intToBits(int value) {
        boolean[] bits = new boolean[32];
        
        for (int i = 0; i < 32; i++) {
            bits[31 - i] = ((value >> i) & 1) == 1;
        }
        
        return bits;
    }
    
    /**
     * Convert bit array to integer (32 bits)
     * Package-private for testing
     */
    public int bitsToInt(boolean[] bits) {
        if (bits.length != 32) {
            throw new IllegalArgumentException("Bit array must be exactly 32 bits long");
        }
        
        int value = 0;
        for (int i = 0; i < 32; i++) {
            if (bits[i]) {
                value |= (1 << (31 - i));
            }
        }
        
        return value;
    }
    
    /**
     * Convert byte array to bit array
     * Package-private for testing
     */
    public boolean[] bytesToBits(byte[] bytes) {
        boolean[] bits = new boolean[bytes.length * 8];
        
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            for (int j = 0; j < 8; j++) {
                bits[i * 8 + j] = ((b >> (7 - j)) & 1) == 1;
            }
        }
        
        return bits;
    }
    
    /**
     * Find the actual length of the message by looking for trailing zeros
     * Improved version that handles encrypted data better
     */
    private int findActualMessageLength(boolean[] bits) {
        // For encrypted data, we need to be more careful about end detection
        // Look for the end of the message by checking for consistent trailing zeros
        // but don't stop at the first occurrence of 8 zeros
        
        // Start from the end and work backwards to find where meaningful data ends
        int lastNonZeroBit = -1;
        for (int i = bits.length - 1; i >= 0; i--) {
            if (bits[i]) {
                lastNonZeroBit = i;
                break;
            }
        }
        
        // If no non-zero bits found, message is empty
        if (lastNonZeroBit == -1) {
            return 0;
        }
        
        // Return length up to the last non-zero bit + 1 (to include it)
        return lastNonZeroBit + 1;
    }
    
    /**
     * Convert bit array to byte array
     * Package-private for testing
     */
    public byte[] bitsToBytes(boolean[] bits) {
        int byteCount = (bits.length + 7) / 8;
        byte[] bytes = new byte[byteCount];
        
        for (int i = 0; i < byteCount; i++) {
            byte b = 0;
            for (int j = 0; j < 8; j++) {
                int bitIndex = i * 8 + j;
                if (bitIndex < bits.length && bits[bitIndex]) {
                    b |= (byte) (1 << (7 - j));
                }
            }
            bytes[i] = b;
        }
        
        return bytes;
    }
}
