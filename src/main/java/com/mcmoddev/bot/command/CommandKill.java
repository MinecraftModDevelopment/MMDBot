package com.mcmoddev.bot.command;

import org.apache.commons.lang3.math.NumberUtils;

import com.mcmoddev.bot.MMDBot;
import com.mcmoddev.bot.util.Utilities;

import sx.blah.discord.handle.obj.IMessage;

public class CommandKill extends CommandAdmin {

    @Override
    public void processCommand (IMessage message, String[] params) {

        final int time = params.length == 2 && NumberUtils.isCreatable(params[1]) ? new Integer(params[1]) : 10;

        if (time > 500) {

            Utilities.sendMessage(message.getChannel(), "The max time is 300 seconds! (5 mins)");
            return;
        }

        if (time < 0) {

            Utilities.sendMessage(message.getChannel(), "Negative times not accepted!");
            return;
        }

        if (time < 1) {

            Utilities.sendMessage(message.getChannel(), "At least one second of delay is required!");
            return;
        }

        Utilities.sendMessage(message.getChannel(), "Oh, I am slain. Killed by %s#%s Death in %d seconds.", message.getAuthor().getName(), message.getAuthor().getDiscriminator(), time);
        Utilities.sendMessage(MMDBot.state.getAuditChannel(), "Oh, I am slain. Killed by %s#%s Death in %d seconds.", message.getAuthor().getName(), message.getAuthor().getDiscriminator(), time);

        try {

            Thread.sleep(1000 * time);
        }

        catch (final InterruptedException e) {

            e.printStackTrace();
        }

        System.exit(0);
    }

    @Override
    public String getDescription () {

        return "Kills the bot";
    }
}
