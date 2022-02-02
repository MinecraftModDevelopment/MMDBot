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
import com.mcmoddev.mmdbot.modules.commands.CommandModule;
import com.mcmoddev.mmdbot.modules.logging.LoggingModule;
import com.mcmoddev.mmdbot.modules.logging.misc.MiscEvents;
import com.mcmoddev.mmdbot.utilities.ThreadedEventListener;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.database.DatabaseManager;
import com.mcmoddev.mmdbot.utilities.database.JSONDataMigrator;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Our Main class.
 *
 * @author Antoine Gagnon
 * @author williambl
 * @author sciwhiz12
 * @author KiriCattus
 * @author jriwanek
 */
public final class MMDBot {

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

    /**
     * The config.
     */
    private static BotConfig config;

    /**
     * The instance.
     */
    private static JDA instance;

    /**
     * The database manager.
     */
    private static DatabaseManager database;

    /**
     * Returns the configuration of this bot.
     *
     * @return The configuration of this bot.
     */
    public static BotConfig getConfig() {
        return MMDBot.config;
    }

    /**
     * Gets the single instance of MMDBot.
     *
     * @return JDA. instance
     */
    public static JDA getInstance() {
        return MMDBot.instance;
    }

    /**
     * {@return the database manager}
     */
    public static DatabaseManager getDatabaseManager() {
        return database;
    }

    /**
     * {@return the Jdbi instance from the database manager}
     *
     * @see DatabaseManager#jdbi()
     */
    public static Jdbi database() {
        return database.jdbi();
    }

    /**
     * The main method.
     *
     * @param args Arguments provided to the program.
     */
    public static void main(final String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> LOGGER.warn("The bot is shutting down!")));
        final var configPath = Paths.get("mmdbot_config.toml");
        MMDBot.config = new BotConfig(configPath);
        if (MMDBot.config.isNewlyGenerated()) {
            MMDBot.LOGGER.warn("A new config file at {} has been generated. Please configure the bot and try again.",
                configPath);
            System.exit(0);
        } else if (MMDBot.config.getToken().isEmpty()) {
            MMDBot.LOGGER.error("No token is specified in the config. Please configure the bot and try again");
            System.exit(0);
        } else if (MMDBot.config.getOwnerID().isEmpty()) {
            MMDBot.LOGGER.error("No owner ID is specified in the config. Please configure the bot and try again");
            System.exit(0);
        } else if (MMDBot.config.getGuildID() == 0L) {
            MMDBot.LOGGER.error("No guild ID is configured. Please configure the bot and try again.");
            System.exit(0);
        }

        try {
            MMDBot.database = DatabaseManager.connectSQLite("jdbc:sqlite:./data.db");
            JSONDataMigrator.checkAndMigrate(MMDBot.database);
            MMDBot.instance = JDABuilder
                .create(MMDBot.config.getToken(), MMDBot.INTENTS)
                .disableCache(CacheFlag.VOICE_STATE)
                .disableCache(CacheFlag.ACTIVITY)
                .disableCache(CacheFlag.CLIENT_STATUS)
                .disableCache(CacheFlag.ONLINE_STATUS)
                .addEventListeners(new ThreadedEventListener(new MiscEvents(), GENERAL_EVENT_THREAD_POOL))
                .build().awaitReady();
            CommandModule.setupCommandModule();
            LoggingModule.setupLoggingModule();

            MMDBot.getInstance().getPresence().setActivity(Activity.of(config.getActivityType(), config.getActivityName()));
        } catch (final LoginException exception) {
            MMDBot.LOGGER.error("Error logging in the bot! Please give the bot a valid token in the config file.",
                exception);
            System.exit(1);
        } catch (InterruptedException e) {
            MMDBot.LOGGER.error("Error awaiting caching.", e);
            System.exit(1);
        }
    }

    /**
     * Instantiates a new MMD bot.
     */
    private MMDBot() {
    }
}
