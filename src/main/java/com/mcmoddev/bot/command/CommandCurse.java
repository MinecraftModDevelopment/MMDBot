package com.mcmoddev.bot.command;

import com.mcmoddev.bot.cursemeta.MessageMods;

import net.darkhax.botbase.BotBase;
import net.darkhax.botbase.commands.Command;
import net.darkhax.botbase.utils.MessageUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

public class CommandCurse implements Command {

    public static final String DAILY = "https://cursemeta.dries007.net/daily.json";
    public static final String WEEKLY = "https://cursemeta.dries007.net/weekly.json";
    public static final String MONTHLY = "https://cursemeta.dries007.net/monthly.json";

    @Override
    public void processCommand (BotBase bot, IChannel channel, IMessage message, String[] params) {

        MessageUtils.sendMessage(channel, new MessageMods(10, params).build());
    }

    @Override
    public String getDescription () {

        return "Generates stats for a creator on Curse. Temporarily disabled.";
    }
}