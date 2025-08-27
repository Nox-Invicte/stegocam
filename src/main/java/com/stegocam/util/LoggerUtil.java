package com.stegocam.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logging helpers for the StegoCam application
 */
public class LoggerUtil {
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Log an info message with timestamp
     */
    public static void info(String message) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        System.out.println("[" + timestamp + "] INFO: " + message);
    }
    
    /**
     * Log a warning message with timestamp
     */
    public static void warn(String message) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        System.out.println("[" + timestamp + "] WARN: " + message);
    }
    
    /**
     * Log an error message with timestamp
     */
    public static void error(String message) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        System.err.println("[" + timestamp + "] ERROR: " + message);
    }
    
    /**
     * Log an error message with exception details
     */
    public static void error(String message, Exception e) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        System.err.println("[" + timestamp + "] ERROR: " + message);
        System.err.println("Exception: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        e.printStackTrace();
    }
    
    /**
     * Log a debug message with timestamp (only if debug mode is enabled)
     */
    public static void debug(String message) {
        // For now, debug logs are disabled by default
        // You can enable them by setting a system property or configuration
        if (Boolean.getBoolean("stegocam.debug")) {
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            System.out.println("[" + timestamp + "] DEBUG: " + message);
        }
    }
    
    /**
     * Log operation start
     */
    public static void startOperation(String operationName) {
        info("Starting operation: " + operationName);
    }
    
    /**
     * Log operation completion
     */
    public static void completeOperation(String operationName, boolean success) {
        if (success) {
            info("Operation completed successfully: " + operationName);
        } else {
            warn("Operation failed: " + operationName);
        }
    }
    
    /**
     * Log image processing information
     */
    public static void logImageInfo(String imagePath, String dimensions, long fileSize) {
        info("Image processed - Path: " + imagePath + 
             ", Dimensions: " + dimensions + 
             ", Size: " + fileSize + " bytes");
    }
    
    /**
     * Log encryption/decryption information
     */
    public static void logCryptoInfo(String operation, int dataSize, boolean success) {
        String status = success ? "successful" : "failed";
        info("Crypto operation " + operation + " " + status + " for " + dataSize + " bytes");
    }
}
