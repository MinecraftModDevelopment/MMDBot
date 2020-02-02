package com.mcmoddev.mmdbot.commands.staff;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.oldchannels.OldChannelsHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
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
        help = "Gives channels which haven't been used in an amount of days given as an argument (default 7). **Locked to <#" + MMDBot.getConfig().getChannel("console") + ">**" +
                "Usage:"+MMDBot.getConfig().getMainPrefix()+"old-channels [oldness-threshold] [channel or category blacklist, seperated by spaces]";
    }

    /**
     *
     */
    @Override
    protected void execute(final CommandEvent event) {
        final Guild guild = event.getGuild();
        final EmbedBuilder embed = new EmbedBuilder();
        final TextChannel outputChannel = event.getTextChannel();
        final long channelID = MMDBot.getConfig().getChannel("console");
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
            final long daysSinceLastMessage = OldChannelsHelper.getLastMessageTime(channel);

            if (daysSinceLastMessage > dayThreshold) {
                embed.addField("#" + channel.getName(), String.valueOf(daysSinceLastMessage), true);
            } if (daysSinceLastMessage == -1) {
                embed.addField("#" + channel.getName(), "Never had a message", true);
            }

        }
        outputChannel.sendMessage(embed.build()).queue();
    }
}
