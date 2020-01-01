package com.mcmoddev.bot.commands.unlocked;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.time.Instant;

/**
 *
 */
public final class CmdEventsHelp extends Command {

	/**
	 *
	 */
    private static final String IMAGE_URL =
            "https://media.discordapp.net/attachments/179315645005955072/540214876929261590/why-doesnt-my-event-handler-work.gif";

    /**
     *
     */
    public CmdEventsHelp() {
        super();
        name = "eventshelp";
        aliases = new String[]{"events", "why-doesnt-my-event-handler-work"};
        help = "Gives info on how to use forge event handlers.";
    }

    /**
     *
     */
    @Override
    protected void execute(final CommandEvent event) {
        final EmbedBuilder embed = new EmbedBuilder();
        final TextChannel channel = event.getTextChannel();

        embed.setTitle("Why doesn't my event handler work?");
        embed.setImage(IMAGE_URL);
        embed.setColor(Color.GREEN);
        embed.setTimestamp(Instant.now());

        channel.sendMessage(embed.build()).queue();
    }
}
