package com.mcmoddev.bot.commands.locked.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.bot.misc.BotConfig;
import com.mcmoddev.bot.misc.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.time.Instant;
import java.time.LocalDateTime;

public class CmdGuild extends Command {

    public CmdGuild() {
        name = "guild";
        aliases = new String[]{"server"};
        help = "Gives info about this guild. **Locked to <#572261616956211201>**";
    }

    @Override
    protected void execute(CommandEvent event) {
        Guild guild = event.getGuild();
        EmbedBuilder embed = new EmbedBuilder();
        TextChannel channel = event.getTextChannel();
        Instant dateGuildCreated = guild.getTimeCreated().toInstant();

        embed.setTitle("Guild info");
        embed.setColor(Color.GREEN);
        embed.setThumbnail(guild.getIconUrl());
        embed.addField("Guilds name:", guild.getName(), true);
        embed.addField("Member count:", Integer.toString(guild.getMembers().size()), true);
        embed.addField("Emote count:", Integer.toString(guild.getEmotes().size()), true);
        embed.addField("Category count:", Integer.toString(guild.getCategories().size()), true);
        embed.addField("Channel count:", Integer.toString(guild.getChannels().size()), true);
        embed.addField("Role count:", Integer.toString(guild.getRoles().size()), true);
        embed.addField("Date created:", Utils.DATE.format(dateGuildCreated.toEpochMilli()), true);
        embed.addField("Guilds age:", Utils.getTimeDifference(Utils.getLocalTime(dateGuildCreated), LocalDateTime.now()), true);
        embed.setTimestamp(Instant.now());

        if (channel.getIdLong() != BotConfig.getConfig().getBotStuffChannelId()) {
            channel.sendMessage("This command is channel locked to <#572261616956211201>").queue();
        } else {
            channel.sendMessage(embed.build()).queue();
        }
    }
}
