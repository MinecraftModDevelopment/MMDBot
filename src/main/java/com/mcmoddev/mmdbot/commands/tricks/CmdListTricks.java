package com.mcmoddev.mmdbot.commands.tricks;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.core.Utils;
import com.mcmoddev.mmdbot.tricks.Tricks;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Optional;

/**
 *
 */
public final class CmdListTricks extends Command {

    /**
     *
     */
    public CmdListTricks() {
        super();
        name = "listtricks";
        aliases = new String[]{"list-tricks"};
        help = "Lists all tricks";
    }

    /**
     *
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) return;
        final TextChannel channel = event.getTextChannel();

        channel.sendMessage(
            new EmbedBuilder()
                .setDescription(Tricks.getTricks()
                    .stream()
                    .map(it -> it.getNames().stream().reduce("", (a, b) -> a + " / " + b))
                    .reduce("", (a, b) -> a + "\n" + b)
                )
                .build()
        ).queue();
    }
}
