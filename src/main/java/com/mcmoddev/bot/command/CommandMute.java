package com.mcmoddev.bot.command;

import java.util.EnumSet;

import com.mcmoddev.bot.MMDBot;

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
                    channel.overrideRolePermissions(message.getGuild().getRoleByID(305875306529554432L), null, EnumSet.of(Permissions.SEND_MESSAGES, Permissions.SEND_TTS_MESSAGES));
                }
                catch (DiscordException | MissingPermissionsException e) {

                    MMDBot.LOG.trace("Error muting " + channel.getName(), e);
                }
            });
        }
    }

    @Override
    public String getDescription () {

        return "Used to mute apply the mute role to all channels.";
    }
}