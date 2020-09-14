package com.mcmoddev.mmdbot;

import com.google.gson.Gson;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.mcmoddev.mmdbot.commands.fun.CmdCatFacts;
import com.mcmoddev.mmdbot.commands.fun.CmdToggleMcServerPings;
import com.mcmoddev.mmdbot.commands.info.*;
import com.mcmoddev.mmdbot.commands.info.server.*;
import com.mcmoddev.mmdbot.commands.search.CmdBing;
import com.mcmoddev.mmdbot.commands.search.CmdDuckDuckGo;
import com.mcmoddev.mmdbot.commands.search.CmdGoogle;
import com.mcmoddev.mmdbot.commands.search.CmdLmgtfy;
import com.mcmoddev.mmdbot.commands.staff.CmdMute;
import com.mcmoddev.mmdbot.commands.staff.CmdUnmute;
import com.mcmoddev.mmdbot.commands.staff.CmdUser;
import com.mcmoddev.mmdbot.core.BotConfig;
import com.mcmoddev.mmdbot.events.MiscEvents;
import com.mcmoddev.mmdbot.events.users.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.nio.charset.StandardCharsets;
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
	 */
	public static final String VERSION = "3.0.1";

	/**
	 * The issue tracker where bugs and crashes should be reported, and PR's made.
	 */
	public static final String ISSUE_TRACKER = "https://github.com/MinecraftModDevelopment/MMDBot/issues/";

	/**
	 * Where needed for events being fired, errors and other misc stuff, log things to console using this.
	 */
	public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

	/**
	 *
	 */
	private static BotConfig config; // = new BotConfig("mmdbot_config.json");

	/**
	 * @return The Bots configuration.
	 */
	public static BotConfig getConfig() {
		return config;
	}

	/**
	 *
	 */
	private MMDBot() {
	}

	private static JDA INSTANCE;

	public static JDA getInstance() {
		return INSTANCE;
	}

	private static final Set<GatewayIntent> intents = new HashSet<>();
	static {
		intents.add(GatewayIntent.DIRECT_MESSAGES);
		intents.add(GatewayIntent.GUILD_BANS);
		intents.add(GatewayIntent.GUILD_EMOJIS);
		intents.add(GatewayIntent.GUILD_MESSAGE_REACTIONS);
		intents.add(GatewayIntent.GUILD_MESSAGES);
	}

	/**
	 * @param args Arguments provided to the program.
	 */
	public static void main(final String[] args) {
		final File configFile = new File("mmdbot_config.json");
		if (configFile.exists()) {
			try (InputStreamReader reader = new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8)) {
				config = new Gson().fromJson(reader, BotConfig.class);
			} catch (final IOException exception) {
				MMDBot.LOGGER.trace("Failed to read config...", exception);
			}
		} else {
			try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8)) {
				new Gson().toJson(new BotConfig(), writer);
			} catch (final FileNotFoundException exception) {
				MMDBot.LOGGER.error("An FileNotFound occurred...", exception);
			} catch (final IOException exception) {
				MMDBot.LOGGER.error("An IOException occurred...", exception);
			}
			MMDBot.LOGGER.error("New config generated. Please configurate the bot.");
			System.exit(0);
		}

		try {
			final CommandClient commandListener = new CommandClientBuilder()
				.setOwnerId(config.getOwnerID())
				.setPrefix(config.getPrefix())
				.setAlternativePrefix(getConfig().getAlternativePrefix())
				.addCommand(new CmdGuild())
				.addCommand(new CmdMe())
				.addCommand(new CmdUser())
				.addCommand(new CmdRoles())
				.addCommand(new CmdJustAsk())
				.addCommand(new CmdPaste())
				.addCommand(new CmdXy())
				.addCommand(new CmdReadme())
				.addCommand(new CmdRules())
				.addCommand(new CmdCatFacts())
				.addCommand(new CmdGoogle())
				.addCommand(new CmdBing())
				.addCommand(new CmdDuckDuckGo())
				.addCommand(new CmdLmgtfy())
				.addCommand(new CmdEventsHelp())
				.addCommand(new CmdToggleMcServerPings())
				.addCommand(new CmdForgeVersion())
				.addCommand(new CmdMute())
				.addCommand(new CmdUnmute())
				.setHelpWord("help")
				.build();

			INSTANCE = JDABuilder
				.create(config.getBotToken(), intents)
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
