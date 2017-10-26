package com.mcmoddev.bot.command;

import com.mcmoddev.bot.util.Utilities;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

public class CommandProbe extends CommandAdmin {

    @Override
    public void processCommand (IMessage message, String[] params) {

        final IChannel channel = message.getChannel();
        Utilities.sendMessage(channel, Utilities.makeMultilineMessage("Channel: " + channel.getName(), "Topic: " + channel.getTopic(), "Position: " + channel.getPosition(), "Date: " + channel.getCreationDate().toLocalDate().toString(), "Users: " + channel.getUsersHere().size(), "ID: " + channel.getStringID()));
    }

    @Override
    public String getDescription () {

        return "Probes the current channel for info about it.";
    }
}