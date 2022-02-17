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
package com.mcmoddev.mmdbot.core;

import com.google.gson.JsonObject;
import com.mcmoddev.mmdbot.core.bot.Bot;
import com.mcmoddev.mmdbot.core.bot.BotRegistry;
import com.mcmoddev.mmdbot.core.util.Constants;
import com.mcmoddev.mmdbot.core.util.Pair;
import com.mcmoddev.mmdbot.dashboard.BotTypeEnum;
import com.mcmoddev.mmdbot.dashboard.ServerBridge;
import com.mcmoddev.mmdbot.dashboard.common.listener.PacketListener;
import com.mcmoddev.mmdbot.dashboard.server.DashboardSever;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@UtilityClass
public class RunBots {

    private static final Logger LOG = LoggerFactory.getLogger(RunBots.class);
    private static List<Bot> loadedBots = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {

        final var config = getOrCreateConfig();

        var bots = BotRegistry.getBotTypes()
            .entrySet()
            .stream()
            .map(entry -> {
                final var botEntry = BotEntry.of(entry.getKey(),
                    config.has(entry.getKey()) ? config.get(entry.getKey()).getAsJsonObject() : new JsonObject());
                return Pair.of(entry.getValue(), botEntry);
            })
            .sorted(Comparator.comparing(p -> -p.first().priority()))
            .map(entry -> entry.mapFirst(type -> type.botType().createBot(createDirectory(entry.second().runPath()))))
            .map(botPair -> {
                final var botEntry = botPair.second();
                final var bot = botPair.first();
                if (botEntry.isEnabled()) {
                    bot.start();
                    bot.getLogger().warn("Bot {} has been found, and it has been launched!", botEntry.name());
                } else {
                    bot.getLogger().warn("Bot {} is disabled! Its features will not work!", botEntry.name());
                }
                return botEntry.isEnabled() ? bot : null;
            })
            .filter(Objects::nonNull);

        loadedBots = bots.toList();
        bots = loadedBots.stream();

        // dashboard stuff
        {
            ServerBridge.setInstance(new ServerBridgeImpl());
            final var dashConfig = getDashboardConfig();
            try {
                // TODO get the public ipv4 address
                final var ip = InetAddress.getByName("localhost");
                final var address = new InetSocketAddress(ip, dashConfig.port);
                final var listeners = new ArrayList<PacketListener>();
                bots.map(b -> b.getType().getPacketListenerUnsafe(b)).forEach(listeners::add);
                DashboardSever.setup(address, listeners.toArray(PacketListener[]::new));
            } catch (Exception e) {
                LOG.error("Error while trying to set up the dashboard endpoint!", e);
            }
        }
    }

    public static List<Bot> getLoadedBots() {
        return loadedBots;
    }

    public static boolean isBotLoaded(BotTypeEnum botTypeEnum) {
        return loadedBots.stream().map(b -> BotRegistry.getBotTypeName(b.getType()))
            .anyMatch(s -> s.equals(botTypeEnum.getName()));
    }

    public static void shutdownBot(BotTypeEnum botTypeEnum) {
        loadedBots.stream().map(b -> Pair.of(b, BotRegistry.getBotTypeName(b.getType())))
            .filter(p -> p.second().equals(botTypeEnum.getName()))
            .map(Pair::first)
            .findAny()
            .ifPresent(Bot::shutdown);
    }

    private static Path createDirectory(String path) {
        return createDirectory(Path.of(path));
    }

    private static Path createDirectory(Path path) {
        if (!path.toFile().exists()) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                LOG.error("Exception while trying to create directory {}!", path, e);
            }
        }
        return path;
    }

    private static Path getPathOrElse(JsonObject json, String name, Path orElse) {
        if (json.has(name) && json.get(name).isJsonPrimitive() && json.get(name).getAsJsonPrimitive().isString()) {
            return Path.of(json.get(name).getAsString());
        }
        return orElse;
    }

    private static JsonObject getOrCreateConfig() {
        final var path = Path.of("config.json");
        if (!path.toFile().exists()) {
            try {
                Files.createFile(path); // If it doesn't exist, generate it
                try (final var fw = new BufferedWriter(new FileWriter(path.toFile()))) {
                    final var obj = new JsonObject();
                    BotRegistry.getBotTypes()
                        .keySet().forEach(n -> {
                            final var botObj = new JsonObject();
                            botObj.addProperty("enabled", true);
                            botObj.addProperty("runPath", n);
                            obj.add(n, botObj);
                        }); // Write all the known bots when generating it
                    Constants.GSON.toJson(obj, fw);
                }
            } catch (IOException e) {
                LOG.error("Exception while trying to generate config!", e);
            }
        }
        try (final var ir = new FileReader(path.toFile())) {
            return Constants.GSON.fromJson(ir, JsonObject.class);
        } catch (IOException e) {
            LOG.error("Exception while trying to read config!", e);
        }
        return new JsonObject();
    }

    private record BotEntry(String name, boolean isEnabled, String runPath) {
        public static BotEntry of(String name, JsonObject json) {
            if (json == null) {
                json = new JsonObject();
            }
            final var enabled = json.has("enabled") && json.get("enabled").getAsBoolean();
            final var runPath = json.has("runPath") ? json.get("runPath").getAsString() : name;
            return new BotEntry(name, enabled, runPath);
        }
    }

    public static DashboardConfig getDashboardConfig() {
        final var path = Path.of("dashboard").resolve("config.json");
        if (!path.toFile().exists()) {
            try {
                Files.createDirectories(Path.of("dashboard"));
                Files.createFile(path); // If it doesn't exist, generate it
                try (final var fw = new BufferedWriter(new FileWriter(path.toFile()))) {
                    Constants.GSON.toJson(new DashboardConfig(), fw);
                }
            } catch (IOException e) {
                LOG.error("Exception while trying to generate dashboard config!", e);
            }
        }
        try (final var ir = new FileReader(path.toFile())) {
            return Constants.GSON.fromJson(ir, DashboardConfig.class);
        } catch (IOException e) {
            LOG.error("Exception while trying to read config!", e);
        }
        return new DashboardConfig();
    }

    public static final class DashboardConfig {
        public int port = 600;

        public Account[] accounts;

        public static final class Account {
            public String username;
            public String password;
        }
    }

}
