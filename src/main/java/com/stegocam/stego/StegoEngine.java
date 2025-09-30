package com.stegocam.stego;

import com.stegocam.config.AppConfig;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

/**
 * Core steganography engine that performs bit-level embedding and extraction
 * using least significant bit (LSB) manipulation across RGB channels.
 */
public class StegoEngine {

    private static final int CHANNEL_COUNT = 3; // R, G, B

    /**
     * Convert an array of bytes (MSB first) into a boolean array.
     */
    public boolean[] bytesToBits(byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes");
        boolean[] bits = new boolean[bytes.length * 8];
        for (int byteIndex = 0; byteIndex < bytes.length; byteIndex++) {
            int value = bytes[byteIndex] & 0xFF;
            for (int bit = 7; bit >= 0; bit--) {
                int bitPosition = (byteIndex * 8) + (7 - bit);
                bits[bitPosition] = ((value >> bit) & 1) == 1;
            }
        }
        return bits;
    }

    /**
     * Convert a boolean array (MSB first) into bytes. Any remaining bits that
     * do not fill a complete byte are ignored.
     */
    public byte[] bitsToBytes(boolean[] bits) {
        Objects.requireNonNull(bits, "bits");
        int byteCount = bits.length / 8;
        byte[] bytes = new byte[byteCount];
        for (int byteIndex = 0; byteIndex < byteCount; byteIndex++) {
            int value = 0;
            for (int bit = 0; bit < 8; bit++) {
                if (bits[(byteIndex * 8) + bit]) {
                    value |= (1 << (7 - bit));
                }
            }
            bytes[byteIndex] = (byte) value;
        }
        return bytes;
    }

    /**
     * Embed the provided bits into a single 8-bit colour component starting at
     * the specified bit index (LSB index = 0).
     */
    public int embedBits(int colourValue, boolean[] bitsToEmbed, int bitIndex) {
        Objects.requireNonNull(bitsToEmbed, "bitsToEmbed");
        int result = colourValue & 0xFF;
        int chunkLength = bitsToEmbed.length;
        for (int i = 0; i < chunkLength; i++) {
            int position = bitIndex + (chunkLength - 1 - i);
            if (bitsToEmbed[i]) {
                result |= (1 << position);
            } else {
                result &= ~(1 << position);
            }
        }
        return result;
    }

    /**
     * Extract the requested number of bits from a colour component into the
     * supplied buffer, starting at the given bit index (LSB index = 0).
     */
    public void extractBits(int colourValue, boolean[] destination, int bitIndex) {
        Objects.requireNonNull(destination, "destination");
        int length = destination.length;
        for (int i = 0; i < length; i++) {
            int position = bitIndex + (length - 1 - i);
            destination[i] = ((colourValue >> position) & 1) == 1;
        }
    }

    /**
     * Determine whether the given image has sufficient capacity (based on the
     * configured number of LSB bits per channel) to store the requested number
     * of bytes.
     */
    public boolean canHoldMessage(BufferedImage image, int messageLengthBytes) {
        if (image == null || messageLengthBytes < 0) {
            return false;
        }
        long capacityBits = (long) image.getWidth() * image.getHeight() * CHANNEL_COUNT * AppConfig.LSB_BITS;
        long requiredBits = (long) messageLengthBytes * 8L;
        return requiredBits <= capacityBits;
    }

    /**
     * Embed a message (UTF-8 or arbitrary binary payload) into the provided
     * image using LSB steganography. The returned image is a copy of the
     * source with the payload embedded and includes a 32-bit length prefix.
     */
    public BufferedImage embedMessage(BufferedImage source, byte[] message) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(message, "message");

        int totalBytes = Integer.BYTES + message.length;
        if (!canHoldMessage(source, totalBytes)) {
            throw new IllegalArgumentException("Message is too large for the provided image.");
        }

        BufferedImage target = duplicateImage(source);
        byte[] prefixedPayload = encodeLengthPrefixed(message);
        boolean[] bits = bytesToBits(prefixedPayload);
        writeBits(target, bits);
        return target;
    }

    /**
     * Extract a steganographically hidden payload from the given image. The
     * payload is expected to have been stored with {@link #embedMessage} and
     * therefore begins with a 32-bit length prefix.
     */
    public byte[] extractMessage(BufferedImage stegoImage) {
        Objects.requireNonNull(stegoImage, "stegoImage");

        int headerBits = Integer.BYTES * 8;
        boolean[] lengthBits = new boolean[headerBits];
        boolean[] messageBits = null;
        int lengthBitIndex = 0;
        int messageBitIndex = 0;

        outerLoop:
        for (int y = 0; y < stegoImage.getHeight(); y++) {
            for (int x = 0; x < stegoImage.getWidth(); x++) {
                int pixel = stegoImage.getRGB(x, y);
                int[] channels = extractChannels(pixel);

                for (int channel = 0; channel < CHANNEL_COUNT; channel++) {
                    int colour = channels[channel];
                    for (int bit = AppConfig.LSB_BITS - 1; bit >= 0; bit--) {
                        boolean value = ((colour >> bit) & 1) == 1;

                        if (lengthBitIndex < headerBits) {
                            lengthBits[lengthBitIndex++] = value;
                            if (lengthBitIndex == headerBits) {
                                byte[] lengthBytes = bitsToBytes(lengthBits);
                                int messageLength = decodeMessageLength(lengthBytes);
                                if (messageLength == 0) {
                                    return new byte[0];
                                }
                                long requiredBits = (long) messageLength * 8L;
                                long availableBits = (long) stegoImage.getWidth() * stegoImage.getHeight() * CHANNEL_COUNT * AppConfig.LSB_BITS;
                                if (headerBits + requiredBits > availableBits) {
                                    throw new IllegalArgumentException("Embedded message length exceeds image capacity.");
                                }
                                messageBits = new boolean[(int) requiredBits];
                            }
                        } else if (messageBits != null) {
                            if (messageBitIndex < messageBits.length) {
                                messageBits[messageBitIndex++] = value;
                                if (messageBitIndex == messageBits.length) {
                                    break outerLoop;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (messageBits == null) {
            throw new IllegalArgumentException("No embedded message length found.");
        }
        if (messageBitIndex != messageBits.length) {
            throw new IllegalArgumentException("Image ended before the embedded message was fully read.");
        }
        return bitsToBytes(messageBits);
    }

    private BufferedImage duplicateImage(BufferedImage source) {
        int imageType = source.getType() == BufferedImage.TYPE_CUSTOM ? BufferedImage.TYPE_INT_ARGB : source.getType();
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), imageType);
        Graphics2D graphics = copy.createGraphics();
        try {
            graphics.drawImage(source, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return copy;
    }

    private void writeBits(BufferedImage target, boolean[] bits) {
        int totalBits = bits.length;
        int bitIndex = 0;

        for (int y = 0; y < target.getHeight() && bitIndex < totalBits; y++) {
            for (int x = 0; x < target.getWidth() && bitIndex < totalBits; x++) {
                int pixel = target.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xFF;
                int red = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = pixel & 0xFF;

                red = embedIntoChannel(red, bits, bitIndex);
                bitIndex += Math.min(AppConfig.LSB_BITS, totalBits - bitIndex);

                if (bitIndex < totalBits) {
                    green = embedIntoChannel(green, bits, bitIndex);
                    bitIndex += Math.min(AppConfig.LSB_BITS, totalBits - bitIndex);
                }

                if (bitIndex < totalBits) {
                    blue = embedIntoChannel(blue, bits, bitIndex);
                    bitIndex += Math.min(AppConfig.LSB_BITS, totalBits - bitIndex);
                }

                int combined = (alpha << 24) | (red << 16) | (green << 8) | blue;
                target.setRGB(x, y, combined);
            }
        }

        if (bitIndex < totalBits) {
            throw new IllegalStateException("Image exhausted before all bits could be embedded.");
        }
    }

    private int embedIntoChannel(int channelValue, boolean[] bits, int bitIndex) {
        int result = channelValue & 0xFF;
        int bitsRemaining = bits.length - bitIndex;
        int bitsToEmbed = Math.min(AppConfig.LSB_BITS, bitsRemaining);
        for (int i = 0; i < bitsToEmbed; i++) {
            boolean bitValue = bits[bitIndex + i];
            int position = AppConfig.LSB_BITS - 1 - i;
            if (bitValue) {
                result |= (1 << position);
            } else {
                result &= ~(1 << position);
            }
        }
        return result;
    }

    private int[] extractChannels(int pixel) {
        int alpha = (pixel >> 24) & 0xFF;
        int red = (pixel >> 16) & 0xFF;
        int green = (pixel >> 8) & 0xFF;
        int blue = pixel & 0xFF;
        return new int[]{red, green, blue, alpha};
    }

    private byte[] encodeLengthPrefixed(byte[] message) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + message.length).order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(message.length);
        buffer.put(message);
        return buffer.array();
    }

    private int decodeMessageLength(byte[] lengthBytes) {
        if (lengthBytes.length < Integer.BYTES) {
            throw new IllegalArgumentException("Invalid length prefix.");
        }
        int length = ByteBuffer.wrap(lengthBytes).order(ByteOrder.BIG_ENDIAN).getInt();
        if (length < 0) {
            throw new IllegalArgumentException("Negative embedded message length.");
        }
        return length;
    }
}
