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
package com.mcmoddev.mmdbot;

import com.mcmoddev.mmdbot.core.BotConfig;
import com.mcmoddev.mmdbot.core.References;
import com.mcmoddev.mmdbot.core.bot.Bot;
import com.mcmoddev.mmdbot.core.bot.BotType;
import com.mcmoddev.mmdbot.core.bot.RegisterBotType;
import com.mcmoddev.mmdbot.dashboard.util.BotUserData;
import com.mcmoddev.mmdbot.dashboard.util.GenericResponse;
import com.mcmoddev.mmdbot.dashboard.util.UpdateConfigContext;
import com.mcmoddev.mmdbot.modules.commands.CommandModule;
import com.mcmoddev.mmdbot.modules.logging.LoggingModule;
import com.mcmoddev.mmdbot.modules.logging.misc.MiscEvents;
import com.mcmoddev.mmdbot.utilities.ThreadedEventListener;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.database.DatabaseManager;
import com.mcmoddev.mmdbot.utilities.database.JSONDataMigrator;
import io.github.matyrobbrt.curseforgeapi.request.AsyncRequest;
import lombok.NonNull;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.AllowedMentions;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Our Main class.
 *
 * @author Antoine Gagnon
 * @author williambl
 * @author sciwhiz12
 * @author KiriCattus
 * @author jriwanek
 * @author matyrobbrt
 * @deprecated The bot is going to be split, one step at a time
 */
@Deprecated(forRemoval = false) // It is for removal, but IDEs complain so...
public final class MMDBot implements Bot {

    @RegisterBotType(name = "mmdbot", priority = -1)
    public static final BotType<MMDBot> BOT_TYPE = new BotType<>() {
        @Override
        public MMDBot createBot(final Path runPath) {
            final var configPath = runPath.resolve("mmdbot_config.toml");
            final var config = new BotConfig(configPath);
            final var db = DatabaseManager.connectSQLite("jdbc:sqlite:" + runPath.resolve("data.db"));
            return new MMDBot(config, db, runPath);
        }

        @Override
        public Logger getLogger() {
            return LOGGER;
        }
    };

    private final AtomicReference<JDA> jda = new AtomicReference<>(null);
    private final BotConfig config;
    private final DatabaseManager database;
    private final Path runPath;

    private MMDBot(final BotConfig config, final DatabaseManager database, final Path runPath) {
        this.config = config;
        this.database = database;
        this.runPath = runPath;
    }

    /**
     * Where needed for events being fired, errors and other misc stuff, log things to console using this.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(References.NAME);

    public static final Executor GENERAL_EVENT_THREAD_POOL = Executors.newFixedThreadPool(2, r -> Utils.setThreadDaemon(new Thread(r, "GeneralEventListener"), true));

    /**
     * The Constant INTENTS.
     */
    private static final Set<GatewayIntent> INTENTS = Set.of(
        GatewayIntent.DIRECT_MESSAGES,
        GatewayIntent.GUILD_BANS,
        GatewayIntent.GUILD_EMOJIS,
        GatewayIntent.GUILD_MESSAGE_REACTIONS,
        GatewayIntent.GUILD_MESSAGES,
        GatewayIntent.GUILD_MEMBERS);

    static {
        AllowedMentions.setDefaultMentionRepliedUser(false);
    }

    /**
     * The instance.
     */
    private static MMDBot instance;

    /**
     * Returns the configuration of this bot.
     *
     * @return The configuration of this bot.
     */
    public static BotConfig getConfig() {
        return instance.config;
    }

    /**
     * Gets the single JDA instance of MMDBot.
     *
     * @return JDA. instance
     */
    public static JDA getJDA() {
        return instance.jda.get();
    }

    /**
     * @return the singleton bot instance
     */
    public static MMDBot getInstance() {
        return instance;
    }

    /**
     * {@return the database manager}
     */
    public static DatabaseManager getDatabaseManager() {
        return instance.database;
    }

    /**
     * {@return the Jdbi instance from the database manager}
     *
     * @see DatabaseManager#jdbi()
     */
    public static Jdbi database() {
        return instance.database.jdbi();
    }

    @Override
    public void start(final String token) {
        instance = this;
        JSONDataMigrator.checkAndMigrate(database);

        String actualToken = token;
        if (actualToken.isEmpty()) {
            actualToken = config.getToken();
        }

        if (config.isNewlyGenerated()) {
            MMDBot.LOGGER.warn("A new config file at {} has been generated. Please configure the bot and try again.", config.getConfigPath());
            System.exit(0);
        } else if (actualToken.isEmpty()) {
            MMDBot.LOGGER.error("No token is specified in the config. Please configure the bot and try again");
            System.exit(0);
        } else if (config.getOwnerID().isEmpty()) {
            MMDBot.LOGGER.error("No owner ID is specified in the config. Please configure the bot and try again");
            System.exit(0);
        } else if (config.getGuildID() == 0L) {
            MMDBot.LOGGER.error("No guild ID is configured. Please configure the bot and try again.");
            System.exit(0);
        }

        try {
            jda.set(JDABuilder
                .create(config.getToken(), MMDBot.INTENTS)
                .disableCache(CacheFlag.VOICE_STATE)
                .disableCache(CacheFlag.ACTIVITY)
                .disableCache(CacheFlag.CLIENT_STATUS)
                .disableCache(CacheFlag.ONLINE_STATUS)
                .addEventListeners(new ThreadedEventListener(new MiscEvents(), GENERAL_EVENT_THREAD_POOL))
                .build().awaitReady());

            CommandModule.setupCommandModule();
            LoggingModule.setupLoggingModule();

            jda.get().getPresence().setActivity(Activity.of(config.getActivityType(), config.getActivityName()));
        } catch (final LoginException exception) {
            MMDBot.LOGGER.error("Error logging in the bot! Please give the bot a valid token in the config file.", exception);
            System.exit(1);
        } catch (InterruptedException e) {
            MMDBot.LOGGER.error("Error awaiting caching.", e);
            System.exit(1);
        }
    }

    @Override
    public void shutdown() {
        jda.get().shutdown();
    }

    @Override
    public BotType<?> getType() {
        return BOT_TYPE;
    }

    @Override
    public BotUserData getBotUserData() {
        final var selfUser = jda.get().getSelfUser();
        return new BotUserData(selfUser.getName(), selfUser.getDiscriminator(),
            selfUser.getAvatarUrl() == null ? selfUser.getDefaultAvatarUrl() : selfUser.getAvatarUrl());
    }

    public Path getRunPath() {
        return runPath;
    }

    // TODO: fix these config methods to work with Configurate
    @Nullable
    @Override
    public Object getConfigValue(final String configName, final String path) {
//        return config.getConfig().get(path);
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public @NonNull GenericResponse updateConfig(final UpdateConfigContext context) {
        return GenericResponse.Type.INVALID_REQUEST.noMessage();
//        config.getConfig().set(context.path(), context.newValue());
//        config.getConfig().save();
//        return GenericResponse.Type.SUCCESS.noMessage();
    }
}
