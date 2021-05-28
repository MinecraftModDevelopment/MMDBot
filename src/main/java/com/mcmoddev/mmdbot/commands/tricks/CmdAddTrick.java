package com.mcmoddev.mmdbot.commands.tricks;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.core.Utils;
import com.mcmoddev.mmdbot.tricks.Tricks;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 *
 */
public final class CmdAddTrick extends Command {

    /**
     *
     */
    public CmdAddTrick() {
        super();
        name = "addtrick";
        aliases = new String[]{"add-trick"};
        help = "Adds a new trick";
    }

    /**
     *
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) return;
        final TextChannel channel = event.getTextChannel();

        String args = event.getArgs();
        int firstSpace = args.indexOf(" ");

        //TODO: Permissions
        Tricks.addTrick(Tricks.getTrickType(args.substring(0, firstSpace)).createFromArgs(args.substring(firstSpace+1)));
    }
}
