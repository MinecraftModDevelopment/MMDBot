package com.mcmoddev.bot.command;

import net.darkhax.botbase.BotBase;
import net.darkhax.botbase.commands.Command;
import net.darkhax.botbase.embed.MessageUser;
import net.darkhax.botbase.utils.MessageUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class CommandMe implements Command {

    @Override
    public void processCommand (BotBase bot, IChannel channel, IMessage message, String[] params) {

        final IUser user = message.getAuthor();
        final IGuild guild = message.getGuild();

        MessageUtils.sendMessage(message.getChannel(), new MessageUser(user, guild).build());
    }

    @Override
    public String getDescription () {

        return "Posts info about the current user in chat, including when they joined discord and the server.";
    }
}