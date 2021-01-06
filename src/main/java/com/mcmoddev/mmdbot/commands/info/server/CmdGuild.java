package com.mcmoddev.mmdbot.commands.info.server;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 *
 */
public final class CmdGuild extends Command {

    /**
     *
     */
    public CmdGuild() {
        super();
        name = "guild";
        aliases = new String[]{"server"};
        help = "Gives info about this guild.";
    }

    /**
     *
     */
    @Override
    protected void execute(final CommandEvent event) {
    	if (!Utils.checkCommand(this, event)) return;
        final Guild guild = event.getGuild();
        final EmbedBuilder embed = new EmbedBuilder();
        final TextChannel channel = event.getTextChannel();
        final Instant dateGuildCreated = guild.getTimeCreated().toInstant();

        embed.setTitle("Guild info");
        embed.setColor(Color.GREEN);
        embed.setThumbnail(guild.getIconUrl());
        embed.addField("Guilds name:", guild.getName(), true);
        embed.addField("Member count:", Integer.toString(guild.getMembers().size()), true);
        embed.addField("Emote count:", Integer.toString(guild.getEmotes().size()), true);
        embed.addField("Category count:", Integer.toString(guild.getCategories().size()), true);
        embed.addField("Channel count:", Integer.toString(guild.getChannels().size()), true);
        embed.addField("Role count:", Integer.toString(guild.getRoles().size()), true);
        embed.addField("Date created:", new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH).format(dateGuildCreated.toEpochMilli()), true);
        embed.addField("Guilds age:", Utils.getTimeDifference(Utils.getLocalTime(dateGuildCreated), LocalDateTime.now()), true);
        embed.setTimestamp(Instant.now());
		channel.sendMessage(embed.build()).queue();
    }
}
