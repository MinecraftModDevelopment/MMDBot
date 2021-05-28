package com.mcmoddev.mmdbot.commands.tricks;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.core.Utils;
import com.mcmoddev.mmdbot.tricks.Tricks;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 *
 */
public final class CmdRemoveTrick extends Command {

    /**
     *
     */
    public CmdRemoveTrick() {
        super();
        name = "removetrick";
        aliases = new String[]{"remove-trick", "remtrick"};
        help = "Removes a trick";
    }

    /**
     *
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) return;
        final TextChannel channel = event.getTextChannel();

        //TODO: Permissions
        Tricks.getTrick(event.getArgs().split(" ")[0]).ifPresent(Tricks::removeTrick);
    }
}
