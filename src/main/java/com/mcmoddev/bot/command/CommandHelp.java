package com.mcmoddev.bot.command;

import java.util.Map.Entry;
import java.util.StringJoiner;

import net.darkhax.botbase.BotBase;
import net.darkhax.botbase.commands.Command;
import net.darkhax.botbase.utils.MessageUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

public class CommandHelp implements Command {

    @Override
    public void processCommand (BotBase bot, IChannel channel, IMessage message, String[] args) {

        final StringJoiner joiner = new StringJoiner(MessageUtils.SEPERATOR + MessageUtils.SEPERATOR);

        for (final Entry<String, Command> s : bot.getCommands().getCommands().entrySet()) {

            if (s.getValue().isValidUsage(bot, message)) {

                joiner.add(bot.getCommandKey() + " " + s.getKey() + " - " + s.getValue().getDescription());
            }
        }

        MessageUtils.sendPrivateMessage(bot.instance, message.getAuthor(), MessageUtils.makeMultiCodeBlock(joiner.toString()));
    }

    @Override
    public String getDescription () {

        return "Lists all commands available to the user, along with a basic description of each command.";
    }
}