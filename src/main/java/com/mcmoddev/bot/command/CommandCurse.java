package com.mcmoddev.bot.command;

import net.darkhax.botbase.BotBase;
import net.darkhax.botbase.commands.Command;
import net.darkhax.botbase.utils.MessageUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

public class CommandCurse implements Command {

    @Override
    public void processCommand (BotBase bot, IChannel channel, IMessage message, String[] params) {

        MessageUtils.sendMessage(message.getChannel(), "This command is currently disabled. Stay tuned for updates.");
    }

    @Override
    public String getDescription () {

        return "Generates stats for a creator on Curse. Temporarily disabled.";
    }
}