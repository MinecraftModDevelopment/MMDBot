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
