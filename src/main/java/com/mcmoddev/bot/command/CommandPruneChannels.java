package com.mcmoddev.bot.command;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.mcmoddev.bot.util.Utilities;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

public class CommandPruneChannels extends CommandAdmin {

    @Override
    public void processCommand (IMessage message, String[] params) {

        final LocalDateTime current = LocalDateTime.now();
        final int minDaysOfInactivity = params.length == 2 ? Integer.parseInt(params[1]) : 7;
        final StringBuilder builder = new StringBuilder();

        for (final IChannel channel : message.getGuild().getChannels())
            try {

                final IMessage latest = channel.getMessages().getLatestMessage();

                if (latest != null) {

                    final int daysSinceUsed = Math.toIntExact(ChronoUnit.DAYS.between(latest.getCreationDate(), current));

                    if (daysSinceUsed >= minDaysOfInactivity)
                        builder.append("#" + channel.getName() + " - " + daysSinceUsed + Utilities.SEPERATOR);
                }
            }

            catch (final ArrayIndexOutOfBoundsException e) {

                builder.append("#" + channel.getName() + " - unknown" + Utilities.SEPERATOR);
            }

        final EmbedBuilder embed = new EmbedBuilder();
        embed.ignoreNullEmptyFields();
        embed.withDesc(builder.toString());
        embed.withColor((int) (Math.random() * 0x1000000));
        embed.withFooterText("How do you work");

        Utilities.sendMessage(message.getChannel(), "The following channels have not been used in " + minDaysOfInactivity + " days.", embed.build());
    }

    @Override
    public String getDescription () {

        return "Lists all channels which have not been used in 7 days. You can change the amount of days to look for by adding a number to the end of the command.";
    }
}
