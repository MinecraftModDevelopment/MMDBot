package com.mcmoddev.bot.command;

import net.darkhax.botbase.BotBase;
import net.darkhax.botbase.commands.Command;
import net.darkhax.botbase.utils.MessageUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

public class CommandXY implements Command {

    public static String body = "The XY problem is asking about your attempted solution rather than your actual problem. This leads to enormous amounts of wasted time and energy, both on the part of people asking for help, and on the part of those providing help." + MessageUtils.SEPERATOR + MessageUtils.makeHyperlink("More Info", "http://xyproblem.info/");

    @Override
    public void processCommand (BotBase bot, IChannel channel, IMessage message, String[] params) {

        final EmbedBuilder embed = new EmbedBuilder();

        embed.setLenient(true);
        embed.withDesc(body);
        embed.withColor((int) (Math.random() * 0x1000000));
        embed.withTitle("The XY Problem, what is it?");
        MessageUtils.sendMessage(message.getChannel(), embed.build());
    }

    @Override
    public String getDescription () {

        return "Gives info on " + MessageUtils.quote("The XY Problem");
    }
}
