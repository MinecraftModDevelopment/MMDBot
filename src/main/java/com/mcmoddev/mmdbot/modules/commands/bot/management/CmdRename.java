package com.mcmoddev.mmdbot.modules.commands.bot.management;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.Utils;

/**
 * Rename the bot, not a nickname but it's actual name.
 *
 * @author ProxyNeko
 */
public class CmdRename extends Command {

    /**
     * Instantiates a new Cmd avatar.
     */
    public CmdRename() {
        super();
        name = "rename";
        help = "Set the name of the bot. Name can only be used twice an hour, has a 35 min cooldown each time used.";
        ownerCommand = true;
        hidden = true;
        guildOnly = false;
        cooldown = 2100;
    }

    /**
     * Try to set a new username for the bot.
     *
     * @param event The event
     */
    @Override
    protected void execute(final CommandEvent event) {
        final var commandArgs = event.getArgs();
        final var channel = event.getChannel();
        final var trigger = event.getMessage();
        final var newName = event.getArgs();
        final var selfUser = MMDBot.getInstance().getSelfUser();

        if (commandArgs.isEmpty()) {
            channel.sendMessage("No new name provided! Please provide me with a new name!")
                .reference(trigger).queue();
            return;
        }

        //TODO Better handling of the twice an hour name change limit... -ProxyNeko
        try {
            selfUser.getManager().setName(newName).queue();
            Utils.sleepTimer();
            channel.sendMessage("I shall henceforth be known as... **" + selfUser.getAsMention() + "**!")
                .reference(trigger).queue();
        } catch (Exception exception) {
            channel.sendMessage("Failed to set a new username... Please see logs for more info!").queue();
            MMDBot.LOGGER.error("Failed to set a new username... ", exception);
        }
    }
}
