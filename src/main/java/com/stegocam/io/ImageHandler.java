package com.stegocam.io;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Handles image input/output operations
 */
public class ImageHandler {
    
    /**
     * Load an image from file
     */
    public BufferedImage loadImage(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println("File does not exist: " + filePath);
                return null;
            }
            return ImageIO.read(file);
        } catch (IOException e) {
            System.err.println("Error loading image: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Save an image to file
     */
    public boolean saveImage(BufferedImage image, String filePath) {
        try {
            File file = new File(filePath);
            
            // Determine format from file extension
            String format = getFormatFromExtension(filePath);
            if (format == null) {
                format = "png"; // default format
            }
            
            return ImageIO.write(image, format, file);
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get image format from file extension
     */
    private String getFormatFromExtension(String filePath) {
        if (filePath.toLowerCase().endsWith(".jpg") || filePath.toLowerCase().endsWith(".jpeg")) {
            return "jpg";
        } else if (filePath.toLowerCase().endsWith(".png")) {
            return "png";
        } else if (filePath.toLowerCase().endsWith(".bmp")) {
            return "bmp";
        } else if (filePath.toLowerCase().endsWith(".gif")) {
            return "gif";
        }
        return null;
    }
    
    /**
     * Check if file is a supported image format
     */
    public boolean isSupportedFormat(String filePath) {
        String format = getFormatFromExtension(filePath);
        return format != null;
    }
    
    /**
     * Get image dimensions
     */
    public String getImageDimensions(String filePath) {
        BufferedImage image = loadImage(filePath);
        if (image == null) {
            return "Unable to load image";
        }
        return image.getWidth() + "x" + image.getHeight();
    }
    
    /**
     * Create a copy of an image
     */
    public BufferedImage copyImage(BufferedImage original) {
        BufferedImage copy = new BufferedImage(
            original.getWidth(),
            original.getHeight(),
            original.getType()
        );
        copy.getGraphics().drawImage(original, 0, 0, null);
        return copy;
    }
}
