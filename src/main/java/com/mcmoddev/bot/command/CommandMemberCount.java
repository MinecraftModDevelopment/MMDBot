package com.mcmoddev.bot.command;

import com.mcmoddev.bot.util.Utilities;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

public class CommandMemberCount implements Command {
    
    @Override
    public void processCommand (IMessage message, String[] params) {
        
        final IGuild guild = message.getGuild();
        
        if (guild != null)
            Utilities.sendMessage(message.getChannel(), String.format("There are %d people in the server :)", guild.getUsers().size()));
    }
    
    @Override
    public String getDescription () {
        
        return "Counts the amount of members in the room.";
    }
}
