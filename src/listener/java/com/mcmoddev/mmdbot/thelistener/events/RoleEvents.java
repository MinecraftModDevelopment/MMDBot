/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * Specifically version 2.1 of the License.
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

import com.mcmoddev.mmdbot.thelistener.TheListener;
import com.mcmoddev.mmdbot.thelistener.util.LoggingType;
import com.mcmoddev.mmdbot.thelistener.util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class RoleEvents extends ListenerAdapter {

    @Override
    public void onGuildMemberRoleAdd(@NotNull final GuildMemberRoleAddEvent event) {
        final var roleIds = event.getRoles().stream().map(Role::getIdLong).toList();
        if (TheListener.getInstance().getConfigForGuild(event.getGuild().getIdLong()).getNoLoggingRoles().stream().anyMatch(roleIds::contains)) {
            return;
        }
        final List<Role> previousRoles = new ArrayList<>(event.getMember().getRoles());
        final List<Role> addedRoles = new ArrayList<>(event.getRoles());
        previousRoles.removeAll(addedRoles); // Just if the member has already been updated
        final var target = event.getMember();
        Utils.getAuditLog(event.getGuild(), target.getIdLong(), log -> log.type(ActionType.MEMBER_ROLE_UPDATE).limit(5), entry -> {
            final var embed = new EmbedBuilder();

            embed.setColor(Color.YELLOW);
            embed.setTitle("User Role(s) Added");
            embed.setThumbnail(target.getEffectiveAvatarUrl());
            embed.addField("User:", target.getAsMention() + " (" + target.getId() + ")", true);

            final var targetId = entry.getTargetIdLong();

            if (targetId != target.getIdLong()) {
                TheListener.LOGGER.warn("Inconsistency between target of retrieved audit log entry and actual " + "role event target: retrieved is {}, but target is {}", targetId, target);
            } else if (entry.getUser() != null) {
                final var editor = entry.getUser();
                embed.addField("Editor:", editor.getAsMention() + " (" + editor.getId() + ")",
                    true);
            }

            embed.addField("Previous Role(s):", ifEmpty(previousRoles.stream().map(id -> "<@&%s>".formatted(id.getId())).collect(Collectors.joining(" "))), false);
            embed.addField("Added Role(s):", ifEmpty(addedRoles.stream().map(id -> "<@&%s>".formatted(id.getId())).collect(Collectors.joining(" "))), false);
            embed.setTimestamp(entry.getTimeCreated());

            final var bEmb = embed.build();
            final var loggingChannels = LoggingType.MESSAGE_EVENTS.getChannels(event.getGuild().getIdLong());
            loggingChannels
                .forEach(id -> {
                    final var ch = id.resolve(idL -> event.getJDA().getChannelById(net.dv8tion.jda.api.entities.MessageChannel.class, idL));
                    if (ch != null) {
                        ch.sendMessageEmbeds(bEmb).queue();
                    }
                });
        });
    }

    @Override
    public void onGuildMemberRoleRemove(@NotNull final GuildMemberRoleRemoveEvent event) {
        final var roleIds = event.getRoles().stream().map(Role::getIdLong).toList();
        if (TheListener.getInstance().getConfigForGuild(event.getGuild().getIdLong()).getNoLoggingRoles().stream().anyMatch(roleIds::contains)) {
            return;
        }
        final List<Role> previousRoles = new ArrayList<>(event.getMember().getRoles());
        final List<Role> removedRoles = new ArrayList<>(event.getRoles());
        previousRoles.removeAll(removedRoles); // Just if the member has already been updated
        final var target = event.getMember();
        Utils.getAuditLog(event.getGuild(), target.getIdLong(), log -> log.type(ActionType.MEMBER_ROLE_UPDATE).limit(5), entry -> {
            final var embed = new EmbedBuilder();

            embed.setColor(Color.YELLOW);
            embed.setTitle("User Role(s) Removed");
            embed.addField("User:", target.getAsMention() + " (" + target.getId() + ")", true);
            embed.setThumbnail(target.getAvatarUrl());

            final var targetId = entry.getTargetIdLong();

            if (targetId != target.getIdLong()) {
                TheListener.LOGGER.warn("Inconsistency between target of retrieved audit log entry and actual " + "role event target: retrieved is {}, but target is {}", targetId, target);
            } else if (entry.getUser() != null) {
                final var editor = entry.getUser();
                embed.addField("Editor:", editor.getAsMention() + " (" + editor.getId() + ")",
                    true);
            }

            embed.addField("Previous Role(s):", ifEmpty(previousRoles.stream().map(id -> "<@&%s>"
                .formatted(id.getId())).collect(Collectors.joining(" "))), false);
            embed.addField("Removed Role(s):", ifEmpty(removedRoles.stream().map(id -> "<@&%s>"
                .formatted(id.getId())).collect(Collectors.joining(" "))), false);
            embed.setTimestamp(entry.getTimeCreated());

            final var bEmb = embed.build();
            final var loggingChannels = LoggingType.MESSAGE_EVENTS.getChannels(event.getGuild().getIdLong());
            loggingChannels
                .forEach(id -> {
                    final var ch = id.resolve(idL -> event.getJDA().getChannelById(net.dv8tion.jda.api.entities.MessageChannel.class, idL));
                    if (ch != null) {
                        ch.sendMessageEmbeds(bEmb).queue();
                    }
                });
        });
    }

    private static String ifEmpty(String str) {
        return str.isBlank() ? "None" : str;
    }
}
