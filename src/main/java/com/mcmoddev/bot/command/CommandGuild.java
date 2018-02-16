package com.mcmoddev.bot.command;

import net.darkhax.botbase.BotBase;
import net.darkhax.botbase.commands.Command;
import net.darkhax.botbase.embed.MessageGuild;
import net.darkhax.botbase.utils.MessageUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

public class CommandGuild implements Command {

    @Override
    public void processCommand (BotBase bot, IChannel channel, IMessage message, String[] params) {

        if (channel.getGuild() != null) {

            MessageUtils.sendMessage(channel, new MessageGuild(channel.getGuild()).build());
        }
    }

    @Override
    public String getDescription () {

        return "Displays information about the current guild.";
    }
}
