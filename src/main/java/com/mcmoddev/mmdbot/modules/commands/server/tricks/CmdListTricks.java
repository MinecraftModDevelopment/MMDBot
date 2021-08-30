package com.mcmoddev.mmdbot.modules.commands.server.tricks;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.core.Utils;
import com.mcmoddev.mmdbot.utilities.tricks.Tricks;
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

        builder
            .setTitle("Tricks")
            .setDescription(Tricks.getTricks()
                .stream()
                .map(it -> it.getNames().stream().reduce("", (a, b) -> (a.isEmpty() ? a : a + " / ") + b))
                .reduce("", (a, b) -> a + "\n" + b)
            );

        if (!builder.isEmpty()) {
            channel.sendMessageEmbeds(builder.build()).queue();
        } else {
            channel.sendMessage("No tricks currently exist!").queue();
        }
    }
}
