package com.mcmoddev.mmdbot.commands.tricks;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.core.Utils;
import com.mcmoddev.mmdbot.tricks.Tricks;

/**
 * @author williambl
 *
 * The type Cmd remove trick.
 */
public final class CmdRemoveTrick extends Command {

    /**
     * Instantiates a new Cmd remove trick.
     */
    public CmdRemoveTrick() {
        super();
        name = "removetrick";
        aliases = new String[]{"remove-trick", "remtrick"};
        help = "Removes a trick";
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
        final var channel = event.getTextChannel();

        //TODO: Permissions
        Tricks.getTrick(event.getArgs().split(" ")[0]).ifPresent(Tricks::removeTrick);
        channel.sendMessage("Removed trick!").queue();
    }
}
