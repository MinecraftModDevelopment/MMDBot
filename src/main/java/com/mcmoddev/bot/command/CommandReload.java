package com.mcmoddev.bot.command;

import com.mcmoddev.bot.MMDBot;
import com.mcmoddev.bot.util.Utilities;

import sx.blah.discord.handle.obj.IMessage;

public class CommandReload extends CommandAdmin {

    @Override
    public void processCommand (IMessage message, String[] params) {

        Utilities.sendMessage(message.getChannel(), "Reloading handlers and resource!");
        MMDBot.initHandlers();
        Utilities.sendMessage(message.getChannel(), "Reload complete. That tickled ;)");
    }

    @Override
    public String getDescription () {

        return "Reloads all of the handlers and resources for the bot, including commands!";
    }
}