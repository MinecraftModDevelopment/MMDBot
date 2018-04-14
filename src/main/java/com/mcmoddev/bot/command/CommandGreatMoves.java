package com.mcmoddev.bot.command;

import net.darkhax.botbase.BotBase;
import net.darkhax.botbase.commands.Command;
import net.darkhax.botbase.utils.MessageUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

public class CommandGreatMoves implements Command {

    @Override
    public void processCommand (BotBase bot, IChannel channel, IMessage message, String[] params) {
    
        bot.sendMessage(channel, "Great Moves! Keep it Up! Proud of ya! Papa bless!" + MessageUtils.SEPERATOR + "https://soundcloud.com/aldenchambers/great-moves-keep-it-up");
    }

    @Override
    public String getDescription () {

        return "Posts an encouraging message in chat.";
    }
}