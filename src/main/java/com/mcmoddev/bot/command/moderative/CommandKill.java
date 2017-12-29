package com.mcmoddev.bot.command.moderative;

import org.apache.commons.lang3.math.NumberUtils;

import com.mcmoddev.bot.MMDBot;

import net.darkhax.botbase.IDiscordBot;
import net.darkhax.botbase.commands.CommandAdmin;
import net.darkhax.botbase.utils.MessageUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

public class CommandKill extends CommandAdmin {

    @Override
    public void processCommand (IDiscordBot bot, IChannel channel, IMessage message, String[] params) {

        final int time = params.length == 2 && NumberUtils.isCreatable(params[1]) ? new Integer(params[1]) : 10;

        if (time > 500) {

            MessageUtils.sendMessage(message.getChannel(), "The max time is 300 seconds! (5 mins)");
            return;
        }

        if (time < 0) {

            MessageUtils.sendMessage(message.getChannel(), "Negative times not accepted!");
            return;
        }

        if (time < 1) {

            MessageUtils.sendMessage(message.getChannel(), "At least one second of delay is required!");
            return;
        }

        MessageUtils.sendMessage(message.getChannel(), "Oh, I am slain. Killed by %s#%s Death in %d seconds.", message.getAuthor().getName(), message.getAuthor().getDiscriminator(), time);
        // TODO audit

        try {

            Thread.sleep(1000 * time);
        }

        catch (final InterruptedException e) {

            MMDBot.instance.getLogger().trace("Imortal Bot Exception", e);
        }

        System.exit(0);
    }

    @Override
    public String getDescription () {

        return "Kills the bot";
    }
}
