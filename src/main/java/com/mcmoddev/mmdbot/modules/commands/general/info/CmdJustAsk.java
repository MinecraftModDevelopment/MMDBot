package com.mcmoddev.mmdbot.modules.commands.general.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.time.Instant;

/**
 * The type Cmd just ask.
 *
 * @author
 */
public final class CmdJustAsk extends Command {

    /**
     * The constant BODY.
     */
    private static final String BODY =
        "Please just ask the question; don't test the waters for the _real_ question. Instead, ask the full "
            + "question so that others can better understand what you need, rather than creating an "
            + "atmosphere of assumptions and discouraging people from wanting to help."
            + System.lineSeparator() + Utils.makeHyperlink("More info", "https://sol.gfxile.net/dontask.html");

    /**
     * Instantiates a new Cmd just ask.
     */
    public CmdJustAsk() {
        super();
        name = "justask";
        aliases = new String[]{"ask", "askyourquestion", "dontask", "askawaypadawan"};
        help = "Don't ask if you can ask a question, it's pointless. Ask your question and get an answer faster.";
    }

    /**
     * Execute.
     *
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }
        final var embed = new EmbedBuilder();
        final var channel = event.getTextChannel();

        embed.setTitle("Are you asking if you can ask a question?");
        embed.setDescription(BODY);
        embed.setColor(Color.GREEN);
        embed.setTimestamp(Instant.now());
        channel.sendMessageEmbeds(embed.build()).queue();
    }
}
