package com.mcmoddev.bot.command.moderative;

import net.darkhax.botbase.BotBase;
import net.darkhax.botbase.commands.CommandModerator;
import net.darkhax.botbase.utils.MessageUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

public class CommandReload extends CommandModerator {

    @Override
    public void processCommand (BotBase bot, IChannel channel, IMessage message, String[] params) {

        final long start = System.currentTimeMillis();
        bot.reload();
        MessageUtils.sendMessage(channel, "I am reloaded! Took " + (System.currentTimeMillis() - start) + "ms.");
    }

    @Override
    public String getDescription () {

        return "Reloades the bot. Commands and local data.";
    }
}
