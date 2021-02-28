package com.mcmoddev.mmdbot.commands.info.server;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.Color;
import java.time.Instant;

/**
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
     *
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) return;
        final EmbedBuilder embed = new EmbedBuilder();
        final TextChannel channel = event.getTextChannel();

        embed.setTitle("Please read the readme.");
        embed.setDescription(BODY);
        embed.setColor(Color.CYAN);
        embed.setTimestamp(Instant.now());
        channel.sendMessage(embed.build()).queue();
    }
}
