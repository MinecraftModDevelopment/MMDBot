package com.mcmoddev.bot.command;

import com.mcmoddev.bot.MMDBot;
import com.mcmoddev.bot.util.Utilities;
import com.mcmoddev.bot.util.message.MessageUser;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class CommandUser extends CommandModerator {

    @Override
    public void processCommand (IMessage message, String[] params) {

        IUser user = null;
        IGuild guild = null;

        // Just user
        if (params.length == 2) {

            guild = message.getGuild();
            user = guild.getUserByID(params[1]);
        }

        // Guild and user
        else if (params.length == 3) {

            guild = MMDBot.instance.getGuildByID(params[1]);

            if (guild != null) {

                user = guild.getUserByID(params[2]);
            }
        }

        if (user != null) {

            Utilities.sendMessage(message.getChannel(), new MessageUser(user, guild).build());
        }

        else {

            Utilities.sendMessage(message.getChannel(), this.getDescription());
        }

        System.out.println(params.length + " - " + user != null);
    }

    @Override
    public String getDescription () {

        return "Retrieves info about a user. There are two ways to use this command, the first takes just the user id, the other requires a guild id and then the user id.";
    }
}