package com.stegocam.config;

/**
 * Application configuration class
 * Contains application-wide configuration settings
 */
public class AppConfig {
    
    // Image processing settings
    public static final int MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final String[] SUPPORTED_IMAGE_FORMATS = {"jpg", "jpeg", "png", "bmp"};
    
    // Encryption settings
    public static final String CRYPTO_ALGORITHM = "AES/GCM/NoPadding";
    public static final int KEY_SIZE = 256;
    public static final int IV_SIZE = 12;
    
    // Steganography settings
    public static final int LSB_BITS = 2;
    public static final int MAX_MESSAGE_SIZE = 1024;
    
    // Application settings
    public static final String APP_NAME = "StegoCam";
    public static final String APP_VERSION = "1.0.0";
}
