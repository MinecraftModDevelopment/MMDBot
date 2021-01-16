package com.mcmoddev.mmdbot;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.mcmoddev.mmdbot.commands.fun.CmdCatFacts;
import com.mcmoddev.mmdbot.commands.fun.CmdToggleEventPings;
import com.mcmoddev.mmdbot.commands.fun.CmdToggleMcServerPings;
import com.mcmoddev.mmdbot.commands.info.CmdBuild;
import com.mcmoddev.mmdbot.commands.info.CmdEventsHelp;
import com.mcmoddev.mmdbot.commands.info.CmdFabricVersion;
import com.mcmoddev.mmdbot.commands.info.CmdForgeVersion;
import com.mcmoddev.mmdbot.commands.info.CmdJustAsk;
import com.mcmoddev.mmdbot.commands.info.CmdMinecraftVersion;
import com.mcmoddev.mmdbot.commands.info.CmdPaste;
import com.mcmoddev.mmdbot.commands.info.CmdSearch;
import com.mcmoddev.mmdbot.commands.info.CmdXy;
import com.mcmoddev.mmdbot.commands.info.server.CmdGuild;
import com.mcmoddev.mmdbot.commands.info.server.CmdMe;
import com.mcmoddev.mmdbot.commands.info.server.CmdReadme;
import com.mcmoddev.mmdbot.commands.info.server.CmdRoles;
import com.mcmoddev.mmdbot.commands.info.server.CmdRules;
import com.mcmoddev.mmdbot.commands.staff.CmdMute;
import com.mcmoddev.mmdbot.commands.staff.CmdUnmute;
import com.mcmoddev.mmdbot.commands.staff.CmdUser;
import com.mcmoddev.mmdbot.core.BotConfig;
import com.mcmoddev.mmdbot.events.EventReactionAdded;
import com.mcmoddev.mmdbot.events.MiscEvents;
import com.mcmoddev.mmdbot.events.users.EventNicknameChanged;
import com.mcmoddev.mmdbot.events.users.EventRoleAdded;
import com.mcmoddev.mmdbot.events.users.EventRoleRemoved;
import com.mcmoddev.mmdbot.events.users.EventUserJoined;
import com.mcmoddev.mmdbot.events.users.EventUserLeft;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

/**
 * Our Main class.
 */
public final class MMDBot {


    /**
     * The name of the bot in code.
     */
    public static final String NAME = "MMDBot";

    /**
     * The bots current version.
     * <p>
     * The version will be taken from the {@code Implementation-Version} attribute of the JAR manifest.
     * If that is unavailable, the version shall be the combination of the string {@code "DEV "} and the the current
     * date and time in {@link java.time.format.DateTimeFormatter#ISO_OFFSET_DATE_TIME}.
     */
    public static final String VERSION;

    // Gets the version from the JAR manifest, else defaults to the time the bot was started
    static {
        String version = MMDBot.class.getPackage().getImplementationVersion();
        if (version == null) {
            version = "DEV " + DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(OffsetDateTime.now(ZoneOffset.UTC));
        }
        VERSION = version;
    }

    /**
     * The issue tracker where bugs and crashes should be reported, and PR's made.
     */
    public static final String ISSUE_TRACKER = "https://github.com/MinecraftModDevelopment/MMDBot/issues/";

    /**
     * Where needed for events being fired, errors and other misc stuff, log things to console using this.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);
    private static final Set<GatewayIntent> intents = new HashSet<>();
    private static BotConfig config;
    private static JDA INSTANCE;

    static {
        intents.add(GatewayIntent.DIRECT_MESSAGES);
        intents.add(GatewayIntent.GUILD_BANS);
        intents.add(GatewayIntent.GUILD_EMOJIS);
        intents.add(GatewayIntent.GUILD_MESSAGE_REACTIONS);
        intents.add(GatewayIntent.GUILD_MESSAGES);
        intents.add(GatewayIntent.GUILD_MEMBERS);
    }

    /**
     *
     */
    private MMDBot() {
    }

    /**
     * Returns the configuration of this bot.
     *
     * @return The configuration of this bot
     */
    public static BotConfig getConfig() {
        return config;
    }

    public static JDA getInstance() {
        return INSTANCE;
    }

    /**
     * @param args Arguments provided to the program.
     */
    public static void main(final String[] args) {
        final Path configPath = Paths.get("mmdbot_config.toml");
        config = new BotConfig(configPath);
        if (config.isNewlyGenerated()) {
            LOGGER.warn("A new config file at {} has been generated. Please configure the bot and try again.", configPath);
            System.exit(0);
        } else if (config.getToken() == null) {
            LOGGER.error("No token is specified in the config. Please configure the bot and try again");
        } else if (config.getGuildID() == 0L) {
            LOGGER.error("No guild ID is configured. Please configure the bot and try again.");
            System.exit(0);
        }

        try {

            final CommandClient commandListener = new CommandClientBuilder()
                    .setOwnerId(config.getOwnerID())
                    .setPrefix(config.getMainPrefix())
                    .setAlternativePrefix(config.getAlternativePrefix())
                    .addCommand(new CmdGuild())
                    .addCommand(new CmdBuild())
                    .addCommand(new CmdMe())
                    .addCommand(new CmdUser())
                    .addCommand(new CmdRoles())
                    .addCommand(new CmdJustAsk())
                    .addCommand(new CmdPaste())
                    .addCommand(new CmdXy())
                    .addCommand(new CmdReadme())
                    .addCommand(new CmdRules())
                    .addCommand(new CmdCatFacts())
                    .addCommand(new CmdSearch("google", "https://www.google.com/search?q=", "goog"))
                    .addCommand(new CmdSearch("bing", "https://www.bing.com/search?q="))
                    .addCommand(new CmdSearch("duckduckgo", "https://duckduckgo.com/?q=", "ddg"))
                    .addCommand(new CmdSearch("lmgtfy", "https://lmgtfy.com/?q=", "let-me-google-that-for-you"))
                    .addCommand(new CmdEventsHelp())
                    .addCommand(new CmdToggleMcServerPings())
                    .addCommand(new CmdToggleEventPings())
                    .addCommand(new CmdForgeVersion())
                    .addCommand(new CmdMinecraftVersion())
                    .addCommand(new CmdFabricVersion())
                    .addCommand(new CmdMute())
                    .addCommand(new CmdUnmute())
                    .setHelpWord("help")
                    .build();

            INSTANCE = JDABuilder
                    .create(config.getToken(), intents)
                    .disableCache(CacheFlag.VOICE_STATE)
                    .disableCache(CacheFlag.ACTIVITY)
                    .disableCache(CacheFlag.CLIENT_STATUS)
                    .addEventListeners(new EventUserJoined())
                    .addEventListeners(new EventUserLeft())
                    .addEventListeners(new EventNicknameChanged())
                    .addEventListeners(new EventRoleAdded())
                    .addEventListeners(new EventRoleRemoved())
                    .addEventListeners(new EventReactionAdded())
                    .addEventListeners(new MiscEvents())
                    .addEventListeners(commandListener)
                    .build();
        } catch (final LoginException exception) {
            LOGGER.error("Error logging in the bot! Please give the bot a valid token in the config file.", exception);
            System.exit(1);
        }
    }
}
