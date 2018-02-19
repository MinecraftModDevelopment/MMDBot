package com.mcmoddev.bot.command;

import com.mcmoddev.bot.cursemeta.*;
import net.darkhax.botbase.BotBase;
import net.darkhax.botbase.commands.Command;
import net.darkhax.botbase.utils.MessageUtils;
import sx.blah.discord.handle.obj.*;

public class CommandMod implements Command {
    
    @Override
    public void processCommand(BotBase bot, IChannel channel, IMessage message, String[] params) {
        String[] mods = new String[params.length];
        boolean wildcard = false;
        for(int i = 0; i < params.length; i++) {
            mods[i] = params[i].toLowerCase();
            if(mods[i].equals("*")) {
                wildcard = true;
                break;
            }
        }
        if(wildcard) {
            mods = new String[CurseMetaTracker.instance.getMods().keySet().size()];
            int counter = 0;
            for(Long aLong : CurseMetaTracker.instance.getMods().keySet()) {
                mods[counter++] = aLong.toString();
            }
        }
        MessageUtils.sendMessage(channel, new MessageMod(10, mods).build());
    }
    
    @Override
    public String getDescription() {
        
        return "Prints out information about mods on Curse. You can list multiple mos names, or use * for all mod authors.";
    }
}