# StegoCam - Building Windows Executable

## Quick Start - Build JAR (Easiest Method)

### Option 1: Using the Build Script
Simply double-click `build.bat` or run it from command prompt:
```cmd
build.bat
```

This will create `target\StegoCam-standalone.jar` which you can run with:
```cmd
java -jar target\StegoCam-standalone.jar
```

### Option 2: Manual Maven Build
```cmd
mvn clean package
```

The JAR will be at: `target\StegoCam-standalone.jar`

---

## Creating Windows EXE (Advanced)

### Method 1: Using Launch4j (Recommended)

1. **Download Launch4j**
   - Download from: https://launch4j.sourceforge.net/
   - Install Launch4j

2. **Build the JAR first**
   ```cmd
   mvn clean package
   ```

3. **Create EXE with Launch4j**
   - Open Launch4j
   - Configure:
     - **Output file**: `C:\Users\Sam\Desktop\Personal\Projects\stegocam\target\StegoCam.exe`
     - **Jar**: `C:\Users\Sam\Desktop\Personal\Projects\stegocam\target\StegoCam-standalone.jar`
     - **Icon**: (optional) Select an .ico file
     - **JRE minimum version**: 17
     - **Max heap size**: 1024 (MB)
   - Click the gear icon to build

4. **Run the EXE**
   ```cmd
   target\StegoCam.exe
   ```

### Method 2: Using jpackage (Native Windows Installer)

**Requirements:**
- JDK 17 or higher with jpackage tool
- WiX Toolset 3.x (for .msi installer) - Download from: https://wixtoolset.org/

**Steps:**

1. **Build the project**
   ```cmd
   mvn clean package
   ```

2. **Create runtime image**
   ```cmd
   jlink --add-modules java.base,java.desktop,java.logging,java.xml,javafx.controls,javafx.fxml,javafx.graphics,javafx.base,javafx.media,javafx.swing --output target/java-runtime
   ```

3. **Create Windows installer**
   ```cmd
   jpackage --input target --name StegoCam --main-jar StegoCam-standalone.jar --main-class com.stegocam.App --type msi --win-dir-chooser --win-menu --win-shortcut --app-version 1.0.0 --vendor "StegoCam" --dest target/installer
   ```

4. **Install and run**
   - The installer will be at: `target\installer\StegoCam-1.0.0.msi`
   - Double-click to install
   - Launch from Start Menu

### Method 3: Using JWrapper (Commercial, Easiest GUI)

1. Download JWrapper from: https://www.jwrapper.com/
2. Import your JAR
3. Configure Windows executable settings
4. Build

---

## Troubleshooting

### "Java not found" Error
Make sure Java 17+ is installed:
```cmd
java -version
```

If not installed, download from: https://adoptium.net/

### "Module not found" Error with jpackage
Your JAR might be missing JavaFX modules. Use the standalone JAR created by maven-shade-plugin.

### Large File Size
The EXE/installer will be large (~50-100MB) because it includes:
- JavaFX runtime
- All dependencies
- Optionally, a bundled JRE

---

## Quick Distribution Guide

### Distribute JAR (Requires Java)
- Users need Java 17+ installed
- Share: `StegoCam-standalone.jar`
- Run: `java -jar StegoCam-standalone.jar`

### Distribute EXE (Launch4j - Requires Java)
- Users need Java 17+ installed
- Share: `StegoCam.exe`
- Run: Double-click `StegoCam.exe`

### Distribute MSI Installer (jpackage - No Java Required)
- Bundles Java runtime
- Share: `StegoCam-1.0.0.msi`
- Install and run from Start Menu
- **Largest file but best user experience**

---

## Recommended Approach

For most users, I recommend **Method 1 (Launch4j)** because:
- ✅ Simple to use
- ✅ Creates a clean .exe file
- ✅ Free and open source
- ✅ Good for distribution
- ✅ Small file size if user has Java

For professional distribution where users may not have Java, use **Method 2 (jpackage)** to create a full installer with bundled JRE.
