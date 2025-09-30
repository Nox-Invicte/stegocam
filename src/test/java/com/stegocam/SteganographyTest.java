package com.stegocam;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.*;

public class SteganographyTest {

    @Test
    public void testEmbedAndExtractText() {
        BufferedImage coverImage = createGradientImage(64, 64);
        String secretText = "Test Secret";

        BufferedImage embeddedImage = Steganography.embedText(coverImage, secretText);
        assertNotNull(embeddedImage);

        String extractedText = Steganography.extractText(embeddedImage, secretText.length());
        assertEquals(secretText, extractedText);
    }

    @Test
    public void testEmbedAndExtractImage() throws IOException {
        BufferedImage coverImage = createGradientImage(128, 128);
        BufferedImage secretImage = createCheckerboardImage(32, 32);

        BufferedImage embeddedImage = Steganography.embedImage(coverImage, secretImage);
        assertNotNull(embeddedImage);

        BufferedImage extractedImage = Steganography.extractImage(embeddedImage, secretImage.getWidth(), secretImage.getHeight());
        assertNotNull(extractedImage);
        assertEquals(secretImage.getWidth(), extractedImage.getWidth());
        assertEquals(secretImage.getHeight(), extractedImage.getHeight());
        assertTrue(imagesEqual(secretImage, extractedImage));
    }

    private BufferedImage createGradientImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int red = (x * 255) / Math.max(1, width - 1);
                int green = (y * 255) / Math.max(1, height - 1);
                int blue = ((x + y) * 255) / Math.max(1, width + height - 2);
                image.setRGB(x, y, new Color(red, green, blue).getRGB());
            }
        }
        return image;
    }

    private BufferedImage createCheckerboardImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean even = ((x / 4) + (y / 4)) % 2 == 0;
                image.setRGB(x, y, even ? Color.WHITE.getRGB() : Color.BLACK.getRGB());
            }
        }
        return image;
    }

    private boolean imagesEqual(BufferedImage first, BufferedImage second) throws IOException {
        byte[] firstBytes = toPngBytes(first);
        byte[] secondBytes = toPngBytes(second);
        return java.util.Arrays.equals(firstBytes, secondBytes);
    }

    private byte[] toPngBytes(BufferedImage image) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", outputStream);
            return outputStream.toByteArray();
        }
    }
}
