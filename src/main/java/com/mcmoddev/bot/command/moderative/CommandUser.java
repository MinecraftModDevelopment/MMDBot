package com.mcmoddev.bot.command.moderative;

import com.mcmoddev.bot.MMDBot;

import net.darkhax.botbase.IDiscordBot;
import net.darkhax.botbase.commands.CommandModerator;
import net.darkhax.botbase.embed.MessageUser;
import net.darkhax.botbase.utils.MessageUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class CommandUser extends CommandModerator {

    @Override
    public void processCommand (IDiscordBot bot, IChannel channel, IMessage message, String[] params) {

        IUser user = null;
        IGuild guild = null;

        // Just user
        if (params.length == 2) {

            guild = message.getGuild();
            user = guild.getUserByID(Long.parseUnsignedLong(params[1]));
        }

        // Guild and user
        else if (params.length == 3) {

            guild = MMDBot.instance.getClient().getGuildByID(Long.parseUnsignedLong(params[1]));

            if (guild != null) {
                user = guild.getUserByID(Long.parseUnsignedLong(params[2]));
            }
        }

        if (user != null) {

            MessageUtils.sendMessage(message.getChannel(), new MessageUser(user, guild).build());
        }

        else {

            MessageUtils.sendMessage(message.getChannel(), this.getDescription());
        }

        System.out.println(params.length + " - " + user != null);
    }

    @Override
    public String getDescription () {

        return "Retrieves info about a user. There are two ways to use this command, the first takes just the user id, the other requires a guild id and then the user id.";
    }
}