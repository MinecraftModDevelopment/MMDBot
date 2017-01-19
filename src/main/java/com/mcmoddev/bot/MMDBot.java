package com.mcmoddev.bot;

import java.util.logging.Logger;

import com.mcmoddev.bot.handlers.CommandHandler;
import com.mcmoddev.bot.handlers.ServerEventHandler;
import com.mcmoddev.bot.handlers.ZalgoHandler;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

public class MMDBot {

    public static IDiscordClient INSTANCE;

    public static final Logger LOG = Logger.getLogger("MMDBot");

    public static final String MMD_GUILD_ID = "176780432371744769";

    public static final String MMDS_GUILD_ID = "229851088319283202";

    public static final String BOTZONE_CHANNEL_ID = "179302857143615489";

    public static final String EVENTS_CHANNEL_ID = "271498021286576128";

    public static final String COMMAND_KEY = "!mmd";

    public static void main (String... args) throws RateLimitException {

        try {

            INSTANCE = new ClientBuilder().withToken(args[0]).login();
            INSTANCE.getDispatcher().registerListener(new CommandHandler());
            INSTANCE.getDispatcher().registerListener(new ServerEventHandler());
            INSTANCE.getDispatcher().registerListener(new ZalgoHandler());
        }

        catch (final DiscordException e) {

            e.printStackTrace();
        }
    }
}