package com.mcmoddev.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcmoddev.bot.handlers.CommandHandler;
import com.mcmoddev.bot.handlers.LoggingHandler;
import com.mcmoddev.bot.handlers.ServerEventHandler;
import com.mcmoddev.bot.handlers.ZalgoHandler;
import com.mcmoddev.bot.logging.PrintStreamTraced;
import com.mcmoddev.bot.util.Utilities;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

public class MMDBot {

    // HANDLERS

    private static final LoggingHandler logging = new LoggingHandler();

    private static final CommandHandler commands = new CommandHandler();

    private static final ServerEventHandler auditor = new ServerEventHandler();

    private static final ZalgoHandler zalgo = new ZalgoHandler();

    public static final Logger LOG = LoggerFactory.getLogger("MMDBot");

    public static final String COMMAND_KEY = "!mmd";

    public static IDiscordClient instance;

    public static IGuild mmdGuild;

    public static IChannel botZone;

    public static IChannel events;

    public static IChannel console;

    public static boolean isReady = false;

    public static void main (String... args) throws RateLimitException {

        LOG.info("The bot has started.");

        LOG.info("Wrapping standard output and error streams with tracer!");
        System.setOut(new PrintStreamTraced(System.out));
        System.setErr(new PrintStreamTraced(System.err));

        if (args.length >= 1) {

            LOG.info("Starting bot with token " + Utilities.partiallyReplace(args[0], 4));
            
            try {

                instance = new ClientBuilder().withToken(args[0]).login();
                instance.getDispatcher().registerListener(commands);
                instance.getDispatcher().registerListener(auditor);
                instance.getDispatcher().registerListener(zalgo);
            }

            catch (final DiscordException e) {

                LOG.trace("Error during startup", e);
            }
        }
        else
            LOG.error("Attempted to launch the bot without a discord token!");
    }
}