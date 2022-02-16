package com.mcmoddev.mmdbot.client.scenes;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainScene {

    public static void makeMainPageScene(Stage stage) {
        final var vbox = new VBox();
        stage.setScene(new Scene(vbox));
    }

}
