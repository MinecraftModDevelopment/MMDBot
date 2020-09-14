package com.mcmoddev.mmdbot;

import com.google.gson.Gson;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.mcmoddev.mmdbot.commands.fun.CmdCatFacts;
import com.mcmoddev.mmdbot.commands.fun.CmdToggleMcServerPings;
import com.mcmoddev.mmdbot.commands.info.CmdEventsHelp;
import com.mcmoddev.mmdbot.commands.info.CmdForgeVersion;
import com.mcmoddev.mmdbot.commands.info.CmdJustAsk;
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
import com.mcmoddev.mmdbot.events.MiscEvents;
import com.mcmoddev.mmdbot.events.users.EventNicknameChanged;
import com.mcmoddev.mmdbot.events.users.EventReactionAdded;
import com.mcmoddev.mmdbot.events.users.EventRoleAdded;
import com.mcmoddev.mmdbot.events.users.EventRoleRemoved;
import com.mcmoddev.mmdbot.events.users.EventUserJoined;
import com.mcmoddev.mmdbot.events.users.EventUserLeft;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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

	private static Set<GatewayIntent> intents = new HashSet<>();
	static {
		intents.add(GatewayIntent.DIRECT_MESSAGES);
		intents.add(GatewayIntent.GUILD_BANS);
		intents.add(GatewayIntent.GUILD_EMOJIS);
		intents.add(GatewayIntent.GUILD_MEMBERS);
		intents.add(GatewayIntent.GUILD_MESSAGE_REACTIONS);
		intents.add(GatewayIntent.GUILD_MESSAGES);
		intents.add(GatewayIntent.GUILD_PRESENCES);
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
			MMDBot.LOGGER.error("New Config generated please edit and add a bot token to the config and start the bot.");
			System.exit(0);
			//throw new RuntimeException(null, null);
		}

		try {
			final JDABuilder botBuilder = new JDABuilder(AccountType.BOT).setToken(config.getBotToken());

			botBuilder.addEventListeners(new EventUserJoined());
			botBuilder.addEventListeners(new EventUserLeft());
			botBuilder.addEventListeners(new EventNicknameChanged());
			botBuilder.addEventListeners(new EventRoleAdded());
			botBuilder.addEventListeners(new EventRoleRemoved());
			botBuilder.addEventListeners(new EventReactionAdded());
			botBuilder.addEventListeners(new MiscEvents());

			final CommandClientBuilder commandBuilder = new CommandClientBuilder();

			commandBuilder.setOwnerId(config.getOwnerID());
			commandBuilder.setPrefix(config.getPrefix());
			commandBuilder.setAlternativePrefix(getConfig().getAlternativePrefix());
			commandBuilder.addCommand(new CmdGuild());
			commandBuilder.addCommand(new CmdMe());
			commandBuilder.addCommand(new CmdUser());
			commandBuilder.addCommand(new CmdRoles());
			commandBuilder.addCommand(new CmdJustAsk());
			commandBuilder.addCommand(new CmdPaste());
			commandBuilder.addCommand(new CmdXy());
			commandBuilder.addCommand(new CmdReadme());
			commandBuilder.addCommand(new CmdRules());
			commandBuilder.addCommand(new CmdCatFacts());
			commandBuilder.addCommand(new CmdSearch("Google", "https://www.google.com/search?q=", "goog"));
			commandBuilder.addCommand(new CmdSearch("Bing", "https://www.bing.com/search?q="));
			commandBuilder.addCommand(new CmdSearch("DuckDuckGo", "https://duckduckgo.com/?q=", "ddg"));
			commandBuilder.addCommand(new CmdSearch("LMGTFY", "https://lmgtfy.com/?q=", "let-me-google-that-for-you"));
			commandBuilder.addCommand(new CmdEventsHelp());
			commandBuilder.addCommand(new CmdToggleMcServerPings());
			commandBuilder.addCommand(new CmdForgeVersion());
			commandBuilder.addCommand(new CmdMute());
			commandBuilder.addCommand(new CmdUnmute());
			commandBuilder.setHelpWord("help");

			final CommandClient commandListener = commandBuilder.build();
			botBuilder.addEventListeners(commandListener);
			botBuilder.enableIntents(intents);

			INSTANCE = botBuilder.build();
		} catch (final LoginException exception) {
			LOGGER.error("Error logging in the bot! Please give the bot a valid token in the config file.", exception);
			System.exit(1);
			//throw new RuntimeException(null, null);
		}
	}
}
