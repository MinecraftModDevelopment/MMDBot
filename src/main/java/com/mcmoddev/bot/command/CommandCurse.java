package com.mcmoddev.bot.command;

import com.mcmoddev.bot.util.Utilities;

import sx.blah.discord.handle.obj.IMessage;

public class CommandCurse implements Command {

    @Override
    public void processCommand (IMessage message, String[] params) {

        Utilities.sendMessage(message.getChannel(), "This command is currently disabled. Stay tuned for updates.");
    }

    @Override
    public String getDescription () {

        return "Generates stats for a creator on Curse. Temporarily disabled.";
    }
}