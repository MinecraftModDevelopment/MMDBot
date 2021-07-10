package com.mcmoddev.mmdbot.modules.commands.bot.management;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Shut down the bot and the JDA instance gracefully.
 *
 * @author ProxyNeko
 */
public class CmdShutdown extends Command {

    /**
     * Instantiates a new Cmd.
     */
    public CmdShutdown() {
        super();
        name = "shutdown";
        help = "Shuts the bot down without restarting it.";
        ownerCommand = true;
        hidden = true;
        guildOnly = false;
    }

    /**
     * Shut down the bot on command.
     *
     * @param event The event.
     */
    @Override
    protected void execute(final CommandEvent event) {
        event.reply("Shutting down the bot! " + event.getAuthor().getAsMention());
        //Shut down the JDA instance gracefully.
        event.getJDA().shutdown();
        MMDBot.LOGGER.warn("Shutting down the bot by request of " + event.getAuthor().getName() + " via Discord!");
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.exit(0);
            }
        }, 1000);
    }
}
