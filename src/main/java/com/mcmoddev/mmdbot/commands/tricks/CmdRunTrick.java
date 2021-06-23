package com.mcmoddev.mmdbot.commands.tricks;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.core.Utils;
import com.mcmoddev.mmdbot.tricks.Trick;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.List;

/**
 * @author williambl
 *
 * The type Cmd run trick.
 */
public final class CmdRunTrick extends Command {

    /**
     * The Trick.
     */
    private final Trick trick;

    /**
     * Instantiates a new Cmd run trick.
     *
     * @param trick the trick
     */
    public CmdRunTrick(final Trick trick) {
        super();
        this.trick = trick;
        List<String> trickNames = trick.getNames();
        name = trickNames.get(0);
        aliases = trickNames.size() > 1 ? trickNames.subList(1, trickNames.size())
            .toArray(new String[0]) : new String[0];
        hidden = true;
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
        final var embed = new EmbedBuilder();
        final var channel = event.getTextChannel();

        channel.sendMessage(trick.getMessage(event.getArgs().split(" "))).queue();
    }
}
