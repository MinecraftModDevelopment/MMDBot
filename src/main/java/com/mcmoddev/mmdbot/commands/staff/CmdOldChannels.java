package com.mcmoddev.bot.commands.locked.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.bot.MMDBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 *
 */
public final class CmdOldChannels extends Command {

    /**
     *
     */
    public CmdOldChannels() {
        super();
        name = "old-channels";
        help = "Gives channels which haven't been used in an amount of days given as an argument (default 7). **Locked to <#" + MMDBot.getConfig().getChannelIDConsole() + ">**";
    }

    /**
     *
     */
    @Override
    protected void execute(final CommandEvent event) {
        final Guild guild = event.getGuild();
        final EmbedBuilder embed = new EmbedBuilder();
        final TextChannel outputChannel = event.getTextChannel();
        final long channelID = MMDBot.getConfig().getChannelIDConsole();
        if (outputChannel.getIdLong() != channelID) {
            outputChannel.sendMessage("This command is channel locked to <#" + channelID + ">").queue();
            return;
        }

        final int dayThreshold = event.getArgs().matches("-?\\d+") ? Integer.parseInt(event.getArgs()) : 7;
        final List<TextChannel> channelList = guild.getTextChannels();

        embed.setTitle("Old channels");
        embed.setColor(Color.YELLOW);

        for (TextChannel channel : channelList) {
            final List<Message> latestMessages = channel.getHistory().retrievePast(1).complete();
            final long daysSinceLastMessage = latestMessages.size() > 0 ?
                    ChronoUnit.DAYS.between(latestMessages.get(0).getTimeCreated(), OffsetDateTime.now()) :
                    -1;
            if (daysSinceLastMessage > dayThreshold) {
                embed.addField("#" + channel.getName(), String.valueOf(daysSinceLastMessage), true);
            } if (daysSinceLastMessage == -1) {
                embed.addField("#" + channel.getName(), "Never had a message", true);
            }

        }
        outputChannel.sendMessage(embed.build()).queue();
    }
}
