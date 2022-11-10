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
import com.mcmoddev.mmdbot.core.util.TaskScheduler;
import joptsimple.OptionParser;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.messages.MessageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

        MessageRequest.setDefaultMentionRepliedUser(false);
        MessageRequest.setDefaultMentions(EnumSet.complementOf(EnumSet.of(Message.MentionType.HERE, Message.MentionType.EVERYONE)));
    }

    private static final Logger LOG = LoggerFactory.getLogger(RunBots.class);
    private static List<Bot> loadedBots = new ArrayList<>();

    public static void main(String[] args) {
        startBots(BotRegistry.getBotTypes(), args, true);
    }

    public static void startBots(Map<String, BotRegistry.BotRegistryEntry<?>> botTypes, String[] args, boolean withConfig) {
        System.setProperty("java.net.preferIPv4Stack", "true");

        final var parser = new OptionParser();
        final var migrateOption = parser.acceptsAll(List.of("m", "migrate"), "If to migrate bot data").withOptionalArg();
        final var disableOption = parser.acceptsAll(List.of("d", "disable"), "A list of bots to explicitly disable even if the config enables them").withOptionalArg();
        final var configOption = parser.acceptsAll(List.of("c", "config"), "A path to the configuration file").withOptionalArg().ofType(File.class);
        final var clearCommandsOption = parser.acceptsAll(List.of("clear-commands", "delete-commands", "clc"), "If the commands of the bot should be deleted before it starts").withOptionalArg();

        final var options = parser.parse(args);

        final var doMigrate = options.valueOfOptional(migrateOption)
            .map(Boolean::parseBoolean).orElse(true);
        final var config = withConfig ? getOrCreateConfig(options.valueOfOptional(configOption).map(File::toPath).orElse(Path.of("config.json"))) : new JsonObject();
        final var disabledBots = options.valuesOf(disableOption);

        final var botsAmount = new AtomicInteger();

        record BotListing<T extends Bot>(BotRegistry.BotRegistryEntry<T> registryEntry, BotEntry entry) {
        }
        record CreatedBotListing<T extends Bot>(T bot, BotEntry entry) {
        }

        var bots = botTypes
            .entrySet()
            .stream()
            .map(entry -> {
                if (withConfig) {
                    final BotEntry botEntry;
                    if (disabledBots.contains(entry.getKey())) {
                        botEntry = new BotEntry(entry.getKey(), false, entry.getKey());
                    } else {
                        botEntry = BotEntry.of(entry.getKey(),
                            config.has(entry.getKey()) ? config.get(entry.getKey()).getAsJsonObject() : new JsonObject());
                    }
                    return new BotListing<>(entry.getValue(), botEntry);
                } else {
                    return new BotListing<>(entry.getValue(), new BotEntry(entry.getKey(), true, ""));
                }
            })
            .sorted(Comparator.comparing(p -> -p.registryEntry().priority()))
            .map(listing -> new CreatedBotListing<>(
                listing.registryEntry().botType().createBot(createDirectory(listing.entry().runPath())),
                listing.entry()
            ))
            .map(botPair -> {
                final var botEntry = botPair.entry();
                final var bot = botPair.bot();
                if (botEntry.isEnabled()) {
                    if (bot != null) {
                        startBot(bot, botsAmount, doMigrate, options.has(clearCommandsOption), botEntry.name());
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

        final var botsTarget = botTypes.size();
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

    private static void startBot(Bot bot, AtomicInteger botsAmount, boolean doMigrate, boolean clearCommands, String botName) {
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
            if (clearCommands) clearCommands(bot.getToken(), botName);

            if (bot.blocksStartupThread()) {
                TaskScheduler.scheduleTask(() -> {
                    botsAmount.incrementAndGet();
                    bot.getLogger().warn("Bot {} has been found, and it has been launched!", botName);
                }, 7, TimeUnit.SECONDS); // Give the bot 7 seconds to startup... it
                // should add its listeners until then
            }
            try {
                bot.start();
            } catch (LoginException e) {
                bot.getLogger().error("Exception logging in: ", e);
            }
            botsAmount.incrementAndGet();
            bot.getLogger().warn("Bot {} has been found, and it has been launched!", botName);
        }).exceptionally(t -> {
            bot.getLogger().error("Exception starting bot up: ", t);
            botsAmount.incrementAndGet();
            return null;
        });
    }

    @SneakyThrows
    private static void clearCommands(String botToken, String botName) {
        LOG.warn("Clearing commands for bot {}...", botName);
        final JDA jda = JDABuilder.createLight(botToken)
            .setEnabledIntents(Set.of())
            .build().awaitReady();
        jda.updateCommands().complete();
        jda.getGuilds().stream()
            .map(Guild::updateCommands)
            .forEach(CommandListUpdateAction::complete);
        jda.shutdownNow();
        LOG.warn("Cleared commands for bot {}", botName);
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
                if (!(e instanceof FileAlreadyExistsException)) {
                    LOG.error("Exception while trying to create directory {}!", path, e);
                }
            }
        }
        return path;
    }

    private static JsonObject getOrCreateConfig(Path path) {
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
}
