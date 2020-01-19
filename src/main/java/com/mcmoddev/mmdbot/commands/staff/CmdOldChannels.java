package com.mcmoddev.bot.commands.locked.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.bot.MMDBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
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
        help = "Gives channels which haven't been used in an amount of days given as an argument (default 7). **Locked to <#" + MMDBot.getConfig().getChannelIDConsole() + ">**" +
                "Usage:"+MMDBot.getConfig().getPrefix()+"old-channels [oldness-threshold] [channel or category blacklist, seperated by spaces]";
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
        final List<String> args = Arrays.asList(event.getArgs().split(" "));
        if (outputChannel.getIdLong() != channelID) {
            outputChannel.sendMessage("This command is channel locked to <#" + channelID + ">").queue();
            return;
        }

        final int dayThreshold = args.size() > 0 && args.get(0).matches("-?\\d+") ? Integer.parseInt(args.get(0)) : 7;

        List<String> channelBlacklist;

        if (args.size() > 1) {
            channelBlacklist = new ArrayList<>(args);
            channelBlacklist.remove(0);
        } else {
            channelBlacklist = new ArrayList<>();
        }

        final List<TextChannel> channelList = guild.getTextChannels();

        embed.setTitle("Old channels");
        embed.setColor(Color.YELLOW);

        for (TextChannel channel : channelList) {
            if (channelBlacklist.contains(channel.getName())) {
                continue;
            }
            if (channel.getParent() != null && channelBlacklist.contains(channel.getParent().getName().replace(' ', '-'))) {
                continue;
            }
            final MessageHistory history = channel.getHistory();
            List<Message> latestMessages = history.retrievePast(1).complete();
            if (latestMessages.size() > 0) {
                while (latestMessages.get(0).isWebhookMessage()) {
                    latestMessages = history.retrievePast(1).complete();
                }
            }
            final long daysSinceLastMessage = latestMessages.size() > 0 ?
                    ChronoUnit.DAYS.between(latestMessages.get(latestMessages.size()-1).getTimeCreated(), OffsetDateTime.now()) :
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
