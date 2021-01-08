package com.mcmoddev.mmdbot.commands.staff;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.oldchannels.OldChannelsHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

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
                "Usage:"+MMDBot.getConfig().getMainPrefix()+"old-channels [threshold] [channel or category blacklist, seperated by spaces]";
    }

    /**
     *
     */
    @Override
    protected void execute(final CommandEvent event) {
        final Guild guild = event.getGuild();
        final EmbedBuilder embed = new EmbedBuilder();
        final TextChannel outputChannel = event.getTextChannel();
        final List<String> args = Arrays.asList(event.getArgs().split(" "));
        if (MMDBot.getConfig().getAllowedChannels("old-commands", guild.getIdLong()).contains(outputChannel.getIdLong())) {
            outputChannel.sendMessage("This command is channel locked.").queue();
            return;
        }

        final int dayThreshold = args.size() > 0 && args.get(0).matches("-?\\d+") ? Integer.parseInt(args.get(0)) : 60;

        List<String> blacklist;

        if (args.size() > 1) {
            blacklist = new ArrayList<>(args);
            blacklist.remove(0);
        } else {
            blacklist = new ArrayList<>();
        }

        embed.setTitle("Days since last message in channels:");
        embed.setColor(Color.YELLOW);

		guild.getTextChannels().stream()
			.distinct()
			.filter(channel -> !blacklist.contains(channel.getName()))
			.filter(listDoesNotContainChannelParentName(blacklist))
			.map(channel -> new ChannelData(channel, OldChannelsHelper.getLastMessageTime(channel)))
			.forEach(channelData -> {
				if (channelData.days > dayThreshold) {
					embed.addField("#" + channelData.channel.getName(), String.valueOf(channelData.days), true);
				} else if (channelData.days == -1) {
					embed.addField("#" + channelData.channel.getName(), "Never had a message", true);
				}
			});

        outputChannel.sendMessage(embed.build()).queue();
    }

    private Predicate<TextChannel> listDoesNotContainChannelParentName(List<String> list) {
    	return (channel) -> !(channel.getParent() != null && list.contains(channel.getParent().getName().replace(' ', '-')));
	}

	private static class ChannelData {
    	private TextChannel channel;
    	private long days;

    	private ChannelData(final TextChannel channel, final long days) {
    		this.channel = channel;
    		this.days = days;
		}
	}
}
