package com.mcmoddev.bot.command.moderative;

import net.darkhax.botbase.BotBase;
import net.darkhax.botbase.commands.CommandModerator;
import net.darkhax.botbase.embed.MessageUser;
import net.darkhax.botbase.utils.MessageUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class CommandUser extends CommandModerator {

    @Override
    public void processCommand (BotBase bot, IChannel channel, IMessage message, String[] params) {

        IUser user = null;
        IGuild guild = null;

        // Just user
        if (params.length == 1) {

            guild = message.getGuild();
            user = guild.getUserByID(Long.parseUnsignedLong(params[0]));
        }

        if (user != null) {

            MessageUtils.sendMessage(message.getChannel(), new MessageUser(user, guild).withColor(user.getColorForGuild(channel.getGuild())).build());
        }

        else {

            MessageUtils.sendMessage(message.getChannel(), this.getDescription());
        }
    }

    @Override
    public String getDescription () {

        return "Retrieves info about a user. Requires the user's long id as a parameter.";
    }
}