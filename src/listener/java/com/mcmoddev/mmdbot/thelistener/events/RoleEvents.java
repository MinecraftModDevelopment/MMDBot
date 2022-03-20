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
package com.mcmoddev.mmdbot.thelistener.events;

import com.mcmoddev.mmdbot.core.util.Pair;
import com.mcmoddev.mmdbot.thelistener.TheListener;
import com.mcmoddev.mmdbot.thelistener.util.ListenerAdapter;
import com.mcmoddev.mmdbot.thelistener.util.LoggingType;
import com.mcmoddev.mmdbot.thelistener.util.Utils;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.guild.MemberUpdateEvent;
import discord4j.core.object.audit.ActionType;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class RoleEvents extends ListenerAdapter {

    @Override
    public void onMemberUpdate(final MemberUpdateEvent event) {
        event.getMember().map(m -> Pair.makeOptional(event.getOld(), Optional.of(m)))
            .subscribe(pairO -> pairO.ifPresent(pair -> pair.accept((oldMember, newMember) -> {
                final var oldRoles = oldMember.getRoleIds();
                final var newRoles = newMember.getRoleIds();
                if (oldRoles.size() < newRoles.size()) {
                    final var rolesAdded = new HashSet<>(newRoles);
                    rolesAdded.removeAll(oldRoles);
                    oldRoles.removeAll(rolesAdded);
                    onRoleAdded(event, newMember, oldMember, oldRoles, rolesAdded);
                } else if (oldRoles.size() > newRoles.size()) {
                    final var rolesRemoved = new HashSet<>(oldRoles);
                    rolesRemoved.removeAll(newRoles);
                    onRoleRemoved(event, newMember, oldMember, oldRoles, rolesRemoved);
                }
            })));
    }

    private void onRoleAdded(final MemberUpdateEvent event, final Member newMember, final Member oldMember, final Set<Snowflake> rolesBefore, final Set<Snowflake> rolesAdded) {
        if (TheListener.getInstance().getConfigForGuild(event.getGuildId()).getNoLoggingRoles().stream().anyMatch(s -> rolesAdded.contains(s))) {
            return; // Ignore ignorable roles
        }
        event.getGuild().subscribe(guild -> {
            Utils.getAuditLog(guild, newMember.getId().asLong(), log -> log.withLimit(5).withActionType(ActionType.MEMBER_ROLE_UPDATE).withGuild(guild), entry -> {
                final var embed = EmbedCreateSpec.builder();
                final var target = new User(event.getClient(), newMember.getUserData());

                embed.color(Color.YELLOW);
                embed.title("User Role(s) Added");
                embed.thumbnail(target.getAvatarUrl());
                embed.addField("User:", target.getMention() + " (" + target.getId().asLong() + ")", true);

                final var targetId = (long) entry.getTargetId().map(Snowflake::asLong).orElse(0L);

                if (targetId != target.getId().asLong()) {
                    TheListener.LOGGER.warn("Inconsistency between target of retrieved audit log entry and actual " + "role event target: retrieved is {}, but target is {}", targetId, target);
                } else {
                    entry.getResponsibleUser().ifPresent(u -> embed.addField("Editor:", u.getMention() + "(%s)".formatted(u.getId().asLong()), true));
                }

                embed.addField("Previous Role(s):", ifEmpty(rolesBefore.stream().map(id -> "<@&%s>".formatted(id.asLong())).collect(Collectors.joining(" ")), "None"), false);
                embed.addField("Added Role(s):", ifEmpty(rolesAdded.stream().map(id -> "<@&%s>".formatted(id.asLong())).collect(Collectors.joining(" ")), "None"), false);
                embed.timestamp(Instant.now());

                Utils.executeInLoggingChannel(event.getGuildId(), LoggingType.ROLE_EVENTS, c -> c.createMessage(embed.build()).subscribe());
            });
        });
    }

    private void onRoleRemoved(final MemberUpdateEvent event, final Member newMember, final Member oldMember, final Set<Snowflake> rolesBefore, final Set<Snowflake> rolesRemoved) {
        if (TheListener.getInstance().getConfigForGuild(event.getGuildId()).getNoLoggingRoles().stream().anyMatch(rolesRemoved::contains)) {
            return; // Ignore ignorable roles
        }
        event.getGuild().subscribe(guild -> {
            Utils.getAuditLog(guild, newMember.getId().asLong(), log -> log.withLimit(5).withActionType(ActionType.MEMBER_ROLE_UPDATE).withGuild(guild), entry -> {
                final var embed = EmbedCreateSpec.builder();
                final var target = new User(event.getClient(), newMember.getUserData());

                embed.color(Color.YELLOW);
                embed.title("User Role(s) Removed");
                embed.addField("User:", target.getMention() + " (" + target.getId().asLong() + ")", true);
                embed.thumbnail(target.getAvatarUrl());

                final var targetId = (long) entry.getTargetId().map(Snowflake::asLong).orElse(0L);

                if (targetId != newMember.getId().asLong()) {
                    TheListener.LOGGER.warn("Inconsistency between target of retrieved audit log entry and actual " + "role event target: retrieved is {}, but target is {}", targetId, target);
                } else {
                    entry.getResponsibleUser().ifPresent(u -> embed.addField("Editor:", u.getMention() + "(%s)".formatted(u.getId().asLong()), true));
                }

                embed.addField("Previous Role(s):", ifEmpty(rolesBefore.stream().map(id -> "<@&%s>"
                    .formatted(id.asLong())).collect(Collectors.joining(" ")), "None"), false);
                embed.addField("Removed Role(s):", ifEmpty(rolesRemoved.stream().map(id -> "<@&%s>"
                    .formatted(id.asLong())).collect(Collectors.joining(" ")), "None"), false);
                embed.timestamp(Instant.now());

                Utils.executeInLoggingChannel(event.getGuildId(), LoggingType.ROLE_EVENTS, c ->
                    c.createMessage(embed.build()).subscribe());
            });
        });
    }

    private static String ifEmpty(String str, String ifEmpty) {
        return str.isBlank() ? ifEmpty : str;
    }
}
