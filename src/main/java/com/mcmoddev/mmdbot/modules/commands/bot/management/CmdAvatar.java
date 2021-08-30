package com.mcmoddev.mmdbot.modules.commands.bot.management;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import net.dv8tion.jda.api.entities.Icon;

import java.io.IOException;

/**
 * @author ProxyNeko
 *
 * Sett he avatar of the bot, requires bot admin permissions.
 */
public class CmdAvatar extends Command {

    /**
     * Instantiates a new Cmd avatar.
     */
    public CmdAvatar() {
        super();
        name = "avatar";
        help = "Set the avatar of the bot.";
        ownerCommand = true;
        hidden = true;
        guildOnly = false;
    }

    /**
     * Try to set the a new avatar for the bot.
     *
     * @param event the event
     */
    @Override
    protected void execute(final CommandEvent event) {
        final var commandArgs = event.getArgs();
        final var channel = event.getChannel();
        final var trigger = event.getMessage();
        final var attachment = event.getMessage().getAttachments();
        final var newAvatar = attachment.get(0);

        if (commandArgs.length() <= 1) {
            if (attachment.isEmpty()) {
                channel.sendMessage("No image attachment provided, I need a new avatar!")
                    .reference(trigger).queue();
                return;
            }

            if (!newAvatar.isImage()) {
                channel.sendMessage("This attachment is not an image! "
                        + "Please provide a valid image to use as my avatar!")
                    .reference(trigger).queue();
                return;
            }

            newAvatar.retrieveInputStream().thenAccept(setIcon -> {
                try {
                    MMDBot.getInstance().getSelfUser().getManager().setAvatar(Icon.from(setIcon)).queue();
                    channel.sendMessage("New avatar set, how do I look?").queue();
                } catch (IOException exception) {
                    channel.sendMessage("Failed to set a new avatar... Please see logs for more info!").queue();
                    MMDBot.LOGGER.error("Failed to set a new avatar... ", exception);
                }
            });
        }
    }
}
