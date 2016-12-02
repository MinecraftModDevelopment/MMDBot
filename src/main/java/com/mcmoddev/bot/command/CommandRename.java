package com.mcmoddev.bot.command;

import com.mcmoddev.bot.MMDBot;
import com.mcmoddev.bot.util.Utilities;

import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class CommandRename extends CommandAdmin {
    
    @Override
    public void proccessCommand (IMessage message, String[] params) {
        
        try {
            
            if (params.length == 2)
                try {
                    
                    message.getGuild().setUserNickname(MMDBot.instance.getOurUser(), params[1]);
                    Utilities.sendMessage(message.getChannel(), "You can call me " + params[1] + ".");
                }
                
                catch (final MissingPermissionsException e) {
                    
                    Utilities.sendMessage(message.getChannel(), "I am not allowed to change my identity. #Triggered");
                }
            else
                Utilities.sendMessage(message.getChannel(), "You must enter a valid name for this command to work.");
        }
        
        catch (DiscordException | RateLimitException e) {
            
            if (e.getMessage().contains("You are changing your username too fast"))
                Utilities.sendMessage(message.getChannel(), "You can only change the username twice per hour!");
            e.printStackTrace();
        }
    }
    
    @Override
    public String getDescription () {
        
        return "Allows the display name of the bot to be changed.";
    }
}
