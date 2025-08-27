package com.stegocam.controller;

import com.stegocam.crypto.CryptoEngine;
import com.stegocam.crypto.KeyGenerator;
import com.stegocam.stego.StegoEngine;
import com.stegocam.io.ImageHandler;

import java.awt.image.BufferedImage;
import java.security.Key;

/**
 * Controller class that handles encryption/decryption calls
 * Coordinates between crypto, stego, and IO components
 */
public class StegoController {
    
    private final CryptoEngine cryptoEngine;
    private final StegoEngine stegoEngine;
    private final ImageHandler imageHandler;
    private final KeyGenerator keyGenerator;
    
    public StegoController() {
        this.cryptoEngine = new CryptoEngine();
        this.stegoEngine = new StegoEngine();
        this.imageHandler = new ImageHandler();
        this.keyGenerator = new KeyGenerator();
    }
    
    /**
     * Encrypt and embed a message into an image
     */
    public boolean encryptAndEmbed(String inputImagePath, String outputImagePath, String message, String password) {
        try {
            // Load the image
            BufferedImage image = imageHandler.loadImage(inputImagePath);
            if (image == null) {
                return false;
            }
            
            // Generate encryption key from password
            Key key = keyGenerator.generateKeyFromPassword(password);
            
            // Encrypt the message
            byte[] encryptedMessage = cryptoEngine.encrypt(message.getBytes(), key);
            
            // Embed the encrypted message into the image
            BufferedImage stegoImage = stegoEngine.embedMessage(image, encryptedMessage);
            
            // Save the stego image
            return imageHandler.saveImage(stegoImage, outputImagePath);
            
        } catch (Exception e) {
            System.err.println("Error during encryption and embedding: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Extract and decrypt a message from an image
     */
    public String extractAndDecrypt(String stegoImagePath, String password) {
        try {
            // Load the stego image
            BufferedImage stegoImage = imageHandler.loadImage(stegoImagePath);
            if (stegoImage == null) {
                return null;
            }
            
            // Generate encryption key from password
            Key key = keyGenerator.generateKeyFromPassword(password);
            
            // Extract the encrypted message
            byte[] encryptedMessage = stegoEngine.extractMessage(stegoImage);
            
            // Decrypt the message
            byte[] decryptedBytes = cryptoEngine.decrypt(encryptedMessage, key);
            
            return new String(decryptedBytes);
            
        } catch (Exception e) {
            System.err.println("Error during extraction and decryption: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Check if an image can hold a message of given size
     */
    public boolean canHoldMessage(String imagePath, int messageSize) {
        try {
            BufferedImage image = imageHandler.loadImage(imagePath);
            if (image == null) {
                return false;
            }
            return stegoEngine.canHoldMessage(image, messageSize);
        } catch (Exception e) {
            return false;
        }
    }
}
