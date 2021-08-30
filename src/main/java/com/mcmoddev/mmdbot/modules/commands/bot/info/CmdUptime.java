package com.mcmoddev.mmdbot.modules.commands.bot.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.core.References;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * The type Cmd uptime.
 *
 * @author ProxyNeko
 */
public class CmdUptime extends Command {

    /**
     * Instantiates a new Cmd uptime.
     */
    public CmdUptime() {
        super();
        name = "uptime";
        guildOnly = false;
        help = "State how long the current instance of the bot has been running, can also be used as a ping test.";
    }

    /**
     * Execute.
     *
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final CommandEvent event) {
        final var embed = new EmbedBuilder();
        final var channel = event.getChannel();

        embed.setTitle("Time spent online.");
        embed.setColor(Color.GREEN);
        //TODO Fix utils that handle time to show hours and minuets too.
        embed.addField("I've been online for: ", Utils.getTimeDifference(Utils.getLocalTime(
            References.STARTUP_TIME), LocalDateTime.now()), false);
        embed.setTimestamp(Instant.now());
        channel.sendMessageEmbeds(embed.build()).queue();
    }
}
