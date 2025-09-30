package com.stegocam;

import com.stegocam.gui.MainUI;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    private final MainUI mainUI = new MainUI();

    @Override
    public void start(Stage stage) {
        mainUI.initialise(stage);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
