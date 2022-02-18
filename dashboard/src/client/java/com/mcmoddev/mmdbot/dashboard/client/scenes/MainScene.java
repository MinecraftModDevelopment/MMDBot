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
package com.mcmoddev.mmdbot.dashboard.client.scenes;

import com.mcmoddev.mmdbot.dashboard.client.DashboardClient;
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
