package com.stegocam;

import javafx.application.Application;
import javafx.stage.Stage;
import com.stegocam.gui.MainUI;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        MainUI mainUI = new MainUI();
        mainUI.start(stage); // Launches the MainUI

        stage.setTitle("StegoCam");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args); // Launches JavaFX application
    }
}
