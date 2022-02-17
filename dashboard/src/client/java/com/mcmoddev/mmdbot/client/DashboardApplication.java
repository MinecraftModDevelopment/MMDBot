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
package com.mcmoddev.mmdbot.client;

import com.mcmoddev.mmdbot.client.scenes.LoginScene;
import com.sun.javafx.css.StyleManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import static com.mcmoddev.mmdbot.client.util.Checks.notNull;

public class DashboardApplication extends Application {

    @Override
    public void start(final Stage primaryStage) throws Exception {
        Application.setUserAgentStylesheet(Application.STYLESHEET_CASPIAN);
        StyleManager.getInstance().addUserAgentStylesheet(notNull(getClass().getResource("/themes/dark.css"), "themeStyle").toExternalForm());

        final Scene loginScene = LoginScene.createAddressSelectionScreen(primaryStage);
        primaryStage.setTitle("MMDBot Dashboard");
        primaryStage.getIcons().add(new Image(notNull(getClass().getResourceAsStream("/icon.png"), "icon")));
        primaryStage.setScene(loginScene);
        primaryStage.requestFocus();
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        DashboardClient.shutdown();
    }
}
