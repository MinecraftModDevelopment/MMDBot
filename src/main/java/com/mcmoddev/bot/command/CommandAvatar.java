package com.mcmoddev.bot.command;

import com.mcmoddev.bot.MMDBot;
import com.mcmoddev.bot.util.Utilities;

import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.RateLimitException;

public class CommandAvatar extends CommandAdmin {

    @Override
    public void processCommand (IMessage message, String[] params) {

        try {

            if (params.length == 3) {

                MMDBot.instance.changeAvatar(Image.forUrl(params[1], params[2]));
                Utilities.sendMessage(message.getChannel(), "How do I look?");
            }
            else {
                Utilities.sendMessage(message.getChannel(), "You must enter an extension and a valid url for this to work.");
            }
        }

        catch (DiscordException | RateLimitException e) {

            MMDBot.LOG.trace("Could not change avatar", e);
        }
    }

    @Override
    public String getDescription () {

        return "Changes the avatar for the bot. Requires two parameters. The first is the file extension, and the second is a valid url that points to the image.";
    }
}
