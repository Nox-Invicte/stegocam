package com.stegocam.io;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

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
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            String format = getFormatFromExtension(filePath);
            if (format == null) {
                format = "png";
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
        String extension = extractExtension(filePath);
        if (extension == null) {
            return null;
        }

        String normalized = switch (extension) {
            case "jpeg" -> "jpg";
            case "tif" -> "tiff";
            default -> extension;
        };

        if (!ImageIO.getImageWritersBySuffix(normalized).hasNext()) {
            return null;
        }

        return normalized;
    }

    private String extractExtension(String filePath) {
        if (filePath == null) {
            return null;
        }
        int dotIndex = filePath.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filePath.length() - 1) {
            return null;
        }
        return filePath.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
    
    /**
     * Check if file is a supported image format
     */
    public boolean isSupportedFormat(String filePath) {
        String extension = extractExtension(filePath);
        if (extension == null) {
            return false;
        }
        return ImageIO.getImageReadersBySuffix(extension).hasNext();
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
        if (original == null) {
            throw new IllegalArgumentException("Original image cannot be null");
        }
        int imageType = original.getType() == BufferedImage.TYPE_CUSTOM ? BufferedImage.TYPE_INT_ARGB : original.getType();
        BufferedImage copy = new BufferedImage(original.getWidth(), original.getHeight(), imageType);
        Graphics2D graphics = copy.createGraphics();
        try {
            graphics.drawImage(original, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return copy;
    }
}
