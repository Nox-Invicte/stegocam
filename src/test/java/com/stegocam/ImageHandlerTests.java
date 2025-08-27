package com.stegocam;

import com.stegocam.io.ImageHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Unit tests for ImageHandler component
 */
public class ImageHandlerTests {
    
    private ImageHandler imageHandler;
    private Path tempDir;
    
    @BeforeEach
    void setUp() throws IOException {
        imageHandler = new ImageHandler();
        tempDir = Files.createTempDirectory("stegocam_test");
        tempDir.toFile().deleteOnExit();
    }
    
    @Test
    void testCreateAndSaveImage() {
        // Create a simple test image
        BufferedImage testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        
        // Fill with a gradient
        for (int y = 0; y < 100; y++) {
            for (int x = 0; x < 100; x++) {
                int rgb = (x * 2) << 16 | (y * 2) << 8 | ((x + y));
                testImage.setRGB(x, y, rgb);
            }
        }
        
        // Save the image
        String testFilePath = tempDir.resolve("test_image.png").toString();
        boolean saveResult = imageHandler.saveImage(testImage, testFilePath);
        
        assertTrue(saveResult, "Image should be saved successfully");
        
        // Verify file exists
        File savedFile = new File(testFilePath);
        assertTrue(savedFile.exists(), "Saved file should exist");
        assertTrue(savedFile.length() > 0, "Saved file should have content");
    }
    
    @Test
    void testSupportedFormats() {
        assertTrue(imageHandler.isSupportedFormat("test.png"), "PNG should be supported");
        assertTrue(imageHandler.isSupportedFormat("test.jpg"), "JPG should be supported");
        assertTrue(imageHandler.isSupportedFormat("test.jpeg"), "JPEG should be supported");
        assertTrue(imageHandler.isSupportedFormat("test.bmp"), "BMP should be supported");
        assertTrue(imageHandler.isSupportedFormat("test.gif"), "GIF should be supported");
        
        assertFalse(imageHandler.isSupportedFormat("test.txt"), "TXT should not be supported");
        assertFalse(imageHandler.isSupportedFormat("test.pdf"), "PDF should not be supported");
    }
    
    @Test
    void testImageCopy() {
        BufferedImage original = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        
        // Fill with some data
        for (int y = 0; y < 50; y++) {
            for (int x = 0; x < 50; x++) {
                int rgb = (x * 5) << 16 | (y * 5) << 8 | 128;
                original.setRGB(x, y, rgb);
            }
        }
        
        BufferedImage copy = imageHandler.copyImage(original);
        
        assertNotNull(copy, "Copy should not be null");
        assertEquals(original.getWidth(), copy.getWidth(), "Copy should have same width");
        assertEquals(original.getHeight(), copy.getHeight(), "Copy should have same height");
        assertEquals(original.getType(), copy.getType(), "Copy should have same type");
        
        // Verify pixel data is the same
        for (int y = 0; y < 50; y++) {
            for (int x = 0; x < 50; x++) {
                assertEquals(original.getRGB(x, y), copy.getRGB(x, y), 
                            "Pixel at (" + x + "," + y + ") should match");
            }
        }
    }
    
    @Test
    void testFormatFromExtension() {
        // This tests the private method indirectly through saveImage behavior
        BufferedImage testImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        
        String pngPath = tempDir.resolve("test.png").toString();
        String jpgPath = tempDir.resolve("test.jpg").toString();
        String bmpPath = tempDir.resolve("test.bmp").toString();
        
        assertTrue(imageHandler.saveImage(testImage, pngPath), "PNG save should succeed");
        assertTrue(imageHandler.saveImage(testImage, jpgPath), "JPG save should succeed");
        assertTrue(imageHandler.saveImage(testImage, bmpPath), "BMP save should succeed");
    }
    
    @Test
    void testNonExistentFileLoad() {
        BufferedImage image = imageHandler.loadImage("non_existent_file.png");
        assertNull(image, "Loading non-existent file should return null");
    }
}
