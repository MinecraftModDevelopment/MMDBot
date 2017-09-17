package com.mcmoddev.bot.command;

import java.util.EnumSet;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

public class CommandMute extends CommandAdmin {

    @Override
    public void processCommand (IMessage message, String[] params) {

        for (final IChannel channel : message.getGuild().getChannels()) {
            RequestBuffer.request( () -> {
                try {
                    channel.overrideRolePermissions(message.getGuild().getRoleByID("305875306529554432"), null, EnumSet.of(Permissions.SEND_MESSAGES, Permissions.SEND_TTS_MESSAGES));
                    System.out.println(channel.getName());
                }
                catch (DiscordException | MissingPermissionsException e) {

                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public String getDescription () {

        return "";
    }
}