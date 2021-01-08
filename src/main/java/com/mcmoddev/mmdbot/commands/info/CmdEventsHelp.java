package com.mcmoddev.mmdbot.commands.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.Color;
import java.time.Instant;

/**
 *
 */
public final class CmdEventsHelp extends Command {

    /**
     *
     */
    private static final String IMAGE_URL =
            "https://cdn.discordapp.com/attachments/665281306426474506/665605979798372392/eventhandler.png";

    /**
     *
     */
    public CmdEventsHelp() {
        super();
        name = "events";
        aliases = new String[]{"eventshelp", "why-doesnt-my-event-handler-work"};
        help = "Gives info on how to use forge event handlers.";
    }

    /**
     *
     */
    @Override
    protected void execute(final CommandEvent event) {
		if (!Utils.checkCommand(this, event)) return;
        final EmbedBuilder embed = new EmbedBuilder();
        final TextChannel channel = event.getTextChannel();

        embed.setTitle("Why doesn't my event handler work?");
        embed.setImage(IMAGE_URL);
        embed.setColor(Color.GREEN);
        embed.setTimestamp(Instant.now());

        channel.sendMessage(embed.build()).queue();
    }
}
