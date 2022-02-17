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
import com.mcmoddev.mmdbot.client.util.Consumer;
import com.mcmoddev.mmdbot.client.util.ExceptionFunction;
import com.mcmoddev.mmdbot.client.util.Fonts;
import com.mcmoddev.mmdbot.client.util.StyleUtils;
import com.mcmoddev.mmdbot.dashboard.packets.CheckAuthorizedPacket;
import com.mcmoddev.mmdbot.dashboard.packets.RequestLoadedBotTypesPacket;
import com.mcmoddev.mmdbot.dashboard.util.Credentials;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public final class LoginScene {

    public static Scene createAddressSelectionScreen(final Stage stage) {
        stage.setWidth(402);
        stage.setHeight(240);
        stage.setResizable(false);
        final var ipLabel = new Label("IP: ");
        final var portLabel = new Label("Port: ");
        final var ipTextField = new TextField();
        final var portTextField = new TextField();
        final var continueButton = new Button("Continue");

        Consumer.<Label>make(l -> l.setTextFill(Color.CHARTREUSE.brighter()))
            .andThen(l -> l.setFont(Fonts.monospaced(21)))
            .acceptOnMultiple(ipLabel, portLabel);

        continueButton.setTextFill(Color.CORAL.darker().darker());
        continueButton.setFont(Fonts.monospaced(12));

        Consumer.<TextField>make(f -> StyleUtils.setRoundedCorners(f, 5))
            .andThen(f -> f.setOnKeyReleased(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    continueButton.getOnAction().handle(new ActionEvent());
                }
            }))
            .acceptOnMultiple(ipTextField, portTextField);

        StyleUtils.applyStyle(ipTextField, portTextField, continueButton);

        continueButton.setOnAction(event -> {
            final var portText = portTextField.getText();
            final var port = ExceptionFunction
                .<String, Integer, NumberFormatException>make(Integer::parseInt)
                .applyAndCatchException(portText, e -> Platform.runLater(() -> {
                    final var alert = new Alert(AlertType.ERROR);
                    alert.setContentText("Please provide a valid port!");
                    alert.setTitle("Invalid port");
                    alert.show();
                }));
            if (port == null) {
                return;
            }
            try {
                final var ip = ipTextField.getText();
                if (ip.isBlank()) throw new UnknownHostException();
                DashboardClient.setup(new InetSocketAddress(InetAddress.getByName(ip), port));
                makeLoginScene(stage);
                stage.show();
                stage.requestFocus();
            } catch (UnknownHostException e) {
                final var alert = new Alert(AlertType.ERROR);
                alert.setContentText("The IP provided is unknown!");
                alert.setTitle("Invalid IP");
                alert.show();
            }
        });

        final var vbox = new VBox(6, ipLabel, ipTextField, portLabel, portTextField, continueButton);
        vbox.setBackground(new Background(new BackgroundFill(Color.DIMGREY.darker(), null, null)));
        vbox.setPadding(new Insets(12));
        return new Scene(vbox);
    }

    private static void makeLoginScene(Stage stage) {
        final var usernameField = new TextField();
        final var passwordField = new TextField();
        final var loginBtn = new Button("Login");
        loginBtn.setOnAction(event -> DashboardClient.sendAndAwaitResponse(new CheckAuthorizedPacket(usernameField.getText(), passwordField.getText()))
            .withPlatformAction(packet -> {
                final var alert = new Alert(AlertType.INFORMATION);
                if (packet.getResponseType().isAuthorized()) {
                    alert.setTitle("Authorized");
                    alert.setContentText("You are authorized! The dashboard will open up soon.");
                    DashboardClient.credentials = new Credentials(usernameField.getText(), passwordField.getText());
                    DashboardClient.sendAndAwaitResponse(new RequestLoadedBotTypesPacket())
                        .withPlatformAction(response -> {
                            DashboardClient.botTypes = response.getTypes();
                            MainScene.makeMainPageScene(stage);
                            stage.show();
                        })
                        .withTimeout(10, TimeUnit.SECONDS)
                        .withPlatformTimeoutAction(() -> {
                            final var newAlert = new Alert(AlertType.ERROR);
                            newAlert.setContentText("Could not receive the loaded bot types!");
                            newAlert.show();
                        }).queue();
                } else {
                    alert.setAlertType(AlertType.WARNING);
                    alert.setTitle("Invalid credentials");
                    alert.setContentText("The credentials you provided are invalid.");
                }
                alert.show();
            })
            .withTimeout(20, TimeUnit.SECONDS)
            .withPlatformTimeoutAction(() -> {
                final var alert = new Alert(AlertType.ERROR);
                alert.setTitle("No response");
                alert.setHeaderText("The server did not send a response!");
                alert.setContentText("This usually means that you are trying to connect to a server that is not a dashboard.");
                alert.setWidth(120);
                alert.setHeight(120);
                alert.show();
            }).queue());
        final var vbox = new VBox(4,
            new HBox(2, new Label("Username: "), usernameField),
            new HBox(2, new Label("Password: "), passwordField),
            loginBtn);
        stage.setScene(new Scene(vbox));
    }

}
