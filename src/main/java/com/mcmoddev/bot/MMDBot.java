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

    // Handlers

    private static final LoggingHandler logging = new LoggingHandler();

    public static final StateHandler state = new StateHandler();

    private static final CommandHandler commands = new CommandHandler();

    private static final ServerEventHandler auditor = new ServerEventHandler();

    private static final ScheduleHandler schedule = new ScheduleHandler();

    // Other

    public static final Logger LOG = LoggerFactory.getLogger("MMDBot");

    public static final String COMMAND_KEY = "!mmd";

    public static IDiscordClient instance;

    public static void main (String... args) throws RateLimitException {

        LOG.info("The bot has started.");

        LOG.info("Wrapping standard output and error streams with tracer!");
        System.setOut(new PrintStreamTraced(System.out));
        System.setErr(new PrintStreamTraced(System.err));

        LOG.info("Shutting Discord4J's Yap");
        LoggingHandler.setLoggerLevel((ch.qos.logback.classic.Logger) Discord4J.LOGGER, Level.OFF);

        if (args.length >= 1) {

            LOG.info("Starting bot with token " + Utilities.partiallyReplace(args[0], 4));

            try {

                instance = new ClientBuilder().withToken(args[0]).login();
                instance.getDispatcher().registerListener(state);
                instance.getDispatcher().registerListener(commands);
                instance.getDispatcher().registerListener(auditor);
            }

            catch (final DiscordException e) {

                LOG.trace("Error during startup", e);
            }
        }
        else
            LOG.error("Attempted to launch the bot without a discord token!");
    }
}