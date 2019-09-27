package com.mcmoddev.bot.commands.unlocked;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.bot.misc.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.time.Instant;

public class CmdJustAsk extends Command {

    private static String BODY =
            "Please just ask the question; don't test the waters for the _real_ question. Instead, ask the full " +
                    "question so that others can better understand what you need, rather than creating an " +
                    "atmosphere of assumptions and discouraging people from wanting to help." +
                    System.lineSeparator() + Utils.makeHyperlink("More info", "http://sol.gfxile.net/dontask.html");

    public CmdJustAsk() {
        name = "justask";
        aliases = new String[]{"ask", "askyourquestion", "dontask"};
        help = "Don't ask if you can ask a question, it's pointless. Ask your question and get an answer faster.";
    }

    @Override
    protected void execute(CommandEvent event) {
        EmbedBuilder embed = new EmbedBuilder();
        TextChannel channel = event.getTextChannel();

        embed.setTitle("Are you asking if you can ask a question?");
        embed.setDescription(BODY);
        embed.setColor(Color.GREEN);
        embed.setTimestamp(Instant.now());
        channel.sendMessage(embed.build()).queue();
    }
}
