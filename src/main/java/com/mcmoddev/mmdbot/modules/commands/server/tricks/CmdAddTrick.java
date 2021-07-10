package com.mcmoddev.mmdbot.modules.commands.server.tricks;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.core.Utils;
import com.mcmoddev.mmdbot.utilities.tricks.Tricks;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

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
        final var guild = event.getGuild();
        final var author = guild.getMember(event.getAuthor());
        final List<Role> roleList = event.getMember().getRoles();

        String args = event.getArgs();
        int firstSpace = args.indexOf(" ");

        //TODO Add permissions check.
        //if () {
        Tricks.addTrick(Tricks.getTrickType(args.substring(0, firstSpace))
            .createFromArgs(args.substring(firstSpace + 1)));
        channel.sendMessage("Added trick!").queue();
        //} else {
        //    channel.sendMessage("You can't add tricks yet, please find a member of staff to do so!").queue();
        //}
    }
}
