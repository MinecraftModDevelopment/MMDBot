package com.mcmoddev.bot.command;

import com.mcmoddev.bot.util.MMDRole;

import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public abstract class CommandAdmin implements Command {

    @Override
    public boolean isValidUsage (IMessage message) {

        final IUser user = message.getAuthor();

        return MMDRole.ADMIN.hasRole(user) || MMDRole.BOT_HOST.hasRole(user);
    }
}