package com.mcmoddev.bot;

import java.util.logging.Logger;

import com.mcmoddev.bot.command.CommandHandler;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

public class MMDBot {
    
    public static IDiscordClient instance;
    public static Logger LOG = Logger.getLogger("MMDBot");
    
    public static void main (String... args) throws RateLimitException {
        
        try {
            
            instance = new ClientBuilder().withToken(args[0]).login();
            instance.getDispatcher().registerListener(new MMDBot());
            initHandlers();
        }
        
        catch (final DiscordException e) {
            
            e.printStackTrace();
        }
    }
    
    public static void initHandlers () {
        
        CommandHandler.initCommands();
    }
    
    @EventSubscriber
    public void onMessageRecieved (MessageReceivedEvent event) {
        
        if (event.getMessage().getContent().startsWith(CommandHandler.COMMAND_KEY))
            CommandHandler.attemptCommandTriggers(event.getMessage());
    }
}