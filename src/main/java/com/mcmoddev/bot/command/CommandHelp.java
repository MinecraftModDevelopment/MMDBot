package com.mcmoddev.bot.command;

import java.util.Map.Entry;

import com.mcmoddev.bot.MMDBot;
import com.mcmoddev.bot.handlers.CommandHandler;
import com.mcmoddev.bot.util.Utilities;

import sx.blah.discord.handle.obj.IMessage;

public class CommandHelp implements Command {

    @Override
    public void processCommand (IMessage message, String[] args) {

        String descriptions = "";

        if (args.length > 1)
            for (int index = 1; index < args.length; index++) {

                final Command cmd = CommandHandler.getCommand(args[index]);

                if (cmd != null && cmd.isValidUsage(message))
                    descriptions += MMDBot.config.key + " " + args[index] + " - " + cmd.getDescription() + Utilities.SEPERATOR + Utilities.SEPERATOR;
            }
        else
            for (final Entry<String, Command> command : CommandHandler.getCommands().entrySet())
                if (command.getValue().isValidUsage(message))
                    descriptions += MMDBot.config.key + " " + command.getKey() + " - " + command.getValue().getDescription() + Utilities.SEPERATOR + Utilities.SEPERATOR;

        Utilities.sendPrivateMessage(message.getAuthor(), Utilities.makeMultiCodeBlock(descriptions));
    }

    @Override
    public String getDescription () {

        return "Lists all commands available to the user, along with a basic description of each command. You can run the command with other command names as additional arguments to get a more thorough description of the command.";
    }
}