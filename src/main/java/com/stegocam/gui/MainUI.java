package com.stegocam.gui;

import com.stegocam.controller.StegoController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.imageio.ImageIO;

/**
 * JavaFX UI for StegoCam application - Pure Steganography
 */
public class MainUI {

    private final StegoController stegoController;
    private Stage ownerStage;
    private TextField sourceFolderField;
    private TextField imageFileField;
    private TextField outputNameField;
    private ComboBox<String> outputExtensionBox;
    private TextArea messageArea;
    private TextArea resultArea;

    public MainUI() {
        this.stegoController = new StegoController();
    }

    public void initialise(Stage primaryStage) {
        this.ownerStage = primaryStage;
        primaryStage.setTitle("StegoCam - Image Steganography Tool");

        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.CENTER);

        HBox folderSelectionBox = createFolderSelectionBox();
        HBox imageSelectionBox = createImageSelectionBox();
        HBox outputNamingBox = createOutputNamingBox();

        Label messageLabel = new Label("Message to hide:");
        messageArea = new TextArea();
        messageArea.setPromptText("Enter your secret message here...");
        messageArea.setPrefRowCount(4);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button embedButton = new Button("Embed Message");
        embedButton.setOnAction(e -> handleEmbed());

        Button extractButton = new Button("Extract Message");
        extractButton.setOnAction(e -> handleExtract());

        buttonBox.getChildren().addAll(embedButton, extractButton);

        Label resultLabel = new Label("Results:");
        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefRowCount(3);

        mainLayout.getChildren().addAll(
            folderSelectionBox,
            imageSelectionBox,
            outputNamingBox,
            messageLabel,
            messageArea,
            buttonBox,
            resultLabel,
            resultArea
        );

        Scene scene = new Scene(mainLayout, 600, 600);
        primaryStage.setScene(scene);
    }

    private HBox createFolderSelectionBox() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("Source Folder:");
        sourceFolderField = new TextField();
        sourceFolderField.setPrefWidth(300);

        Button browseButton = new Button("Browse...");
        browseButton.setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select Source Folder");
            File initial = resolveInitialDirectory();
            if (initial != null && initial.isDirectory()) {
                directoryChooser.setInitialDirectory(initial);
            }
            File directory = directoryChooser.showDialog(ownerStage);
            if (directory != null) {
                sourceFolderField.setText(directory.getAbsolutePath());
            }
        });

        box.getChildren().addAll(label, sourceFolderField, browseButton);
        return box;
    }

    private HBox createImageSelectionBox() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("Source Image:");
        imageFileField = new TextField();
        imageFileField.setPrefWidth(220);

        Button browseButton = new Button("Choose...");
        browseButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Source Image");

            Set<String> supportedExtensions = new LinkedHashSet<>();
            for (String suffix : ImageIO.getReaderFileSuffixes()) {
                if (suffix != null && !suffix.isBlank()) {
                    supportedExtensions.add("*." + suffix.toLowerCase());
                }
            }
            if (supportedExtensions.isEmpty()) {
                supportedExtensions.add("*.png");
                supportedExtensions.add("*.bmp");
                supportedExtensions.add("*.jpg");
                supportedExtensions.add("*.jpeg");
                supportedExtensions.add("*.gif");
            }

            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", supportedExtensions.toArray(new String[0]))
            );
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("All Files", "*.*")
            );
            File initial = resolveInitialDirectory();
            if (initial != null && initial.isDirectory()) {
                fileChooser.setInitialDirectory(initial);
            }
            File file = fileChooser.showOpenDialog(ownerStage);
            if (file != null) {
                imageFileField.setText(file.getName());
                sourceFolderField.setText(file.getParentFile().getAbsolutePath());
                if (outputNameField != null && (outputNameField.getText() == null || outputNameField.getText().isBlank())) {
                    outputNameField.setText(stripExtension(file.getName()) + "-stego");
                }
            }
        });

        box.getChildren().addAll(label, imageFileField, browseButton);
        return box;
    }

    private HBox createOutputNamingBox() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label("Output Name:");
        outputNameField = new TextField();
        outputNameField.setPrefWidth(200);

        Label dotLabel = new Label(".");

        outputExtensionBox = new ComboBox<>();
        outputExtensionBox.getItems().addAll("png", "bmp");
        outputExtensionBox.setValue("png");

        box.getChildren().addAll(nameLabel, outputNameField, dotLabel, outputExtensionBox);
        return box;
    }

    private File resolveInitialDirectory() {
        String folder = safeTrim(sourceFolderField == null ? null : sourceFolderField.getText());
        if (!folder.isEmpty()) {
            File dir = new File(folder);
            if (dir.exists() && dir.isDirectory()) {
                return dir;
            }
        }
        return null;
    }

    private String buildPath(String folder, String fileName) {
        return Paths.get(folder, fileName).normalize().toString();
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String stripExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex <= 0) {
            return fileName;
        }
        return fileName.substring(0, dotIndex);
    }

    private void handleEmbed() {
        try {
            String folder = safeTrim(sourceFolderField.getText());
            String imageName = safeTrim(imageFileField.getText());
            String outputName = safeTrim(outputNameField.getText());
            String extension = outputExtensionBox.getValue();
            String message = messageArea.getText();

            if (folder.isEmpty() || imageName.isEmpty() || outputName.isEmpty() ||
                extension == null || extension.isBlank() || message == null || message.isBlank()) {
                resultArea.setText("Please select a source folder, image file, output name, extension, and enter a message.");
                return;
            }

            Path folderPath = Paths.get(folder);
            if (!Files.isDirectory(folderPath)) {
                resultArea.setText("Source folder does not exist: " + folder);
                return;
            }

            String inputPath = buildPath(folder, imageName);
            if (!Files.exists(Paths.get(inputPath))) {
                resultArea.setText("Selected image could not be found: " + inputPath);
                return;
            }

            if (outputName.contains(".")) {
                resultArea.setText("Please enter the output name without an extension.");
                return;
            }

            String normalizedExtension = extension.startsWith(".") ? extension.substring(1) : extension;
            String outputFileName = outputName + "." + normalizedExtension;
            String outputPath = buildPath(folder, outputFileName);

            boolean success = stegoController.embedMessage(inputPath, outputPath, message);

            if (success) {
                resultArea.setText("✓ Message successfully embedded!\n" +
                                   "Output saved to: " + outputPath);
            } else {
                resultArea.setText("✗ Failed to embed message.\nCheck log for details.");
            }

        } catch (Exception e) {
            resultArea.setText("Error: " + e.getMessage());
        }
    }

    private void handleExtract() {
        try {
            String folder = safeTrim(sourceFolderField.getText());
            String imageName = safeTrim(imageFileField.getText());

            if (folder.isEmpty() || imageName.isEmpty()) {
                resultArea.setText("Please select a source folder and image file.");
                return;
            }

            Path folderPath = Paths.get(folder);
            if (!Files.isDirectory(folderPath)) {
                resultArea.setText("Source folder does not exist: " + folder);
                return;
            }

            String inputPath = buildPath(folder, imageName);
            if (!Files.exists(Paths.get(inputPath))) {
                resultArea.setText("Selected image could not be found: " + inputPath);
                return;
            }

            String extractedMessage = stegoController.extractMessage(inputPath);

            if (extractedMessage != null) {
                if (extractedMessage.isEmpty()) {
                    resultArea.setText("ℹ No hidden message detected in the selected image.");
                } else {
                    resultArea.setText("✓ Message successfully extracted:\n\n" + extractedMessage);
                }
            } else {
                resultArea.setText("✗ Failed to extract message.\n" +
                                   "Ensure the image contains an embedded message.");
            }

        } catch (Exception e) {
            resultArea.setText("Error: " + e.getMessage());
        }
    }
}
