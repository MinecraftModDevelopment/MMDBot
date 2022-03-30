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
package com.mcmoddev.mmdbot.watcher;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.mcmoddev.mmdbot.core.bot.Bot;
import com.mcmoddev.mmdbot.core.bot.BotRegistry;
import com.mcmoddev.mmdbot.core.bot.BotType;
import com.mcmoddev.mmdbot.core.bot.RegisterBotType;
import com.mcmoddev.mmdbot.core.commands.CommandUpserter;
import com.mcmoddev.mmdbot.core.util.ConfigurateUtils;
import com.mcmoddev.mmdbot.core.util.DotenvLoader;
import com.mcmoddev.mmdbot.core.util.ReflectionsUtils;
import com.mcmoddev.mmdbot.core.util.event.DismissListener;
import com.mcmoddev.mmdbot.core.util.event.ThreadedEventListener;
import com.mcmoddev.mmdbot.dashboard.util.BotUserData;
import com.mcmoddev.mmdbot.watcher.util.Configuration;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.matyrobbrt.curseforgeapi.util.Utils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.utils.AllowedMentions;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.sqlite.SQLiteDataSource;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class TheWatcher implements Bot {

    public static final Logger LOGGER = LoggerFactory.getLogger("TheWatcher");

    @RegisterBotType(name = BotRegistry.THE_WATCHER_NAME)
    public static final BotType<TheWatcher> BOT_TYPE = new BotType<>() {
        @Override
        public TheWatcher createBot(final Path runPath) {
            try {
                return new TheWatcher(runPath, DotenvLoader.builder()
                    .filePath(runPath.resolve(".env"))
                    .whenCreated(writer -> writer
                        .writeComment("The token of the bot: ")
                        .writeValue("BOT_TOKEN", "")
                    )
                    .load());
            } catch (IOException e) {
                LOGGER.error("Could not load the .env file due to an IOException: ", e);
            }
            return null;
        }

        @Override
        public Logger getLogger() {
            return LOGGER;
        }
    };

    public static final ThreadGroup THREAD_GROUP = new ThreadGroup("The Watcher");

    public static final ThreadedEventListener COMMANDS_LISTENER = Utils.makeWithSupplier(() -> {
        final var group = new ThreadGroup(THREAD_GROUP, "Command Listeners");
        final var poll = (ThreadPoolExecutor) Executors.newFixedThreadPool(2, r ->
            com.mcmoddev.mmdbot.core.util.Utils.setThreadDaemon(new Thread(group, r, "CommandListener #%s".formatted(group.activeCount())),
                true));
        poll.setKeepAliveTime(30, TimeUnit.MINUTES);
        poll.allowCoreThreadTimeOut(true);
        return new ThreadedEventListener(poll);
    });

    private static final Set<GatewayIntent> INTENTS = Set.of(
        GatewayIntent.DIRECT_MESSAGES,
        GatewayIntent.GUILD_BANS,
        GatewayIntent.GUILD_EMOJIS,
        GatewayIntent.GUILD_MESSAGE_REACTIONS,
        GatewayIntent.GUILD_MESSAGES,
        GatewayIntent.GUILD_MEMBERS);

    private static final Set<Message.MentionType> DEFAULT_MENTIONS = EnumSet.of(
        Message.MentionType.EMOTE,
        Message.MentionType.CHANNEL);

    static {
        AllowedMentions.setDefaultMentionRepliedUser(false);
    }

    /**
     * The instance.
     */
    private static TheWatcher instance;

    public static TheWatcher getInstance() {
        return instance;
    }

    public static JDA getJDA() {
        return getInstance().getJda();
    }

    private JDA jda;
    private Jdbi jdbi;
    private CommandClient commandClient;
    private Configuration config;
    private ConfigurationReference<CommentedConfigurationNode> configRef;
    private final Dotenv dotenv;
    private final Path runPath;

    public TheWatcher(final Path runPath, final Dotenv dotenv) {
        this.dotenv = dotenv;
        this.runPath = runPath;
    }

    @Override
    public void start() {
        instance = this;

        try {
            final var configPath = runPath.resolve("config.conf");
            final HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                .emitComments(true)
                .prettyPrinting(true)
                .path(configPath)
                .build();
            final var cPair =
                ConfigurateUtils.loadConfig(loader, configPath, c -> config = c, Configuration.class, Configuration.EMPTY);
            configRef = cPair.second();
            config = cPair.first().get();

        } catch (ConfigurateException e) {
            LOGGER.error("Exception while trying to load general config", e);
            throw new RuntimeException(e);
        }

        // Setup database
        {
            final var dbPath = getRunPath().resolve("data.db");
            if (!Files.exists(dbPath)) {
                try {
                    Files.createFile(dbPath);
                } catch (IOException e) {
                    throw new RuntimeException("Exception creating database!", e);
                }
            }
            final var url = "jdbc:sqlite:" + dbPath;
            SQLiteDataSource dataSource = new SQLiteDataSource();
            dataSource.setUrl(url);
            dataSource.setDatabaseName(BotRegistry.THE_WATCHER_NAME);

            final var flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:watcher/db")
                .load();
            flyway.migrate();

            jdbi = Jdbi.create(dataSource);
        }

        if (config.bot().getOwners().isEmpty()) {
            LOGGER.warn("Please provide at least one bot owner!");
            throw new RuntimeException();
        }
        final var coOwners = config.bot().getOwners().subList(1, config.bot().getOwners().size());

        commandClient = new CommandClientBuilder()
            .setOwnerId(config.bot().getOwners().get(0))
            .setCoOwnerIds(coOwners.toArray(String[]::new))
            .setPrefixes(config.bot().getPrefixes().toArray(String[]::new))
            .setManualUpsert(true)
            .useHelpBuilder(false)
            .setActivity(null)
            .build();
        COMMANDS_LISTENER.addListener((EventListener) commandClient);

        final var upserter = new CommandUpserter(commandClient, config.bot().areCommandsForcedGuildOnly(),
            config.bot().guild());
        COMMANDS_LISTENER.addListener(upserter);

        // Buttons
        COMMANDS_LISTENER.addListener(new DismissListener());

        MessageAction.setDefaultMentionRepliedUser(false);
        MessageAction.setDefaultMentions(DEFAULT_MENTIONS);

        try {
            final var builder = JDABuilder
                .create(dotenv.get("BOT_TOKEN"), INTENTS)
                .addEventListeners(listenerConsumer((ReadyEvent event) ->
                    getLogger().warn("The Watcher is ready to work! Logged in as {}", event.getJDA().getSelfUser().getAsTag())
                ), COMMANDS_LISTENER)
                .disableCache(CacheFlag.CLIENT_STATUS)
                .disableCache(CacheFlag.ONLINE_STATUS)
                .disableCache(CacheFlag.VOICE_STATE)
                .disableCache(CacheFlag.ACTIVITY)
                .setEnabledIntents(INTENTS);
            jda = builder.build().awaitReady();
        } catch (final LoginException exception) {
            LOGGER.error("Error logging in the bot! Please give the bot a valid token in the config file.", exception);
            System.exit(1);
        } catch (InterruptedException e) {
            LOGGER.error("Error awaiting caching.", e);
            System.exit(1);
        }
    }

    @Override
    public void shutdown() {
        jda.shutdown();
    }

    @Override
    public BotType<?> getType() {
        return BOT_TYPE;
    }

    public Dotenv getDotenv() {
        return dotenv;
    }

    public Path getRunPath() {
        return runPath;
    }

    public JDA getJda() {
        return jda;
    }

    @Override
    public BotUserData getBotUserData() {
        final var selfUser = jda.getSelfUser();
        return new BotUserData(selfUser.getName(), selfUser.getDiscriminator(),
            selfUser.getAvatarUrl() == null ? selfUser.getDefaultAvatarUrl() : selfUser.getAvatarUrl());
    }

    public Configuration getConfig() {
        return config;
    }

    public Jdbi getJdbi() {
        return jdbi;
    }

    public CommandClient getCommandClient() {
        return commandClient;
    }

    @SuppressWarnings("unchecked")
    private static <E extends Event> EventListener listenerConsumer(final Consumer<E> listener) {
        return event -> {
            final var type = (Class<E>) net.jodah.typetools.TypeResolver.resolveRawArgument(Consumer.class, listener.getClass());
            if (type.isInstance(event)) {
                listener.accept((E) event);
            }
        };
    }
}
