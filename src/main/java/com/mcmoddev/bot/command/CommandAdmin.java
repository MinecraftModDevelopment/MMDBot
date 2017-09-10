package com.mcmoddev.bot.command;

import com.mcmoddev.bot.MMDBot;

import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public abstract class CommandAdmin implements Command {

    @Override
    public boolean isValidUsage (IMessage message) {

        final IUser user = message.getAuthor();

        return MMDBot.state.isAdmin(user) || MMDBot.state.isBotManager(user);
    }
}