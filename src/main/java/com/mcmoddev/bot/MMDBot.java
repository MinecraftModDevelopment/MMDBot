package com.mcmoddev.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcmoddev.bot.handlers.CommandHandler;
import com.mcmoddev.bot.handlers.LoggingHandler;
import com.mcmoddev.bot.handlers.ScheduleHandler;
import com.mcmoddev.bot.handlers.ServerEventHandler;
import com.mcmoddev.bot.handlers.StateHandler;
import com.mcmoddev.bot.logging.PrintStreamTraced;
import com.mcmoddev.bot.util.Utilities;

import ch.qos.logback.classic.Level;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

public class MMDBot {

    private static final LoggingHandler LOGGING = new LoggingHandler();

    public static final Logger LOG = LoggerFactory.getLogger("MMDBot");

    public static final LaunchConfig config = LaunchConfig.updateConfig();

    public static final StateHandler state = new StateHandler();

    private static final CommandHandler commands = new CommandHandler();

    private static final ServerEventHandler auditor = new ServerEventHandler();

    private static final ScheduleHandler schedule = new ScheduleHandler();

    public static IDiscordClient instance;

    public static void main (String... args) throws RateLimitException {

        LOG.info("Wrapping standard output and error streams with tracer!");
        System.setOut(new PrintStreamTraced(System.out));
        System.setErr(new PrintStreamTraced(System.err));

        LOG.info("Shutting Discord4J's Yap");
        LoggingHandler.setLoggerLevel((ch.qos.logback.classic.Logger) Discord4J.LOGGER, Level.OFF);

        LOG.info("Starting bot with token " + Utilities.partiallyReplace(config.authToken, 4));

        try {

            instance = new ClientBuilder().withToken(config.authToken).login();
            instance.getDispatcher().registerListener(state);
            instance.getDispatcher().registerListener(commands);
            instance.getDispatcher().registerListener(auditor);
        }

        catch (final DiscordException e) {

            LOG.trace("Error during startup", e);
        }
    }
}