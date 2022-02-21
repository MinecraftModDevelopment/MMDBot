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
import com.mcmoddev.mmdbot.dashboard.client.scenes.bot.BotStage;
import com.mcmoddev.mmdbot.dashboard.client.util.StyleUtils;
import com.mcmoddev.mmdbot.dashboard.packets.requests.RequestBotUserDataPacket;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MainScene {

    public static void makeMainPageScene(Stage stage) {
        final var botNodes = new ArrayList<Node>();
        DashboardClient.botTypes.forEach(botType -> {
            DashboardClient.sendAndAwaitResponse(new RequestBotUserDataPacket(botType))
                .withAction(response -> {
                    final var data = response.data();
                    final var image = new ImageView(data.avatarUrl());
                    image.setFitWidth(50);
                    image.setFitHeight(50);
                    final var nameText = new Text(data.username() + "#" + data.discriminator());
                    final var box = new HBox(7, image, nameText);
                    box.setOnMouseClicked(e -> BotStage.STAGES.get(botType).createAndShowStage(data));
                    botNodes.add(box);
                })
                .withTimeout(10, TimeUnit.SECONDS)
                .queueAndBlockNoException(true);
        });
        final var vbox = new VBox(6);
        vbox.getChildren().addAll(botNodes);
        final var scene = new Scene(vbox);
        stage.setResizable(true);
        stage.setWidth(scene.getWidth());
        stage.setHeight(scene.getHeight());
        stage.setScene(scene);
        stage.show();
    }

}
