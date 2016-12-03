package com.mcmoddev.bot.command;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.mcmoddev.bot.util.Utilities;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

public class CommandPruneChannels extends CommandAdmin {
    
    @Override
    public void processCommand (IMessage message, String[] params) {
        
        try {
            
            final LocalDateTime current = LocalDateTime.now();
            final int minDaysOfInactivity = params.length == 2 ? Integer.parseInt(params[1]) : 7;
            final StringBuilder builder = new StringBuilder();
            
            for (final IChannel channel : message.getGuild().getChannels()) {
                
                final IMessage latest = channel.getMessages().getLatestMessage();
                
                if (latest != null) {
                    
                    final int daysSinceUsed = Math.toIntExact(ChronoUnit.DAYS.between(latest.getCreationDate(), current));
                    
                    if (daysSinceUsed >= minDaysOfInactivity)
                        builder.append("#" + channel.getName() + " - " + daysSinceUsed + Utilities.SEPERATOR);
                }
            }
            
            Utilities.sendMessage(message.getChannel(), "The following channels have not been used in " + minDaysOfInactivity + " days." + Utilities.makeMultiCodeBlock(builder.toString()));
        }
        
        catch (final ArrayIndexOutOfBoundsException e) {
            
            Utilities.sendMessage(message.getChannel(), "Exception reading channel messages. Has the bot had time to initialize fully?");
        }
    }
    
    @Override
    public String getDescription () {
        
        return "Lists all channels which have not been used in 7 days. You can change the amount of days to look for by adding a number to the end of the command.";
    }   
}
