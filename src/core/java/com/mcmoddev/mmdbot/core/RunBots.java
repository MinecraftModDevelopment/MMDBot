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
import com.mcmoddev.mmdbot.core.bot.BotType;
import com.mcmoddev.mmdbot.core.util.Constants;
import com.mcmoddev.mmdbot.core.util.Pair;
import com.mcmoddev.mmdbot.core.util.Utils;
import com.mcmoddev.mmdbot.dashboard.BotTypeEnum;
import com.mcmoddev.mmdbot.dashboard.ServerBridge;
import com.mcmoddev.mmdbot.dashboard.common.listener.PacketListener;
import com.mcmoddev.mmdbot.dashboard.server.DashboardSever;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
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
import java.util.Map;
import java.util.Optional;

@UtilityClass
public class RunBots {

    private static final Logger LOG = LoggerFactory.getLogger(RunBots.class);
    private static List<Bot> loadedBots = new ArrayList<>();

    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "true");

        final BotsConfig botsConfig = new BotsConfig(Path.of("bots.conf"), BotRegistry.getBotTypes());

        final List<String> sortedNames = BotRegistry.getBotTypes().entrySet().stream()
            .sorted(Comparator.comparing(s -> -s.getValue().priority()))
            .map(Map.Entry::getKey)
            .toList();

        final List<Bot> enabledBots = new ArrayList<>();

        for (String name : sortedNames) {
            final BotRegistry.BotRegistryEntry<?> entry = BotRegistry.getBotTypes().get(name);
            final BotType<?> botType = entry.botType();

            final Optional<Path> pathOpt = botsConfig.getRunPath(name);
            if (pathOpt.isEmpty()) {
                botType.getLogger().warn("Bot {} has no configured run path, skipping", name);
                continue;
            }
            final Path path = pathOpt.get();
            final Bot botInstance = botType.createBot(path);

            if (botInstance == null) {
                botType.getLogger().warn("Bot type {} returned a null instance, skipping", name);
                continue;
            }

            if (!botsConfig.getEnabled(name).orElse(Boolean.FALSE)) {
                botType.getLogger().warn("Bot {} is disabled, skipping", name);
                continue;
            }

            final Optional<String> tokenOpt = botsConfig.getToken(name);
            if (tokenOpt.isEmpty()) {
                botType.getLogger().warn("Bot {} is enabled yet has no configured token, skipping", name);
                continue;
            }
            final String token = tokenOpt.get();

            botInstance.start(token);

            enabledBots.add(botInstance);
        }

        loadedBots = enabledBots;
        var bots = loadedBots.stream();

        // dashboard stuff
        {
            ServerBridge.setInstance(new ServerBridgeImpl());
            final var dashConfig = getDashboardConfig();
            try {
                // TODO get the public ipv4 address
                final var address = new InetSocketAddress("0.0.0.0", dashConfig.port);
                final var listeners = new ArrayList<PacketListener>();
                bots.map(b -> b.getType().getPacketListenerUnsafe(b)).forEach(listeners::add);
                DashboardSever.setup(address, listeners.toArray(PacketListener[]::new));
            } catch (Exception e) {
                LOG.error("Error while trying to set up the dashboard endpoint!", e);
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> LOG.warn("The bot(s) are shutting down!")));
    }

    public static List<Bot> getLoadedBots() {
        return loadedBots;
    }

    public static boolean isBotLoaded(BotTypeEnum botTypeEnum) {
        return loadedBots.stream().map(b -> BotRegistry.getBotTypeName(b.getType()))
            .anyMatch(s -> s.equals(botTypeEnum.getName()));
    }

    @Nullable
    public static Bot getBotByType(BotTypeEnum botTypeEnum) {
        return loadedBots.stream().filter(b -> BotRegistry.getBotTypeName(b.getType()).equals(botTypeEnum.getName()))
            .findFirst().orElse(null);
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
        if (!Files.exists(path)) {
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

    public static DashboardConfig getDashboardConfig() {
        final var path = Path.of("dashboard").resolve("config.json");
        if (!path.toFile().exists()) {
            try {
                Files.createDirectories(Path.of("dashboard"));
                Files.createFile(path); // If it doesn't exist, generate it
                try (final var fw = new BufferedWriter(new FileWriter(path.toFile()))) {
                    Constants.Gsons.GSON.toJson(new DashboardConfig(), fw);
                }
            } catch (IOException e) {
                LOG.error("Exception while trying to generate dashboard config!", e);
            }
        }
        try (final var ir = new FileReader(path.toFile())) {
            return Constants.Gsons.GSON.fromJson(ir, DashboardConfig.class);
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
