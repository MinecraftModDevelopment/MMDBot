package com.mcmoddev.bot.command;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import com.mcmoddev.bot.util.Utilities;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

public class CommandPruneChannels extends CommandAdmin {

    @Override
    public void processCommand (IMessage message, String[] params) {

        final LocalDateTime current = LocalDateTime.now();
        final int minDaysOfInactivity = params.length == 2 ? Integer.parseInt(params[1]) : 7;
        final EmbedBuilder embed = new EmbedBuilder();
        final Map<String, Integer> channels = new HashMap<String, Integer>();

        for (final IChannel channel : message.getGuild().getChannels()) {
            
            if (channel.getName().equalsIgnoreCase("getting-started"))
                continue;
        
            try {

                final IMessage latest = channel.getMessages().getLatestMessage();

                if (latest != null) {

                    final int daysSinceUsed = Math.toIntExact(ChronoUnit.DAYS.between(latest.getCreationDate(), current));

                    if (daysSinceUsed >= minDaysOfInactivity)
                        channels.put("#" + channel.getName(), daysSinceUsed);
                }
            }

            catch (final ArrayIndexOutOfBoundsException e) {

                channels.put("#" + channel.getName(), -1);
            }
        }
        embed.ignoreNullEmptyFields();
        embed.withColor((int) (Math.random() * 0x1000000));
        embed.withDesc(Utilities.mapToString(Utilities.sortByValue(channels, true)));
        Utilities.sendMessage(message.getChannel(), "The following channels have not been used in " + minDaysOfInactivity + " days.", embed.build());
    }

    @Override
    public String getDescription () {

        return "Lists all channels which have not been used in 7 days. You can change the amount of days to look for by adding a number to the end of the command.";
    }
}
