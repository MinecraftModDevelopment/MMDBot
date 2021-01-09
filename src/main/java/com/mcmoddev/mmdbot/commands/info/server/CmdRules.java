package com.mcmoddev.mmdbot.commands.info.server;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.Color;
import java.time.Instant;

/**
 *
 */
public class CmdRules extends Command {

    /**
     *
     */
    private static final String BODY =
            "Please give <#" + MMDBot.getConfig().getChannel("info.rules") + "> a thorough read, "
                    + "**including** the full text of the Code of Conduct, which is linked there. "
                    + "Having everyone read and understand these rules and guidelines helps keep this server "
                    + "functioning well as a space for collaboration and discussion. Thank you.";

    /**
     *
     */
    public CmdRules() {
        super();
        name = "rules";
        help = "Tells you to read the rules.";
    }

    /**
     *
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) return;
        final EmbedBuilder embed = new EmbedBuilder();
        final TextChannel channel = event.getTextChannel();

        embed.setTitle("Please read the rules.");
        embed.setDescription(BODY);
        embed.setColor(Color.CYAN);
        embed.setTimestamp(Instant.now());
        channel.sendMessage(embed.build()).queue();
    }
}
