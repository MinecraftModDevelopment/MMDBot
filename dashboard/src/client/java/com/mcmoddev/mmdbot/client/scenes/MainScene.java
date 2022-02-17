package com.mcmoddev.mmdbot.client.scenes;

import com.mcmoddev.mmdbot.client.DashboardClient;
import com.mcmoddev.mmdbot.dashboard.packets.ShutdownBotPacket;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainScene {

    public static void makeMainPageScene(Stage stage) {
        final var field = new TextField();
        final var button = new Button("Do");
        button.setOnAction(event -> {
            DashboardClient.sendAndAwaitGenericResponse(id -> new ShutdownBotPacket(id, field.getText()))
                .withPlatformAction(packet -> {
                    final var alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setResizable(true);
                    alert.setContentText(packet.response().toString());
                    alert.show();
                })
                .queue();
        });
        final var vbox = new VBox(field, button);
        stage.setScene(new Scene(vbox));
    }

}
