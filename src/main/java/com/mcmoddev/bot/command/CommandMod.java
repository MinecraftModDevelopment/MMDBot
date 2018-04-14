package com.mcmoddev.bot.command;

import com.mcmoddev.bot.curse.CurseTracker;
import com.mcmoddev.bot.curse.json.Mod;
import com.mcmoddev.bot.cursemeta.MessageMod;

import net.darkhax.botbase.BotBase;
import net.darkhax.botbase.commands.Command;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

import java.util.stream.Collectors;

public class CommandMod implements Command {

    @Override
    public void processCommand (BotBase bot, IChannel channel, IMessage message, String[] params) {

        String[] mods = new String[params.length];
        boolean wildcard = false;
        for (int i = 0; i < params.length; i++) {
            mods[i] = params[i].toLowerCase();
            if (mods[i].equals("*")) {
                wildcard = true;
                break;
            }
        }
        if (wildcard) {
            mods = new String[CurseTracker.instance.getMods().stream().filter(mod -> mod.getPackageType().equalsIgnoreCase("mod")).collect(Collectors.toList()).size()];
            int counter = 0;
            for (final Mod mod: CurseTracker.instance.getMods().stream().filter(mod -> mod.getPackageType().equalsIgnoreCase("mod")).collect(Collectors.toList())) {
                mods[counter++] = mod.getName();
            }
        }
        bot.sendMessage(channel, new MessageMod(10, mods).build());
    }

    @Override
    public String getDescription () {

        return "Prints out information about mods on Curse. You can list multiple mos names, or use * for all mod authors.";
    }
}