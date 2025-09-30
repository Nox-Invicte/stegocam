package com.stegocam.controller;

import com.stegocam.Steganography;
import com.stegocam.io.ImageHandler;
import com.stegocam.stego.StegoEngine;
import com.stegocam.util.LoggerUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * High-level application controller for steganography workflows. Bridges the
 * UI with the lower-level engine and handles file system interactions.
 */
public class StegoController {

    private final ImageHandler imageHandler;
    private final StegoEngine stegoEngine;

    public StegoController() {
        this(new ImageHandler(), new StegoEngine());
    }

    public StegoController(ImageHandler imageHandler, StegoEngine stegoEngine) {
        this.imageHandler = Objects.requireNonNull(imageHandler, "imageHandler");
        this.stegoEngine = Objects.requireNonNull(stegoEngine, "stegoEngine");
    }

    public boolean embedMessage(String inputPath, String outputPath, String message) {
        LoggerUtil.startOperation("Embed message");
        if (isBlank(inputPath) || isBlank(outputPath)) {
            LoggerUtil.warn("Input and output paths must be provided.");
            LoggerUtil.completeOperation("Embed message", false);
            return false;
        }

        String trimmedInput = inputPath.trim();
        String trimmedOutput = outputPath.trim();

        BufferedImage coverImage = imageHandler.loadImage(trimmedInput);
        if (coverImage == null) {
            LoggerUtil.warn("Unable to load input image: " + inputPath);
            LoggerUtil.completeOperation("Embed message", false);
            return false;
        }

        byte[] payload = message == null ? new byte[0] : message.getBytes(StandardCharsets.UTF_8);

        try {
            BufferedImage stegoImage = stegoEngine.embedMessage(coverImage, payload);
            ensureParentDirectory(trimmedOutput);
            boolean saved = imageHandler.saveImage(stegoImage, trimmedOutput);
            LoggerUtil.completeOperation("Embed message", saved);
            return saved;
        } catch (IllegalArgumentException ex) {
            LoggerUtil.warn("Failed to embed message: " + ex.getMessage());
            LoggerUtil.completeOperation("Embed message", false);
            return false;
        } catch (Exception ex) {
            LoggerUtil.error("Unexpected error during embedding", ex);
            LoggerUtil.completeOperation("Embed message", false);
            return false;
        }
    }

    public String extractMessage(String inputPath) {
        LoggerUtil.startOperation("Extract message");
        if (isBlank(inputPath)) {
            LoggerUtil.warn("Input path must be provided.");
            LoggerUtil.completeOperation("Extract message", false);
            return null;
        }

        String trimmedInput = inputPath.trim();
        BufferedImage stegoImage = imageHandler.loadImage(trimmedInput);
        if (stegoImage == null) {
            LoggerUtil.warn("Unable to load image for extraction: " + inputPath);
            LoggerUtil.completeOperation("Extract message", false);
            return null;
        }

        try {
            byte[] data = stegoEngine.extractMessage(stegoImage);
            String message = new String(data, StandardCharsets.UTF_8);
            LoggerUtil.completeOperation("Extract message", true);
            return message;
        } catch (IllegalArgumentException ex) {
            if (isNoMessageFound(ex)) {
                LoggerUtil.warn("No embedded message detected in image: " + inputPath);
                LoggerUtil.completeOperation("Extract message", true);
                return "";
            }
            LoggerUtil.warn("Extraction failed: " + ex.getMessage());
            LoggerUtil.completeOperation("Extract message", false);
            return null;
        } catch (Exception ex) {
            LoggerUtil.error("Unexpected error during extraction", ex);
            LoggerUtil.completeOperation("Extract message", false);
            return null;
        }
    }

    public boolean embedImage(String coverImagePath, String secretImagePath, String outputPath) {
        LoggerUtil.startOperation("Embed image");
        if (isBlank(coverImagePath) || isBlank(secretImagePath) || isBlank(outputPath)) {
            LoggerUtil.warn("Cover image, secret image, and output paths must be provided.");
            LoggerUtil.completeOperation("Embed image", false);
            return false;
        }

        String trimmedCover = coverImagePath.trim();
        String trimmedSecret = secretImagePath.trim();
        String trimmedOutput = outputPath.trim();

        BufferedImage coverImage = imageHandler.loadImage(trimmedCover);
        BufferedImage secretImage = imageHandler.loadImage(trimmedSecret);
        if (coverImage == null || secretImage == null) {
            LoggerUtil.warn("Unable to load required images for embedding.");
            LoggerUtil.completeOperation("Embed image", false);
            return false;
        }

        try {
            BufferedImage stegoImage = Steganography.embedImage(coverImage, secretImage);
            ensureParentDirectory(trimmedOutput);
            boolean saved = imageHandler.saveImage(stegoImage, trimmedOutput);
            LoggerUtil.completeOperation("Embed image", saved);
            return saved;
        } catch (IllegalArgumentException ex) {
            LoggerUtil.warn("Failed to embed image: " + ex.getMessage());
            LoggerUtil.completeOperation("Embed image", false);
            return false;
        } catch (Exception ex) {
            LoggerUtil.error("Unexpected error during image embedding", ex);
            LoggerUtil.completeOperation("Embed image", false);
            return false;
        }
    }

    public boolean extractImage(String inputPath, String outputPath) {
        LoggerUtil.startOperation("Extract image");
        if (isBlank(inputPath) || isBlank(outputPath)) {
            LoggerUtil.warn("Input and output paths must be provided for extraction.");
            LoggerUtil.completeOperation("Extract image", false);
            return false;
        }

        String trimmedInput = inputPath.trim();
        BufferedImage stegoImage = imageHandler.loadImage(trimmedInput);
        if (stegoImage == null) {
            LoggerUtil.warn("Unable to load stego image for extraction: " + inputPath);
            LoggerUtil.completeOperation("Extract image", false);
            return false;
        }

        try {
            BufferedImage extracted = Steganography.extractImage(stegoImage, 0, 0);
            if (extracted == null) {
                LoggerUtil.warn("No embedded image data present.");
                LoggerUtil.completeOperation("Extract image", false);
                return false;
            }
            String trimmedOutput = outputPath.trim();
            ensureParentDirectory(trimmedOutput);
            boolean saved = imageHandler.saveImage(extracted, trimmedOutput);
            LoggerUtil.completeOperation("Extract image", saved);
            return saved;
        } catch (IllegalArgumentException ex) {
            LoggerUtil.warn("Failed to extract image: " + ex.getMessage());
            LoggerUtil.completeOperation("Extract image", false);
            return false;
        } catch (Exception ex) {
            LoggerUtil.error("Unexpected error during image extraction", ex);
            LoggerUtil.completeOperation("Extract image", false);
            return false;
        }
    }

    private void ensureParentDirectory(String outputPath) {
        File output = new File(outputPath);
        File parent = output.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isNoMessageFound(IllegalArgumentException ex) {
        String message = ex.getMessage();
        if (message == null) {
            return false;
        }
        return message.contains("Embedded message length exceeds image capacity")
            || message.contains("No embedded message length found")
            || message.contains("Image ended before the embedded message was fully read");
    }
}
