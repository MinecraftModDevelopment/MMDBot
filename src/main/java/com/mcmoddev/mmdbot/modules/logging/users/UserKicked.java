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
package com.mcmoddev.mmdbot.modules.logging.users;

import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.console.MMDMarkers;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;
import java.time.Instant;

/**
 * Log the kicks of users to a set channel to alert other staff members and to keep a record.
 *
 * @author ProxyNeko
 */
public class UserKicked extends ListenerAdapter {

    /**
     * On guild member removed.
     *
     * @param event the event that was fired.
     */
    @Override
    public void onGuildMemberRemove(final GuildMemberRemoveEvent event) {
        final var guild = event.getGuild();

        if (MMDBot.getConfig().getGuildID() != event.getGuild().getIdLong()) {
            return; //Make sure not to log if it's not related to the main guild.
        }

        final long kickLogsChannel = MMDBot.getConfig().getChannel("events.important-events");

        Utils.getChannelIfPresent(kickLogsChannel, channel ->
            guild.retrieveAuditLogs()
                .type(ActionType.KICK)
                .limit(1)
                .cache(false)
                .map(list -> list.get(0))
                .flatMap(entry -> {
                    final var embed = new EmbedBuilder();
                    final var kickedUser = event.getUser();
                    final var kickedBy = entry.getUser();

                    embed.setColor(Color.RED);
                    embed.setTitle("User Kicked.");
                    embed.setThumbnail(kickedUser.getEffectiveAvatarUrl());
                    embed.addField("**Name:**", kickedUser.getName(), false);
                    embed.addField("**UserID:**", kickedUser.getId(), false);
                    embed.addField("**Profile:**", kickedUser.getAsMention(), false);

                    if (entry.getReason() == null) {
                        embed.addField("**Kick reason:**",
                            "Reason for kick not provided or could not be found, please contact "
                                + kickedBy.getAsMention(), false);
                    } else {
                        embed.addField("**Kick reason:**", entry.getReason(), false);
                    }

                    if (entry.getTargetIdLong() != kickedUser.getIdLong()) {
                        MMDBot.LOGGER.warn(MMDMarkers.EVENTS, "Inconsistency between target of retrieved audit log "
                                + "entry and actual ban event target: retrieved is {}, but target is {}",
                            entry.getUser(), kickedUser);
                    } else if (entry.getUser() != null) {
                        final var editor = entry.getUser();
                        embed.addField("Banned By: ", editor.getAsMention()
                            + " (" + editor.getAsTag() + ")", false);
                    }

                    embed.setTimestamp(Instant.now());

                    return channel.sendMessageEmbeds(embed.build());
                }).queue()
        );
    }
}
