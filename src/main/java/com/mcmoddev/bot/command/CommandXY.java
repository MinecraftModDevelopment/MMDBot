package com.mcmoddev.bot.command;

import com.mcmoddev.bot.util.Utilities;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

public class CommandXY implements Command
{
    @Override
    public void processCommand(IMessage message, String[] params)
    {
        final StringBuilder builder = new StringBuilder();
        final EmbedBuilder embed = new EmbedBuilder();

        builder.append(
                "The XY problem is asking about your attempted solution rather than your actual problem." +
                "This leads to enormous amounts of wasted time and energy, both on the part of people asking for help, and on the part of those providing help.\n" +
                Utilities.SEPERATOR +
                Utilities.makeHyperlink("More Info", "http://xyproblem.info/")
        );

        embed.setLenient(true);
        embed.withDesc(builder.toString());
        embed.withColor((int) (Math.random() * 0x1000000));
        embed.withTitle("The XY Problem, what is it?");
        Utilities.sendMessage(message.getChannel(), embed.build());
    }

    @Override
    public String getDescription()
    {
        return "Gives info on \"The XY Problem\"";
    }
}
