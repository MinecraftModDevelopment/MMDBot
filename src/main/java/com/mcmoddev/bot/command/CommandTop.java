package com.mcmoddev.bot.command;

import com.mcmoddev.bot.curse.CurseTracker;
import com.mcmoddev.bot.curse.json.Mod;
import net.darkhax.botbase.BotBase;
import net.darkhax.botbase.commands.Command;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.EmbedBuilder;

import java.text.NumberFormat;
import java.util.*;

public class CommandTop implements Command {
    
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);
    
    @Override
    public void processCommand(BotBase bot, IChannel channel, IMessage message, String[] params) {
        
        if(params.length < 2) {
            channel.sendMessage("Too few params!");
            return;
        }
        
        int number = Integer.parseInt(params[0]);
        if(number > 20) {
            channel.sendMessage("Too many entries! Capping at 20!");
            number = 20;
        }
        String cat = params[1];
        StringBuilder msg = new StringBuilder();
        List<Mod> values = new ArrayList<>(CurseTracker.instance.getMods());
        
        int counter = 0;
        switch(cat.toLowerCase()) {
            case "monthly":
        
                values.sort(new Comparator<Mod>() {
                    @Override
                    public int compare(Mod o1, Mod o2) {
                        return Double.compare(o2.getMonthly(), o1.getMonthly());
                    }
                });
                for(Mod info : values) {
                    if(counter < number) {
                        msg.append(counter++ + 1).append(") ").append(info.getName()).append(" ").append(NUMBER_FORMAT.format(info.getMonthly())).append("\n");
                    }
                }
                break;
            case "total":
                
                values.sort(new Comparator<Mod>() {
                    @Override
                    public int compare(Mod o1, Mod o2) {
                        return Double.compare(o2.getDownloadCount(), o1.getDownloadCount());
                    }
                });
                for(Mod info : values) {
                    if(counter < number) {
                        msg.append(counter++ + 1).append(") ").append(info.getName()).append(" ").append(NUMBER_FORMAT.format(info.getDownloadCount())).append("\n");
                    }
                }
                break;
        }
        
        
        EmbedBuilder embed = new EmbedBuilder();
        embed.withTitle("Top " + number + " " + cat);
        embed.withDesc(msg.toString());
        bot.sendMessage(channel, embed.build());
    }
    
    @Override
    public String getDescription() {
        
        return "Posts info about the top mods of a category";
    }
}