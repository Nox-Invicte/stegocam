package com.stegocam.gui;

import com.stegocam.controller.StegoController;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * JavaFX UI integrated with Spring for StegoCam application
 */
public class MainUI extends Application {
    
    private StegoController stegoController;
    private TextField inputImageField;
    private TextField outputImageField;
    private TextArea messageArea;
    private PasswordField passwordField;
    private TextArea resultArea;
    
    @Override
    public void start(Stage primaryStage) {
        stegoController = new StegoController();
        
        primaryStage.setTitle("StegoCam - Image Steganography Tool");
        
        // Create main layout
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.CENTER);
        
        // Input image selection
        HBox inputImageBox = createFileSelectionBox("Input Image:", "inputImageField", "Browse...");
        inputImageField = (TextField) inputImageBox.getChildren().get(1);
        
        // Output image selection
        HBox outputImageBox = createFileSelectionBox("Output Image:", "outputImageField", "Browse...");
        outputImageField = (TextField) outputImageBox.getChildren().get(1);
        
        // Message input
        Label messageLabel = new Label("Message to hide:");
        messageArea = new TextArea();
        messageArea.setPromptText("Enter your secret message here...");
        messageArea.setPrefRowCount(4);
        
        // Password input
        Label passwordLabel = new Label("Encryption Password:");
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter encryption password");
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button encryptButton = new Button("Encrypt & Embed");
        encryptButton.setOnAction(e -> handleEncrypt());
        
        Button decryptButton = new Button("Extract & Decrypt");
        decryptButton.setOnAction(e -> handleDecrypt());
        
        buttonBox.getChildren().addAll(encryptButton, decryptButton);
        
        // Result area
        Label resultLabel = new Label("Results:");
        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefRowCount(3);
        
        // Add all components to main layout
        mainLayout.getChildren().addAll(
            inputImageBox,
            outputImageBox,
            messageLabel,
            messageArea,
            passwordLabel,
            passwordField,
            buttonBox,
            resultLabel,
            resultArea
        );
        
        Scene scene = new Scene(mainLayout, 600, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private HBox createFileSelectionBox(String labelText, String fieldId, String buttonText) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        
        Label label = new Label(labelText);
        TextField textField = new TextField();
        textField.setId(fieldId);
        textField.setPrefWidth(300);
        
        Button browseButton = new Button(buttonText);
        browseButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Image File");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp")
            );
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                textField.setText(file.getAbsolutePath());
            }
        });
        
        box.getChildren().addAll(label, textField, browseButton);
        return box;
    }
    
    private void handleEncrypt() {
        try {
            String inputPath = inputImageField.getText();
            String outputPath = outputImageField.getText();
            String message = messageArea.getText();
            String password = passwordField.getText();
            
            if (inputPath.isEmpty() || outputPath.isEmpty() || message.isEmpty() || password.isEmpty()) {
                resultArea.setText("Please fill in all fields.");
                return;
            }
            
            boolean success = stegoController.encryptAndEmbed(inputPath, outputPath, message, password);
            
            if (success) {
                resultArea.setText("✓ Message successfully encrypted and embedded!\n" +
                                 "Output saved to: " + outputPath);
            } else {
                resultArea.setText("✗ Failed to encrypt and embed message.");
            }
            
        } catch (Exception e) {
            resultArea.setText("Error: " + e.getMessage());
        }
    }
    
    private void handleDecrypt() {
        try {
            String inputPath = inputImageField.getText();
            String password = passwordField.getText();
            
            if (inputPath.isEmpty() || password.isEmpty()) {
                resultArea.setText("Please provide input image and password.");
                return;
            }
            
            String decryptedMessage = stegoController.extractAndDecrypt(inputPath, password);
            
            if (decryptedMessage != null) {
                resultArea.setText("✓ Message successfully extracted and decrypted:\n\n" +
                                 decryptedMessage);
            } else {
                resultArea.setText("✗ Failed to extract and decrypt message.\n" +
                                 "Check if the image contains a message and the password is correct.");
            }
            
        } catch (Exception e) {
            resultArea.setText("Error: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
