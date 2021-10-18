/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2021 <MMD - MinecraftModDevelopment>
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
package com.mcmoddev.mmdbot.modules.commands.server;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.mcmoddev.mmdbot.utilities.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;

/**
 * Shows information about a guild.
 * Includes:
 * - Name
 * - Number of members
 * - Number of emotes
 * - Number of categories
 * - Number of channels
 * - Number of roles
 * - Date created
 * - Age of guild
 *
 * @author ProxyNeko
 * @author Curle
 */
public final class CmdGuild extends SlashCommand {

    /**
     * Instantiates a new Cmd guild.
     */
    public CmdGuild() {
        super();
        name = "guild";
        help = "Gives info about this guild.";
        category = new Category("Info");
        aliases = new String[]{"server"};
        guildOnly = true;
    }

    /**
     * Execute.
     *
     * @param event The {@link SlashCommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final SlashCommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }

        if (event.isFromGuild()) {
            final var guild = event.getGuild();
            final var embed = new EmbedBuilder();
            final var dateGuildCreated = guild.getTimeCreated().toInstant();

            embed.setTitle("Guild info");
            embed.setColor(Color.GREEN);
            embed.setThumbnail(guild.getIconUrl());
            embed.addField("Guilds name:", guild.getName(), true);
            embed.addField("Member count:", Integer.toString(guild.getMemberCount()), true);
            embed.addField("Emote count:", Long.toString(guild.getEmoteCache().size()), true);
            embed.addField("Category count:", Long.toString(guild.getCategoryCache().size()), true);
            embed.addField("Channel count:", Integer.toString(guild.getChannels().size()), true);
            embed.addField("Role count:", Long.toString(guild.getRoleCache().size()), true);
            embed.addField("Date created:", new SimpleDateFormat("yyyy/MM/dd HH:mm",
                Locale.ENGLISH).format(dateGuildCreated.toEpochMilli()), true);
            embed.addField("Guilds age:", Utils.getTimeDifference(Utils.getTimeFromUTC(dateGuildCreated),
                OffsetDateTime.now(ZoneOffset.UTC)), true);
            embed.setTimestamp(Instant.now());
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        } else {
            event.reply("This command should not be possible from a DM.").setEphemeral(true).queue();
        }
    }
}
