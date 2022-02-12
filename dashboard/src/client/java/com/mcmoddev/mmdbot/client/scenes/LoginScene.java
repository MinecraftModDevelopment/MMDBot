/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 * https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 */
package com.mcmoddev.mmdbot.client.scenes;

import com.mcmoddev.mmdbot.client.DashboardClient;
import com.mcmoddev.mmdbot.dashboard.common.packet.impl.CheckAuthorizedPacket;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public final class LoginScene {

    public static Scene createAddressSelectionScreen(final Stage stage) {

        final var addressLabel = new Label("Address: ");
        final var addressTextField = new TextField();
        final var continueButton = new Button("Continue");
        continueButton.setOnAction(event -> {
            var text = addressTextField.getText();
            if (text.indexOf(':') < 0) {
                final var alert = new Alert(AlertType.ERROR);
                alert.setContentText("Please provide a port!");
                alert.setTitle("Invalid port");
                alert.show();
                return;
            }
            final var hostName = text.substring(0, text.indexOf(':'));
            text = text.substring(text.indexOf(':') + 1);
            try {
                final var port = Integer.parseInt(text);
                DashboardClient.setup(new InetSocketAddress(InetAddress.getByName(hostName), port));
                stage.setScene(makeLoginScene());
                stage.show();
                stage.requestFocus();
            } catch (NumberFormatException e) {
                final var alert = new Alert(AlertType.ERROR);
                alert.setTitle("Invalid port");
                alert.setContentText("Please provide a valid port!");
                alert.show();
            } catch (UnknownHostException e) {
                final var alert = new Alert(AlertType.ERROR);
                alert.setContentText("The host provided is unknown!");
                alert.setTitle("Invalid port");
                alert.show();
            }
        });

        final var vbox = new VBox(4, addressLabel, addressTextField, continueButton);
        return new Scene(vbox);
    }

    private static Scene makeLoginScene() {
        final var usernameField = new TextField();
        final var passwordField = new TextField();
        final var loginBtn = new Button("Login");
        loginBtn.setOnAction(event -> {
            DashboardClient.sendPacket(new CheckAuthorizedPacket(usernameField.getText(), passwordField.getText()));
            DashboardClient.PACKET_WAITER.waitForPacket(CheckAuthorizedPacket.Response.class, p -> true, packet -> Platform.runLater(() -> {
                final var alert = new Alert(AlertType.INFORMATION);
                if (packet.getResponseType().isAuthorized()) {
                    alert.setTitle("Authorized");
                    alert.setContentText("You are authorized! The dashboard will open up soon.");
                    // TODO open the actual dashboard
                } else {
                    alert.setTitle("Invalid credentials");
                    alert.setContentText("The credentials you provided are invalid.");
                }
                alert.show();
            }), 20, TimeUnit.SECONDS, () -> Platform.runLater(() -> {
                final var alert = new Alert(AlertType.ERROR);
                alert.setTitle("No response");
                alert.setHeaderText("The server did not send a response!");
                alert.setContentText("This usually means that you are trying to connect to a server that is not a dashboard.");
                alert.setWidth(120);
                alert.setHeight(120);
                alert.show();
            }));
        });
        final var vbox = new VBox(4,
            new HBox(2, new Label("Username: "), usernameField),
            new HBox(2, new Label("Password: "), passwordField),
            loginBtn);
        return new Scene(vbox);
    }

}
