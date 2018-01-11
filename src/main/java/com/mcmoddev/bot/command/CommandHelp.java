package com.mcmoddev.bot.command;

import net.darkhax.botbase.IDiscordBot;
import net.darkhax.botbase.commands.Command;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

public class CommandHelp implements Command {

    @Override
    public void processCommand (IDiscordBot bot, IChannel channel, IMessage message, String[] args) {

        // TODO
    }

    @Override
    public String getDescription () {

        return "Lists all commands available to the user, along with a basic description of each command. You can run the command with other command names as additional arguments to get a more thorough description of the command.";
    }
}