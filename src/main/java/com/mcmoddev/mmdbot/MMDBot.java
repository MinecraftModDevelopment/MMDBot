package com.mcmoddev.mmdbot;

import com.jagrosh.jdautilities.command.CommandClient;
import com.mcmoddev.mmdbot.core.BotConfig;
import com.mcmoddev.mmdbot.core.References;
import com.mcmoddev.mmdbot.modules.commands.CommandModule;
import com.mcmoddev.mmdbot.modules.logging.LoggingModule;
import com.mcmoddev.mmdbot.modules.logging.misc.MiscEvents;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * Our Main class.
 *
 * @author Antoine Gagnon
 * @author williambl
 * @author sciwhiz12
 * @author ProxyNeko
 * @author jriwanek
 */
public final class MMDBot {

    /**
     * Where needed for events being fired, errors and other misc stuff, log things to console using this.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(References.NAME);

    /**
     * The Constant INTENTS.
     */
    private static final Set<GatewayIntent> INTENTS = new HashSet<>();

    /**
     * The config.
     */
    private static BotConfig config;

    /**
     * The instance.
     */
    private static JDA instance;

    static {
        MMDBot.INTENTS.add(GatewayIntent.DIRECT_MESSAGES);
        MMDBot.INTENTS.add(GatewayIntent.GUILD_BANS);
        MMDBot.INTENTS.add(GatewayIntent.GUILD_EMOJIS);
        MMDBot.INTENTS.add(GatewayIntent.GUILD_MESSAGE_REACTIONS);
        MMDBot.INTENTS.add(GatewayIntent.GUILD_MESSAGES);
        MMDBot.INTENTS.add(GatewayIntent.GUILD_MEMBERS);
    }

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
     * The main method.
     *
     * @param args Arguments provided to the program.
     */
    public static void main(final String[] args) {
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
            MMDBot.instance = JDABuilder
                .create(MMDBot.config.getToken(), MMDBot.INTENTS)
                .disableCache(CacheFlag.VOICE_STATE)
                .disableCache(CacheFlag.ACTIVITY)
                .disableCache(CacheFlag.CLIENT_STATUS)
                .disableCache(CacheFlag.ONLINE_STATUS)
                .addEventListeners(new MiscEvents())
                .build();
            CommandModule.setupCommandModule();
            LoggingModule.setupLoggingModule();
        } catch (final LoginException exception) {
            MMDBot.LOGGER.error("Error logging in the bot! Please give the bot a valid token in the config file.",
                exception);
            System.exit(1);
        }
    }

    /**
     * Instantiates a new MMD bot.
     */
    private MMDBot() {
    }
}
