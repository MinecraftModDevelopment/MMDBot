package com.mcmoddev.mmdbot.commands.bot.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.time.Instant;

/**
 * The type Cmd about.
 *
 * @author ProxyNeko
 * @author Jriwanek
 */
public final class CmdAbout extends Command {

    /**
     * Instantiates a new Cmd about.
     */
    public CmdAbout() {
        super();
        name = "about";
        aliases = new String[]{"build"};
        help = "Gives info about this bot.";
    }

    /**
     * Execute.
     *
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }
        final var embed = new EmbedBuilder();
        final var channel = event.getTextChannel();

        embed.setTitle("Bot Build info");
        embed.setColor(Color.GREEN);
        embed.setThumbnail(MMDBot.getInstance().getSelfUser().getAvatarUrl());
        embed.setDescription("An in house bot to assists staff with daily tasks and provide fun and useful commands "
            + "for the community, please try ``" + MMDBot.getConfig().getMainPrefix()
            + "help`` for a list of commands!");
        embed.addField("Version:", MMDBot.VERSION, true);
        embed.addField("Issue Tracker:", Utils.makeHyperlink("MMDBot's Github", MMDBot.ISSUE_TRACKER),
            true);
        embed.addField("Current maintainers:", "jriwanek, WillBL, ProxyNeko, sciwhiz12, Antoine Gagnon",
            true);
        embed.setTimestamp(Instant.now());
        channel.sendMessageEmbeds(embed.build()).queue();
    }
}
