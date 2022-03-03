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
package com.mcmoddev.mmdbot.dashboard.client.scenes.bot;

import com.google.common.base.Suppliers;
import com.mcmoddev.mmdbot.dashboard.BotTypeEnum;
import com.mcmoddev.mmdbot.dashboard.client.DashboardApplication;
import com.mcmoddev.mmdbot.dashboard.client.controller.config.ConfigBoxController;
import com.mcmoddev.mmdbot.dashboard.util.BotUserData;
import com.mcmoddev.mmdbot.dashboard.util.DashConfigType;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class DefaultBotStage implements BotStage {

    private final Supplier<Map<String, List<ConfigEntry>>> configsSupplier;
    private final BotTypeEnum botType;

    public DefaultBotStage(final Supplier<Map<String, List<ConfigEntry>>> configsSupplier, final BotTypeEnum botType) {
        this.configsSupplier = configsSupplier;
        this.botType = botType;
    }

    @Override
    public void createAndShowStage(final BotUserData botUserData) {
        final var configTab = new Tab("Configs");
        configTab.setContent(Suppliers.memoize(() -> {
            final var configNamesPane = new TabPane(configsSupplier
                .get() // Create the configs
                .entrySet().stream()
                .map(e -> {
                    final var tab = new Tab(e.getKey());
                    final var box = new VBox(4);
                    box.getChildren().addAll(e.getValue().stream()
                        .sorted(Comparator.comparing(ConfigEntry::path))
                        .map(this::makeConfigBox).toList());

                    final var scrollPane = new ScrollPane(box);
                    scrollPane.setFitToHeight(true);

                    final var root = new BorderPane(scrollPane);
                    root.setPadding(new Insets(15));
                    tab.setContent(root);

                    return tab;
                }).toArray(Tab[]::new));
            configNamesPane.setSide(Side.LEFT);
            configNamesPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            configNamesPane.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
            return new VBox(configNamesPane);
        }).get());

        final var pane = new TabPane(configTab);
        pane.setMaxWidth(-1);
        final var stage = new Stage();
        pane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        pane.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
        stage.setTitle("%s configuration".formatted(botType.getNiceName()));
        stage.getIcons().add(new Image(botUserData.avatarUrl()));
        stage.setScene(new Scene(new HBox(pane)));
        stage.setResizable(true);
        stage.setFullScreen(true);
        stage.showAndWait();
    }

    public final Node makeConfigBox(ConfigEntry entry) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(DashboardApplication.class.getResource("/guis/config_box/" + entry.type().getConfigBoxName()));
            final var constructor = getConstructor(entry.type().getControllerClassName());
            final var controller = constructor.invoke(botType, entry.type(), entry.configName(), entry.path(), entry.comments());
            fxmlLoader.setController(controller);
            final Node toRet = fxmlLoader.load();
            ((ConfigBoxController) fxmlLoader.getController()).init();
            return toRet;
        } catch (Throwable e) {
            // If something is wrong, let it crash
            throw new RuntimeException(e);
        }
    }

    private static final Map<String, MethodHandle> CONSTRUCTORS = new HashMap<>();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    public static MethodHandle getConstructor(String className) {
        return CONSTRUCTORS.computeIfAbsent(className, k -> {
            try {
                final var clz = Class.forName(className);
                final var methodType = MethodType.methodType(void.class, BotTypeEnum.class, DashConfigType.class, String.class, String.class, String[].class);
                return LOOKUP.findConstructor(clz, methodType);
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
