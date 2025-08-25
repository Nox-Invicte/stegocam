package com.stegocam;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args); // Start JavaFX app
    }

    @Override
    public void start(Stage stage) {
        VBox root = new VBox();
        Button encryptBtn = new Button("Encrypt & Hide Message");
        Button extractBtn = new Button("Extract Message");

        root.getChildren().addAll(encryptBtn, extractBtn);

        Scene scene = new Scene(root, 400, 200);
        stage.setTitle("Stego-Cam");
        stage.setScene(scene);
        stage.show();
    }
}
