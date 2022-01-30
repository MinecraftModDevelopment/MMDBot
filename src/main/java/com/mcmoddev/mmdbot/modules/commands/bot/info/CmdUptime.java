/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 * https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 */
package com.mcmoddev.mmdbot.modules.commands.bot.info;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.mcmoddev.mmdbot.core.References;
import com.mcmoddev.mmdbot.utilities.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

import java.awt.Color;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

/**
 * Shows how long the bot has been online.
 *
 * @author ProxyNeko
 * @author Curle
 */
public class CmdUptime extends SlashCommand {

    /**
     * Instantiates a new Cmd uptime.
     */
    public CmdUptime() {
        super();
        name = "uptime";
        help = "State how long the current instance of the bot has been running, can also be used as a ping test.";
        category = new Category("Info");
        guildOnly = false;
    }

    /**
     * Execute.
     *
     * @param event The {@link SlashCommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final SlashCommandEvent event) {
        final var embed = new EmbedBuilder();

        embed.setTitle("Time spent online.");
        embed.setColor(Color.GREEN);
        embed.addField("I've been online for: ", Utils.getTimeDifference(Utils.getTimeFromUTC(
                    References.STARTUP_TIME), OffsetDateTime.now(ZoneOffset.UTC),
                ChronoUnit.YEARS, ChronoUnit.MONTHS, ChronoUnit.DAYS, ChronoUnit.HOURS, ChronoUnit.HOURS, ChronoUnit.SECONDS)
            , false);
        embed.setTimestamp(Instant.now());
        event.replyEmbeds(embed.build()).mentionRepliedUser(false).queue();
    }
}
