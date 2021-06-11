package com.mcmoddev.mmdbot.commands.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.time.Instant;

/**
 *
 * @author
 *
 */
public final class CmdBuild extends Command {

    /**
     *
     */
    public CmdBuild() {
        super();
        name = "build";
        aliases = new String[0];
        help = "Gives build info about this bot.";
    }

    /**
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }
        final var guild = event.getGuild();
        final var embed = new EmbedBuilder();
        final var channel = event.getTextChannel();

        embed.setTitle("Bot Build info");
        embed.setColor(Color.GREEN);
        embed.setThumbnail(guild.getIconUrl());
        embed.addField("Version:", MMDBot.VERSION, true);
        embed.addField("Issue Tracker:", MMDBot.ISSUE_TRACKER, true);
        embed.addField("Current maintainers:", "jriwanek, WillBL, ProxyNeko, sciwhiz12", true);
        embed.setTimestamp(Instant.now());
        channel.sendMessage(embed.build()).queue();
    }
}
