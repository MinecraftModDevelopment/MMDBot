package com.mcmoddev.mmdbot.commands.info.server;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.time.Instant;

/**
 *
 * @author
 *
 */
public final class CmdReadme extends Command {

    /**
     *
     */
    private static final String BODY =
        "Please give <#" + MMDBot.getConfig().getChannel("info.readme") + "> a thorough read, this "
            + "channel gives users a guide to the server, how to get roles and general settling in notes. "
            + "Thank you.";

    /**
     *
     */
    public CmdReadme() {
        super();
        name = "readme";
        aliases = new String[]{"read-me"};
        help = "Tells you to read the readme.";
    }

    /**
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }
        final var embed = new EmbedBuilder();
        final var channel = event.getTextChannel();

        embed.setTitle("Please read the readme.");
        embed.setDescription(BODY);
        embed.setColor(Color.CYAN);
        embed.setTimestamp(Instant.now());
        channel.sendMessageEmbeds(embed.build()).queue();
    }
}
