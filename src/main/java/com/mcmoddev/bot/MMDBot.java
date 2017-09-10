package com.mcmoddev.bot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mcmoddev.bot.handlers.CommandHandler;
import com.mcmoddev.bot.handlers.ConsoleHandler;
import com.mcmoddev.bot.handlers.ServerEventHandler;
import com.mcmoddev.bot.handlers.ZalgoHandler;
import com.mcmoddev.bot.logging.PrintStreamTraced;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

public class MMDBot {

    public static final Logger LOG = LogManager.getLogger("MMDBot");

    public static final String COMMAND_KEY = "!mmd";

    public static IDiscordClient instance;

    public static IGuild mmdGuild;

    public static IChannel botZone;

    public static IChannel events;

    public static IChannel console;

    public static boolean isReady = false;

    public static void main (String... args) throws RateLimitException {

        // Redirects the system out logger to append context on where it was called.
        System.setOut(new PrintStreamTraced(System.out));
        System.setErr(new PrintStreamTraced(System.err));

        try {

            instance = new ClientBuilder().withToken(args[0]).login();
            instance.getDispatcher().registerListener(new CommandHandler());
            instance.getDispatcher().registerListener(new ServerEventHandler());
            instance.getDispatcher().registerListener(new ZalgoHandler());
            instance.getDispatcher().registerListener(new ConsoleHandler());
        }

        catch (final DiscordException e) {

            e.printStackTrace();
        }
    }
}