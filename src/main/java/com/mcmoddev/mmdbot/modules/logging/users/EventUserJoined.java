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
import com.mcmoddev.mmdbot.utilities.database.dao.PersistedRoles;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static com.mcmoddev.mmdbot.MMDBot.LOGGER;
import static com.mcmoddev.mmdbot.MMDBot.getConfig;
import static com.mcmoddev.mmdbot.utilities.console.MMDMarkers.EVENTS;

/**
 * The type Event user joined.
 *
 * @author
 */
public final class EventUserJoined extends ListenerAdapter {

    /**
     * On guild member join.
     *
     * @param event the event
     */
    @Override
    public void onGuildMemberJoin(final GuildMemberJoinEvent event) {
        final var guild = event.getGuild();
        final var channel = guild.getTextChannelById(getConfig().getChannel("events.basic"));
        if (channel == null) {
            return;
        }

        final var user = event.getUser();
        final var member = guild.getMember(user);

        final var guildId = guild.getIdLong();
        if (getConfig().getGuildID() == guildId) {
            LOGGER.info(EVENTS, "User {} joined the guild", user);
            final List<Role> roles = Utils.getOldUserRoles(guild, user.getIdLong());
            if (member != null && !roles.isEmpty()) {
                LOGGER.info(EVENTS, "Giving old roles to user {}: {}", user, roles);
                EventRoleAdded.IGNORE_ONCE.putAll(user, roles);
                for (final Role role : roles) {
                    try {
                        guild.addRoleToMember(member, role).queue();
                    } catch (final HierarchyException ex) {
                        LOGGER.warn(EVENTS, "Unable to give member {} role {}: {}", member, role, ex.getMessage());
                    }
                }
                MMDBot.database().useExtension(PersistedRoles.class, persist -> persist.clear(user.getIdLong()));
            }
            final var embed = new EmbedBuilder();
            embed.setColor(Color.GREEN);
            embed.setTitle("User Joined");
            embed.setThumbnail(user.getEffectiveAvatarUrl());
            embed.addField("User:", user.getAsTag(), true);
            if (!roles.isEmpty()) {
                embed.addField("Roles:", roles.stream().map(IMentionable::getAsMention)
                    .collect(Collectors.joining()), true);
            }
            embed.setFooter("User ID: " + user.getId());
            embed.setTimestamp(Instant.now());

            channel.sendMessageEmbeds(embed.build()).queue();
        }
    }
}
