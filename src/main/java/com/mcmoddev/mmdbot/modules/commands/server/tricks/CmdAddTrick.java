package com.mcmoddev.mmdbot.modules.commands.server.tricks;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.Utils;
import com.mcmoddev.mmdbot.utilities.tricks.Tricks;

/**
 * @author williambl
 * <p>
 * The type Cmd add trick.
 */
public final class CmdAddTrick extends Command {

    /**
     * Instantiates a new Cmd add trick.
     */
    public CmdAddTrick() {
        super();
        name = "addtrick";
        aliases = new String[]{"add-trick"};
        help = "Adds a new trick";
        requiredRole = "bot maintainer";
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

        String args = event.getArgs();
        int firstSpace = args.indexOf(" ");

        try {
            Tricks.addTrick(Tricks.getTrickType(args.substring(0, firstSpace))
                .createFromArgs(args.substring(firstSpace + 1)));
            channel.sendMessage("Added trick!").queue();
        } catch (IllegalArgumentException e) {
            channel.sendMessage("A command with that name already exists!").queue();
            MMDBot.LOGGER.warn("Failure adding trick: {}", e.getMessage());
        }
    }
}
