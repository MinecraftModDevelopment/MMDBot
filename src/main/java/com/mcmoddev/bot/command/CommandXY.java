package com.mcmoddev.bot.command;

import com.mcmoddev.bot.util.Utilities;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

public class CommandXY implements Command
{
    public static String body = "The XY problem is asking about your attempted solution rather than your actual problem. This leads to enormous amounts of wasted time and energy, both on the part of people asking for help, and on the part of those providing help." +  Utilities.SEPERATOR + Utilities.makeHyperlink("More Info", "http://xyproblem.info/");

    @Override
    public void processCommand(IMessage message, String[] params)
    {
        final EmbedBuilder embed = new EmbedBuilder();

        embed.setLenient(true);
        embed.withDesc(body);
        embed.withColor((int) (Math.random() * 0x1000000));
        embed.withTitle("The XY Problem, what is it?");
        Utilities.sendMessage(message.getChannel(), embed.build());
    }

    @Override
    public String getDescription()
    {
        return "Gives info on " + Utilities.quote("The XY Problem");
    }
}
