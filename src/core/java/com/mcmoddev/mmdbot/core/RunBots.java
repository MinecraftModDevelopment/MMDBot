/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * Specifically version 2.1 of the License.
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
import com.mcmoddev.mmdbot.core.common.ScamDetector;
import com.mcmoddev.mmdbot.core.event.Events;
import com.mcmoddev.mmdbot.core.util.Constants;
import com.mcmoddev.mmdbot.core.util.Pair;
import com.mcmoddev.mmdbot.core.util.TaskScheduler;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@UtilityClass
public class RunBots {

    private static final ThreadPoolExecutor BOT_STARTER_EXECUTOR;

    static {
        final var group = new ThreadGroup("Bot Threads");
        BOT_STARTER_EXECUTOR = (ThreadPoolExecutor) Executors.newFixedThreadPool(BotRegistry.getBotTypes().size(), r ->
            new Thread(group, r, "BotThread #%s".formatted(group.activeCount())));
        // Shut down inactive threads after 2 minutes, as if the thread isn't needed
        // at that point, it won't be needed again. The only bots that may freeze that
        // thread are D4J bots
        BOT_STARTER_EXECUTOR.setKeepAliveTime(2, TimeUnit.MINUTES);
        BOT_STARTER_EXECUTOR.allowCoreThreadTimeOut(true);
    }

    private static final Logger LOG = LoggerFactory.getLogger(RunBots.class);
    private static List<Bot> loadedBots = new ArrayList<>();

    public static void main(String[] a) {
        System.setProperty("java.net.preferIPv4Stack", "true");
        final var args = List.of(a);

        final var doMigrate = !args.contains("noMigration");
        final var config = getOrCreateConfig();

        final var botsAmount = new AtomicInteger();

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
                    if (bot != null) {
                        CompletableFuture.runAsync(() -> {
                            if (doMigrate) {
                                try {
                                    bot.getLogger().info("Started data migration...");
                                    bot.migrateData();
                                    bot.getLogger().info("Finished data migration.");
                                } catch (Exception e) {
                                    bot.getLogger().error("An exception occurred migrating data: ", e);
                                }
                            }
                        }, BOT_STARTER_EXECUTOR).whenComplete(($, $$) -> {
                            if (bot.blocksStartupThread()) {
                                TaskScheduler.scheduleTask(() -> {
                                    botsAmount.incrementAndGet();
                                    bot.getLogger().warn("Bot {} has been found, and it has been launched!", botEntry.name());
                                }, 5, TimeUnit.SECONDS); // Give the bot 5 seconds to startup.. it
                                // should add its listeners till then
                            }
                            try {
                                bot.start();
                            } catch (LoginException e) {
                                bot.getLogger().error("Exception logging in: ", e);
                            }
                            botsAmount.incrementAndGet();
                            bot.getLogger().warn("Bot {} has been found, and it has been launched!", botEntry.name());
                        }).exceptionally(t -> {
                            bot.getLogger().error("Exception starting bot up: ", t);
                            botsAmount.incrementAndGet();
                            return null;
                        });
                    } else {
                        LOG.warn("Bot {} was null! Skipping...", botEntry.name);
                        botsAmount.incrementAndGet();
                    }
                } else {
                    bot.getLogger().warn("Bot {} is disabled! Its features will not work!", botEntry.name());
                    botsAmount.incrementAndGet();
                }
                return botEntry.isEnabled() ? bot : null;
            })
            .filter(Objects::nonNull);

        loadedBots = bots.toList();

        final var botsTarget = BotRegistry.getBotTypes().size();
        while (botsAmount.get() < botsTarget) {
            // Block thread
        }

        if (System.getProperty("com.mcmoddev.relauncher.jar") == null) {
            // Make sure we are not running in a launcher environment.
            // We don't need to add the shutdown hook in such context as the listener will already
            // shut the bots down.
            Runtime.getRuntime().addShutdownHook(new Thread(RunBots::shutdown));
            LOG.info("It seems like the bots are not ran by ReLauncher. It is recommended to use it for easier management.");
        }

        Events.MISC_BUS.addListener(ScamDetector::onCollectTasks);
        TaskScheduler.init();
    }

    @Nonnull
    public static List<Bot> getLoadedBots() {
        return loadedBots;
    }

    public static void shutdown() {
        LOG.warn("Shutting down the bots!");
        getLoadedBots().forEach(Bot::shutdown);
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
                    Constants.Gsons.GSON.toJson(obj, fw);
                }
            } catch (IOException e) {
                LOG.error("Exception while trying to generate config!", e);
            }
        }
        try (final var ir = new FileReader(path.toFile())) {
            return Constants.Gsons.GSON.fromJson(ir, JsonObject.class);
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
