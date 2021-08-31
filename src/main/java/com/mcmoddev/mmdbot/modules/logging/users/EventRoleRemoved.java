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

import com.mcmoddev.mmdbot.core.Utils;
import com.mcmoddev.mmdbot.utilities.console.MMDMarkers;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.mcmoddev.mmdbot.MMDBot.LOGGER;
import static com.mcmoddev.mmdbot.MMDBot.getConfig;

/**
 * The type Event role removed.
 *
 * @author
 */
public final class EventRoleRemoved extends ListenerAdapter {

    /**
     * On guild member role remove.
     *
     * @param event the event
     */
    @Override
    public void onGuildMemberRoleRemove(final GuildMemberRoleRemoveEvent event) {
        final var guild = event.getGuild();

        if (getConfig().getGuildID() != guild.getIdLong()) {
            return; // Make sure that we don't post if it's not related to 'our' guild
        }

        final long channelID = getConfig().getChannel("events.important");
        Utils.getChannelIfPresent(channelID, channel ->
            guild.retrieveAuditLogs()
                .type(ActionType.MEMBER_ROLE_UPDATE)
                .limit(1)
                .cache(false)
                .map(list -> list.get(0))
                .flatMap(entry -> {
                    final List<Role> previousRoles = new ArrayList<>(event.getMember().getRoles());
                    final List<Role> removedRoles = new ArrayList<>(event.getRoles());
                    previousRoles.addAll(removedRoles); // Just if the member has already been updated

                    final var embed = new EmbedBuilder();
                    final var target = event.getUser();

                    embed.setColor(Color.YELLOW);
                    embed.setTitle("User Role(s) Removed");
                    embed.setThumbnail(target.getEffectiveAvatarUrl());
                    embed.addField("User:", target.getAsMention() + " (" + target.getId() + ")",
                        true);
                    if (entry.getTargetIdLong() != target.getIdLong()) {
                        LOGGER.warn(MMDMarkers.EVENTS, "Inconsistency between target of retrieved audit log entry and actual "
                            + "role event target: retrieved is {}, but target is {}", target, entry.getUser());
                    } else if (entry.getUser() != null) {
                        final var editor = entry.getUser();
                        embed.addField("Editor:", editor.getAsMention() + " (" + editor.getId() + ")",
                            true);
                    }
                    embed.addField("Previous Role(s):", previousRoles.stream().map(IMentionable::getAsMention)
                        .collect(Collectors.joining(" ")), false);
                    embed.addField("Removed Role(s):", removedRoles.stream().map(IMentionable::getAsMention)
                        .collect(Collectors.joining(" ")), false);
                    embed.setTimestamp(Instant.now());

                    LOGGER.info(MMDMarkers.EVENTS, "Role(s) {} was removed from user {} by {}", removedRoles, target,
                        entry.getUser());

                    return channel.sendMessageEmbeds(embed.build());
                })
                .queue()
        );
    }
}
