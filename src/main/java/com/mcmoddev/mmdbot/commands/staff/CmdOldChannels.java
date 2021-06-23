package com.mcmoddev.mmdbot.commands.staff;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.Utils;
import com.mcmoddev.mmdbot.oldchannels.OldChannelsHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
        help = "Gives channels which haven't been used in an amount of days given as an argument (default 60)." +
            "Usage: " + MMDBot.getConfig().getMainPrefix() + "old-channels [threshold] [channel or category list, seperated by spaces]";
        hidden = true;
    }

    /**
     *
     */
    @Override
    protected void execute(final CommandEvent event) {
        final Guild guild = event.getGuild();
        final EmbedBuilder embed = new EmbedBuilder();
        final TextChannel outputChannel = event.getTextChannel();
        final List<String> args = new ArrayList<>(Arrays.asList(event.getArgs().split(" "))); //I have to do this so we can use `remove` later
        if (!Utils.checkCommand(this, event)) {
            outputChannel.sendMessage("This command is channel locked.").queue();
            return;
        }

        if (!OldChannelsHelper.isReady()) {
            outputChannel.sendMessage("This command is still setting up. Please try again in a few moments.").queue();
            return;
        }

        final int dayThreshold = args.size() > 0 && args.get(0).matches("-?\\d+") ? Integer.parseInt(args.remove(0)) : 60;

        List<String> toCheck = args.stream().map(it -> it.toLowerCase(Locale.ROOT)).collect(Collectors.toList());

        embed.setTitle("Days since last message in channels:");
        embed.setColor(Color.YELLOW);

        guild.getTextChannels().stream()
            .distinct()
            .filter(channelIsAllowedByList(toCheck))
            .map(channel -> new ChannelData(channel, OldChannelsHelper.getLastMessageTime(channel)))
            .forEach(channelData -> {
                if (channelData.days > dayThreshold) {
                    embed.addField("#" + channelData.channel.getName(), String.valueOf(channelData.days), true);
                } else if (channelData.days == -1) {
                    embed.addField("#" + channelData.channel.getName(), "Never had a message", true);
                }
            });

        outputChannel.sendMessageEmbeds(embed.build()).queue();
    }

    private Predicate<TextChannel> channelIsAllowedByList(List<String> list) {
        return (channel) -> list.isEmpty() ||
            (channel.getParent() != null && list.contains(channel.getParent().getName().toLowerCase(Locale.ROOT).replace(' ', '-')))
            || list.contains(channel.getName().toLowerCase(Locale.ROOT));
    }

    private static class ChannelData {
        final private TextChannel channel;
        final private long days;

        private ChannelData(final TextChannel channel, final long days) {
            this.channel = channel;
            this.days = days;
        }
    }
}
