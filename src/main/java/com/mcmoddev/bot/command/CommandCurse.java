package com.mcmoddev.bot.command;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.mcmoddev.bot.util.CurseData;
import com.mcmoddev.bot.util.Utilities;

import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

public class CommandCurse implements Command {
    
    // TOOD implement this
    private static final Map<String, CurseData> CACHE = new HashMap<>();
    
    @Override
    public void processCommand (IMessage message, String[] params) {
        
        if (params.length >= 2) {
            
            Utilities.sendMessage(message.getChannel(), "Getting stats for " + params[1] + ", this will take a bit.");
            
            final CurseData data = new CurseData(params[1]);
            final StringBuilder builder = new StringBuilder();       
            final EmbedBuilder embed = new EmbedBuilder();
            
            if (!data.exists()) {
                
                Utilities.sendMessage(message.getChannel(), "No user could be found by the name " + params[1]);
                return;
            }
            
            else if (!data.hasProjects()) {
                
                Utilities.sendMessage(message.getChannel(), "No projects found for " + params[1]);
                return;
            }
            
            
            for (Entry<String, Long> set : data.getDownloads().entrySet()) {
                
                builder.append(set.getKey().replaceAll("-", " ") + " - " + NumberFormat.getInstance().format(set.getValue()) + Utilities.SEPERATOR);
            }
            
            builder.append("Total Projects: " + data.getProjectCount() + Utilities.SEPERATOR);
            
            embed.ignoreNullEmptyFields();
            embed.withDesc(builder.toString());
            embed.withColor((int) (Math.random() * 0x1000000));
            embed.withFooterText("Powered by Jared");
            embed.withTitle("Total Downloads: " + NumberFormat.getInstance().format(data.getTotalDownloads()));
            
             if (data.hasAvatar()) 
                embed.withThumbnail(data.getAvatar());
            
            Utilities.sendMessage(message.getChannel(), "Stats for " + params[1], embed.build());
        }
        
        else
            Utilities.sendMessage(message.getChannel(), "You must specify the name of a user as well!");
    }
    
    @Override
    public String getDescription () {
        
        return "Gets the total number of downloads for a user on Curse. Stats are powered by Jared. This may take a few moments as we make him count them by hand.";
    }
}