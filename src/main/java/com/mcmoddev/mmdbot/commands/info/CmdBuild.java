package com.mcmoddev.mmdbot.commands.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.Color;
import java.time.Instant;

/**
 *
 */
public final class CmdBuild extends Command {

    /**
     *
     */
    public CmdBuild() {
        super();
        name = "build";
        aliases = new String[0];
        help = "Gives build info about this bot.";
    }

    /**
     *
     */
    @Override
    protected void execute(final CommandEvent event) {
        final Guild guild = event.getGuild();
        final EmbedBuilder embed = new EmbedBuilder();
        final TextChannel channel = event.getTextChannel();

        embed.setTitle("Bot Build info");
        embed.setColor(Color.GREEN);
        embed.setThumbnail(guild.getIconUrl());
        embed.addField("Version:", MMDBot.VERSION, true);
        embed.addField("Issue Tracker:", MMDBot.ISSUE_TRACKER, true);
        embed.addField("Current maintainers:", "jriwanek, WillBL, ProxyNeko", true);
        embed.setTimestamp(Instant.now());
        channel.sendMessage(embed.build()).queue();
    }
}
