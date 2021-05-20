package com.mcmoddev.mmdbot.commands.tricks;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.core.Utils;
import com.mcmoddev.mmdbot.tricks.Trick;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

/**
 *
 */
public final class CmdRunTrick extends Command {

    private Trick trick;

    /**
     *
     */
    public CmdRunTrick(Trick trick) {
        super();
        this.trick = trick;
        List<String> trickNames = trick.getNames();
        name = trickNames.get(0);
        aliases = trickNames.size() > 1 ? trickNames.subList(1, trickNames.size()).toArray(new String[0]) : new String[0];
        hidden = true;
    }

    /**
     *
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) return;
        final EmbedBuilder embed = new EmbedBuilder();
        final TextChannel channel = event.getTextChannel();

        channel.sendMessage(trick.getMessage(event.getArgs().split(" "))).queue();
    }
}
