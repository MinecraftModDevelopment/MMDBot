package com.mcmoddev.mmdbot.commands.tricks;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.core.Utils;
import com.mcmoddev.mmdbot.tricks.Tricks;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * @author williambl
 * <p>
 * The type Cmd list tricks.
 */
public final class CmdListTricks extends Command {

    /**
     * Instantiates a new Cmd list tricks.
     */
    public CmdListTricks() {
        super();
        name = "listtricks";
        aliases = new String[]{"list-tricks", "tricks"};
        help = "Lists all tricks";
    }

    /**
     * Execute.
     *
     * @param event the event
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }
        final var builder = new EmbedBuilder();
        final var channel = event.getTextChannel();

        channel.sendMessageEmbeds(
            builder
                .setDescription(Tricks.getTricks()
                    .stream()
                    .map(it -> it.getNames().stream().reduce("", (a, b) -> String.join(a, " ", b)
                        .trim()))
                    .reduce("", (a, b) -> a + "\n" + b)
                )
                .build()
        ).queue();
    }
}
