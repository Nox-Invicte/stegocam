# StegoCam 🕵️‍♂️

**Advanced Image Steganography Desktop Application**

StegoCam is a powerful Java desktop application that lets you **hide secret messages or entire images** inside ordinary pictures using **Least Significant Bit (LSB) steganography**. The modifications are visually imperceptible, making it an excellent tool for covert communication and data protection.

## ✨ Features

- **Text Steganography** — Embed and extract UTF-8 text messages
- **Image-in-Image Steganography** — Hide one full image inside another
- **AES-256 Encryption Layer** — Strong encryption applied before embedding (recommended)
- **Capacity Validation** — Automatically checks if the image can hold your payload
- **Live Visualization** — Before/after image preview in the UI
- **Multiple Output Formats** — PNG (recommended), BMP, and more
- **Cross-Platform** — Works on Windows, macOS, and Linux
- **Easy Distribution** — Runnable JAR + Windows EXE / MSI installer options

## 🧠 How It Works

StegoCam uses **LSB (Least Significant Bit) manipulation** across the RGB channels of an image:

1. The secret payload (text or image) is prepared and optionally encrypted with AES-256.
2. A 32-bit length prefix is added for reliable extraction.
3. The payload is converted into a bit stream.
4. These bits replace the least significant bits of the color channels in the cover image.
5. The resulting "stego" image looks nearly identical to the original.

The core logic lives in `StegoEngine.java`, while high-level operations are handled by `Steganography.java`.

## 📥 Installation & Quick Start

### Prerequisites
- **Java 17 or higher** (Temurin JDK recommended)

### Option 1: Run Pre-built JAR (Recommended for Users)
1. Download the latest `StegoCam-standalone.jar` from the [Releases](../../releases) page.
2. Run the application:
   ```bash
   java -jar StegoCam-standalone.jar
   ```

### Option 2: Build from Source
```bash
git clone https://github.com/Nox-Invicte/stegocam.git
cd stegocam

# Build the project
mvn clean package

# Run the application
java -jar target/StegoCam-standalone.jar
```

For detailed Windows EXE and installer instructions, refer to **[BUILD_INSTRUCTIONS.md](BUILD_INSTRUCTIONS.md)**.

## 📖 Usage Guide

### Embedding a Secret Message
1. Launch StegoCam
2. Select a **Source Folder** and **Source Image** (larger, high-resolution images offer more capacity)
3. Enter your **Secret Message** in the text area
4. Provide a strong **Encryption Key** (AES-256)
5. Set the desired **Output Name** and format (`.png` is recommended)
6. Click **💾 Embed Message**
7. The stego image will be saved in the source folder

### Extracting a Hidden Message
1. Load the stego image as the source image
2. Enter the **same Encryption Key** used during embedding
3. Click **🔍 Extract Message**
4. The decrypted message will appear in the Results panel

### Advanced: Image-in-Image Embedding
The underlying `Steganography` class supports embedding and extracting full images via `embedImage()` and `extractImage()` methods.

## 🏗️ Project Structure

```
stegocam/
├── src/main/java/com/stegocam/
│   ├── App.java                    # JavaFX Application launcher
│   ├── Steganography.java          # High-level stego API
│   ├── stego/
│   │   └── StegoEngine.java        # Core LSB bit manipulation engine
│   ├── gui/
│   │   └── MainUI.java             # Main JavaFX user interface
│   ├── controller/
│   │   └── StegoController.java    # Business logic controller
│   ├── crypto/
│   │   └── MessageEncryption.java  # AES-256 encryption
│   ├── config/
│   │   └── AppConfig.java          # Configuration constants
│   ├── io/                         # I/O utilities
│   └── util/                       # Helper classes
├── pom.xml                         # Maven build configuration
├── build.bat                       # Windows build helper
├── BUILD_INSTRUCTIONS.md
├── TODO.md
└── launch4j-config.xml
```

## 🔧 Technologies Used

- **Language**: Java 17
- **UI Framework**: JavaFX 21
- **Build Tool**: Maven
- **Image Processing**: Java ImageIO
- **Encryption**: AES-256
- **Packaging**: Spring Boot Maven Plugin + Launch4j + jpackage

## ⚙️ Configuration

Key settings (such as number of LSB bits per channel) can be adjusted in `AppConfig.java`. This controls the balance between **hiding capacity** and **visual stealth**.

## 🔒 Security Notes

- Steganography hides data but does not encrypt it by default — always use the encryption feature with a strong, unique key.
- Use lossless formats like PNG for best results.
- Avoid using the same cover image repeatedly for multiple messages.
- This tool is for educational and legitimate privacy purposes.

## 📋 Roadmap & Status

See **[TODO.md](TODO.md)** for the current development status and planned features.

**Completed:**
- Pure steganography core
- Full GUI with previews
- Text and image embedding/extraction
- Build pipeline for JAR and native executables

## 🧪 Running Tests

```bash
mvn test
```

## 🤝 Contributing

Contributions, bug reports, and feature suggestions are welcome! Feel free to open an issue or submit a pull request.

## 📄 License

This project is open source. See the repository for full licensing information.

---

**StegoCam** — *Hide in plain sight.*

Built for privacy enthusiasts, developers, and security researchers.

*Made with Java & ❤️*
