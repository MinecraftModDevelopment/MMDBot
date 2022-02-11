package com.mcmoddev.mmdbot.client.scenes;

import com.mcmoddev.mmdbot.client.DashboardClient;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public final class LoginScene {

    public static Scene createAddressSelectionScreen(final Stage stage) {

        final var addressLabel = new Label("Address: ");
        final var addressTextField = new TextField();
        final var continueButton = new Button("Continue");
        continueButton.setOnAction(event -> {
            var text = addressTextField.getText();
            final var hostName = text.substring(0, text.indexOf(':'));
            text = text.substring(text.indexOf(':') + 1);
            final var port = Integer.parseInt(text);
            try {
                DashboardClient.setup(new InetSocketAddress(InetAddress.getByName(hostName), 6000));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        });

        final var vbox = new VBox(4, addressLabel, addressTextField, continueButton);
        return new Scene(vbox);
    }

}
