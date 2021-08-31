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
package com.mcmoddev.mmdbot.modules.logging.users;

import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.Utils;
import com.mcmoddev.mmdbot.utilities.console.MMDMarkers;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;
import java.time.Instant;

/**
 * Log the bans of users to a set channel to alert other staff members and to keep a record in case the user deletes
 * the account leaving us without a name for a banned user.
 *
 * @author ProxyNeko
 */
public class UserBanned extends ListenerAdapter {

    /**
     * On guild member banned.
     *
     * @param event the event that was fired.
     */
    @Override
    public void onGuildBan(final GuildBanEvent event) {
        final var guild = MMDBot.getInstance().getGuildById("229851088319283202");

        if (MMDBot.getConfig().getGuildID() != event.getGuild().getIdLong()) {
            return; //Make sure not to log if it's not related to the main guild.
        }

        final long banLogsChannel = MMDBot.getConfig().getChannel("events.ban-log-channel");

        Utils.getChannelIfPresent(banLogsChannel, channel ->
            guild.retrieveAuditLogs()
                .type(ActionType.BAN)
                .limit(1)
                .cache(false)
                .map(list -> list.get(0))
                .flatMap(entry -> {
                    final var embed = new EmbedBuilder();
                    final var target = event.getUser();

                    embed.setColor(Color.RED);
                    embed.setTitle("User Banned.");
                    embed.setThumbnail(target.getEffectiveAvatarUrl());
                    embed.addField("**Name:**", target.getName(), false);
                    embed.addField("**UserID:**", target.getId(), false);
                    embed.addField("**Profile:**", target.getAsMention(), false);

                    if (entry.getReason() == null) {
                        embed.addField("**Ban reason:** ",
                            "Reason for ban was not provided or could not be found!", false);
                    } else {
                        embed.addField("**Details:** ", entry.getReason(), false);
                    }

                    if (entry.getTargetIdLong() != target.getIdLong()) {
                        MMDBot.LOGGER.warn(MMDMarkers.EVENTS, "Inconsistency between target of retrieved audit log "
                                + "entry and actual ban event target: retrieved is {}, but target is {}",
                            target, entry.getUser());
                    } else if (entry.getUser() != null) {
                        final var editor = entry.getUser();
                        embed.setDescription("Banned By: " + editor.getName() + " (" + editor.getId() + ")");
                    }

                    embed.setTimestamp(Instant.now());

                    return channel.sendMessageEmbeds(embed.build());
                }).queue()
        );
    }
}
