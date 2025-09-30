package com.stegocam;

import com.stegocam.stego.StegoEngine;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * High-level convenience API for working with steganography operations. This class delegates the
 * core bit manipulation work to {@link StegoEngine} while adding higher-level workflows for
 * embedding text strings and full images.
 */
public final class Steganography {

    private static final StegoEngine ENGINE = new StegoEngine();
    private static final String IMAGE_FORMAT = "png";

    private Steganography() {
        // Utility class
    }

    /**
     * Embed a UTF-8 text string into the supplied image, returning a copy of the modified
     * image with the message embedded.
     */
    public static BufferedImage embedText(BufferedImage image, String text) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }
        String safeText = text == null ? "" : text;
        byte[] payload = safeText.getBytes(StandardCharsets.UTF_8);
        return ENGINE.embedMessage(image, payload);
    }

    /**
     * Extract a previously embedded UTF-8 text string from the given image. The {@code length}
     * parameter is optional: if greater than zero it limits the returned string to the requested
     * number of characters, otherwise the entire embedded message is returned.
     */
    public static String extractText(BufferedImage image, int length) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }

        byte[] data = ENGINE.extractMessage(image);
        String message = new String(data, StandardCharsets.UTF_8);
        if (length > 0 && length < message.length()) {
            return message.substring(0, length);
        }
        return message;
    }

    /**
     * Embed a secret image inside a cover image. The secret image is encoded as PNG to
     * preserve fidelity before being embedded. The resulting image is safe to persist using
     * {@link javax.imageio.ImageIO}.
     */
    public static BufferedImage embedImage(BufferedImage coverImage, BufferedImage secretImage) {
        if (coverImage == null || secretImage == null) {
            throw new IllegalArgumentException("Cover and secret images must be provided");
        }

        byte[] payload = serialiseImage(secretImage);
        return ENGINE.embedMessage(coverImage, payload);
    }

    /**
     * Extract a previously hidden image from the provided stego image. The width and height
     * parameters are optional; if supplied (greater than zero) they are validated against the
     * embedded metadata.
     */
    public static BufferedImage extractImage(BufferedImage stegoImage, int expectedWidth, int expectedHeight) {
        if (stegoImage == null) {
            throw new IllegalArgumentException("Stego image cannot be null");
        }

        byte[] payload = ENGINE.extractMessage(stegoImage);
        if (payload.length < Integer.BYTES * 3) {
            throw new IllegalArgumentException("Embedded payload is too small to contain image metadata.");
        }

        ByteBuffer buffer = ByteBuffer.wrap(payload).order(ByteOrder.BIG_ENDIAN);
        int width = buffer.getInt();
        int height = buffer.getInt();
        int dataLength = buffer.getInt();

        if (width <= 0 || height <= 0 || dataLength <= 0) {
            throw new IllegalArgumentException("Embedded image metadata is invalid.");
        }

        if (dataLength > buffer.remaining()) {
            throw new IllegalArgumentException("Embedded image data length exceeds available payload.");
        }

        byte[] imageBytes = new byte[dataLength];
        buffer.get(imageBytes);

        BufferedImage extracted;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes)) {
            extracted = ImageIO.read(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to decode embedded image data", e);
        }

        if (extracted == null) {
            throw new IllegalArgumentException("Embedded image data could not be decoded.");
        }

        if (extracted.getWidth() != width || extracted.getHeight() != height) {
            throw new IllegalArgumentException("Embedded image dimensions do not match metadata.");
        }

        if (expectedWidth > 0 && expectedWidth != width) {
            throw new IllegalArgumentException("Embedded image width mismatch.");
        }
        if (expectedHeight > 0 && expectedHeight != height) {
            throw new IllegalArgumentException("Embedded image height mismatch.");
        }

        return extracted;
    }

    private static byte[] serialiseImage(BufferedImage image) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if (!ImageIO.write(image, IMAGE_FORMAT, outputStream)) {
                throw new IllegalStateException("No image writer available for format: " + IMAGE_FORMAT);
            }
            byte[] imageBytes = outputStream.toByteArray();
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES * 3 + imageBytes.length)
                                         .order(ByteOrder.BIG_ENDIAN);
            buffer.putInt(image.getWidth());
            buffer.putInt(image.getHeight());
            buffer.putInt(imageBytes.length);
            buffer.put(imageBytes);
            return buffer.array();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to encode image for embedding", e);
        }
    }
}
