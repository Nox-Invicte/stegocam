package com.stegocam.gui;

import com.stegocam.controller.StegoController;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

public class MainUI {

    private final StegoController stegoController;
    private Stage ownerStage;
    private TextField sourceFolderField;
    private TextField imageFileField;
    private TextField outputNameField;
    private ComboBox<String> outputExtensionBox;
    private TextArea messageArea;
    private TextArea resultArea;

    // Visualization
    private ImageView originalView;
    private ImageView stegoView;
    private ProgressBar progress;
    private Label stepLabel;

    public MainUI() {
        this.stegoController = new StegoController();
    }

    public void initialise(Stage primaryStage) {
        this.ownerStage = primaryStage;
        primaryStage.setTitle("StegoCam — Image Steganography");

        // Header
        Label header = new Label("🕵️‍♂️ StegoCam");
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        header.setTextFill(Color.web("#ffffff"));
        Label subtitle = new Label("Hide and extract secret messages in images securely");
        subtitle.setFont(Font.font("Segoe UI", 14));
        subtitle.setTextFill(Color.web("#bbbbbb"));
        VBox headerBox = new VBox(header, subtitle);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(10));

        // Left column
        VBox folderSection = createTitledSection("Source Settings", createFolderSelectionBox(), createImageSelectionBox());
        VBox outputSection = createTitledSection("Output Settings", createOutputNamingBox());
        VBox messageSection = createTitledSection("Secret Message", createMessageBox());
        VBox actionSection = createActionSection();
        VBox resultSection = createResultSection();
        VBox leftColumn = new VBox(20, folderSection, outputSection, messageSection, actionSection, resultSection);
        leftColumn.setPadding(new Insets(20));

        // Right column (visualization)
        VBox rightColumn = createVisualizationPanel();

        // Root
        BorderPane root = new BorderPane();
        root.setTop(headerBox);
        root.setLeft(leftColumn);
        root.setCenter(rightColumn);
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #2c2f33, #23272a);");
        BorderPane.setMargin(leftColumn, new Insets(20));
        BorderPane.setMargin(rightColumn, new Insets(20));

        Scene scene = new Scene(root, 1100, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // ---------------- UI Components ----------------

    private VBox createTitledSection(String title, javafx.scene.Node... content) {
        Label label = new Label(title);
        label.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 16));
        label.setTextFill(Color.web("#ffffff"));
        VBox box = new VBox(10);
        box.getChildren().add(label);
        box.getChildren().addAll(content);
        box.setPadding(new Insets(10, 15, 15, 15));
        box.setStyle("-fx-background-color: #40444b; -fx-background-radius: 10;");
        return box;
    }

    private HBox createFolderSelectionBox() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        Label label = styledLabel("Source Folder:");
        sourceFolderField = styledTextField(300);
        Button browseButton = styledButton("Browse");
        browseButton.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select Source Folder");
            File dir = chooser.showDialog(ownerStage);
            if (dir != null) sourceFolderField.setText(dir.getAbsolutePath());
        });
        box.getChildren().addAll(label, sourceFolderField, browseButton);
        return box;
    }

    private HBox createImageSelectionBox() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        Label label = styledLabel("Source Image:");
        imageFileField = styledTextField(220);
        Button browseButton = styledButton("Choose");
        browseButton.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Image File");
            Set<String> exts = new LinkedHashSet<>();
            for (String ext : ImageIO.getReaderFileSuffixes()) exts.add("*." + ext.toLowerCase());
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", exts.toArray(new String[0])));
            File file = chooser.showOpenDialog(ownerStage);
            if (file != null) {
                imageFileField.setText(file.getName());
                sourceFolderField.setText(file.getParent());
                if (outputNameField.getText().isBlank()) outputNameField.setText(stripExtension(file.getName()) + "-stego");
                originalView.setImage(new Image("file:" + file.getAbsolutePath()));
            }
        });
        box.getChildren().addAll(label, imageFileField, browseButton);
        return box;
    }

    private HBox createOutputNamingBox() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = styledLabel("Output Name:");
        outputNameField = styledTextField(200);
        Label dotLabel = styledLabel(".");
        outputExtensionBox = new ComboBox<>();
        outputExtensionBox.getItems().addAll("png", "bmp");
        outputExtensionBox.setValue("png");
        box.getChildren().addAll(nameLabel, outputNameField, dotLabel, outputExtensionBox);
        return box;
    }

    private VBox createMessageBox() {
        Label label = styledLabel("Message to hide:");
        messageArea = new TextArea();
        messageArea.setPromptText("Enter your secret message here...");
        messageArea.setPrefRowCount(4);
        messageArea.setStyle("-fx-control-inner-background: #2c2f33; -fx-text-fill: white; -fx-border-radius: 8; -fx-background-radius: 8;");
        return new VBox(8, label, messageArea);
    }

    private VBox createActionSection() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        Button embedBtn = styledButton("💾 Embed Message");
        Button extractBtn = styledButton("🔍 Extract Message");
        embedBtn.setOnAction(e -> handleEmbed());
        extractBtn.setOnAction(e -> handleExtract());
        buttonBox.getChildren().addAll(embedBtn, extractBtn);
        return new VBox(buttonBox);
    }

    private VBox createResultSection() {
        Label label = styledLabel("Results:");
        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefRowCount(4);
        resultArea.setWrapText(true);
        resultArea.setStyle("-fx-control-inner-background: #1e2124; -fx-text-fill: #00ffcc; -fx-font-family: 'Consolas'; -fx-border-radius: 8; -fx-background-radius: 8;");
        return new VBox(8, label, resultArea);
    }

    // ---------------- Visualization Panel ----------------
    private VBox createVisualizationPanel() {
        Label title = new Label("🧠 How Steganography Works");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#00ffcc"));

        stepLabel = new Label("1️⃣ Select an image to begin.");
        stepLabel.setTextFill(Color.web("#dddddd"));
        stepLabel.setWrapText(true);

        progress = new ProgressBar(0);
        progress.setPrefWidth(250);
        progress.setStyle("-fx-accent: #00ffcc;");

        originalView = new ImageView();
        originalView.setFitWidth(250);
        originalView.setFitHeight(180);
        originalView.setStyle("-fx-border-color: #777; -fx-border-radius: 8; -fx-background-color: #2c2f33;");

        stegoView = new ImageView();
        stegoView.setFitWidth(250);
        stegoView.setFitHeight(180);
        stegoView.setStyle("-fx-border-color: #00ffcc; -fx-border-radius: 8; -fx-background-color: #2c2f33;");

        VBox panel = new VBox(15, title, stepLabel, progress, originalView, stegoView);
        panel.setPadding(new Insets(20));
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setStyle("-fx-background-color: #40444b; -fx-background-radius: 12;");
        return panel;
    }

    // ---------------- Utility Methods ----------------
    private Label styledLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.web("#ffffff"));
        label.setFont(Font.font("Segoe UI", 14));
        return label;
    }

    private TextField styledTextField(double width) {
        TextField tf = new TextField();
        tf.setPrefWidth(width);
        tf.setStyle("-fx-background-color: #2c2f33; -fx-text-fill: white; -fx-background-radius: 6;");
        return tf;
    }

    private Button styledButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        btn.setTextFill(Color.WHITE);
        btn.setStyle("-fx-background-color: #5865f2; -fx-background-radius: 8;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #4752c4; -fx-background-radius: 8;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #5865f2; -fx-background-radius: 8;"));
        return btn;
    }

    private String stripExtension(String name) {
        int i = name.lastIndexOf('.');
        return (i > 0) ? name.substring(0, i) : name;
    }

    // ---------------- Core Functionalities ----------------
    private void handleEmbed() {
        try {
            String folder = sourceFolderField.getText().trim();
            String image = imageFileField.getText().trim();
            String outputName = outputNameField.getText().trim();
            String extension = outputExtensionBox.getValue();
            String message = messageArea.getText();

            if (folder.isEmpty() || image.isEmpty() || outputName.isEmpty() || message.isEmpty()) {
                resultArea.setText("⚠ Please fill in all fields!");
                return;
            }

            String inputPath = Paths.get(folder, image).toString();
            String outputPath = Paths.get(folder, outputName + "." + extension).toString();

            // Load original image
            BufferedImage origImg = ImageIO.read(new File(inputPath));
            originalView.setImage(new Image("file:" + inputPath));
            stepLabel.setText("Embedding message...");

            new Thread(() -> {
                try {
                    int width = origImg.getWidth();
                    int height = origImg.getHeight();
                    BufferedImage animImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            animImg.setRGB(x, y, origImg.getRGB(x, y));
                        }
                        int progressVal = y;
                        Platform.runLater(() -> {
                            progress.setProgress(progressVal / (double) height);
                            stegoView.setImage(SwingFXUtils.toFXImage(animImg, null));
                        });
                        Thread.sleep(2); // animation speed
                    }

                    boolean success = stegoController.embedMessage(inputPath, outputPath, message);
                    Platform.runLater(() -> {
                        if (success) {
                            stepLabel.setText("✅ Message embedded successfully!");
                            stegoView.setImage(new Image("file:" + outputPath));
                            resultArea.setText("✅ Message embedded!\nSaved to: " + outputPath);
                        } else {
                            stepLabel.setText("❌ Embedding failed.");
                            resultArea.setText("❌ Embedding failed.");
                        }
                        progress.setProgress(0);
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> resultArea.setText("Error: " + ex.getMessage()));
                }
            }).start();

        } catch (Exception ex) {
            resultArea.setText("Error: " + ex.getMessage());
        }
    }

    private void handleExtract() {
        try {
            String folder = sourceFolderField.getText().trim();
            String image = imageFileField.getText().trim();

            if (folder.isEmpty() || image.isEmpty()) {
                resultArea.setText("⚠ Please select a source folder and image.");
                return;
            }

            String inputPath = Paths.get(folder, image).toString();
            String message = stegoController.extractMessage(inputPath);

            stepLabel.setText((message != null && !message.isEmpty())
                    ? "🔍 Message extracted!"
                    : "ℹ No hidden message found.");
            resultArea.setText((message != null && !message.isEmpty())
                    ? "✅ Extracted Message:\n\n" + message
                    : "ℹ No hidden message found.");
        } catch (Exception ex) {
            resultArea.setText("Error: " + ex.getMessage());
        }
    }
}
