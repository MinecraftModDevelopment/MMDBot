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

import com.mcmoddev.mmdbot.core.util.config.SnowflakeValue;
import com.mcmoddev.mmdbot.thelistener.TheListener;
import com.mcmoddev.mmdbot.thelistener.util.LoggingType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogChange;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.audit.AuditLogKey;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.guild.GuildAuditLogEntryCreateEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public final class RoleEvents extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoleEvents.class);

    @Override
    public void onGuildAuditLogEntryCreate(@NotNull final GuildAuditLogEntryCreateEvent event) {
        final AuditLogEntry entry = event.getEntry();
        if (entry.getType() != ActionType.MEMBER_ROLE_UPDATE) return;

        event.getGuild().retrieveMemberById(entry.getTargetId())
            .queue(targetMember -> {
                    final List<Role> addedRoles = parseRoles(entry, AuditLogKey.MEMBER_ROLES_ADD);
                    if (!addedRoles.isEmpty()) {
                        processEvent(entry, targetMember, "Added", addedRoles, List::removeAll);
                    }

                    final List<Role> removedRoles = parseRoles(entry, AuditLogKey.MEMBER_ROLES_REMOVE);
                    if (!removedRoles.isEmpty()) {
                        processEvent(entry, targetMember, "Removed", removedRoles, List::addAll);
                    }
                }, new ErrorHandler()
                    .handle(List.of(ErrorResponse.UNKNOWN_MEMBER, ErrorResponse.UNKNOWN_USER),
                        e -> LOGGER.warn("Could not find target member with ID {} for log entry {}", entry.getTargetId(), entry))
            );
    }

    private static List<Role> parseRoles(final AuditLogEntry entry, AuditLogKey logKey) {
        final @Nullable AuditLogChange change = entry.getChangeByKey(logKey);

        if (change == null) return List.of();

        // https://discord.com/developers/docs/resources/audit-log#audit-log-change-object-audit-log-change-exceptions
        // The added/removed roles are marked in the new_value property of the audit log change

        final List<String> roleIds = requireNonNull(change.getNewValue());
        final List<Role> roles = new ArrayList<>(roleIds.size());
        final JDA jda = entry.getJDA();

        for (String roleId : roleIds) {
            final @Nullable Role roleById = jda.getRoleById(roleId);
            if (roleById == null) {
                LOGGER.warn("Could not find role with ID {} while parsing change key {} for log entry {}", roleId, logKey, entry);
                continue;
            }
            roles.add(roleById);
        }

        return roles;
    }

    private static void processEvent(AuditLogEntry entry, Member member, String changeType, List<Role> modifiedRoles,
                                     BiConsumer<List<Role>, List<Role>> roleListsConsumer) {
        // The role list consumer allows the caller to update the previous roles list with the modified roles list,
        // to guard against the case where the target's roles list has been updated before we receive the event
        // We could also check JDA's impl to see if the event always fires before the target's roles list is updated and
        // get rid of this, but :shrug:
        final var guild = member.getGuild();

        final var roleIds = modifiedRoles.stream().map(Role::getIdLong).toList();
        final var noLoggingRoles = TheListener.getInstance().getConfigForGuild(guild.getIdLong()).getNoLoggingRoles();
        if (noLoggingRoles.stream().map(SnowflakeValue::asLong).anyMatch(roleIds::contains)) {
            return;
        }

        final List<Role> previousRoles = new ArrayList<>(member.getRoles());
        roleListsConsumer.accept(previousRoles, modifiedRoles);

        buildAndSendMessage(entry, member, changeType, previousRoles, modifiedRoles);
    }

    private static void buildAndSendMessage(AuditLogEntry entry, Member target, String changeType,
                                            List<Role> previousRoles, List<Role> modifiedRoles) {
        final var jda = target.getJDA();
        final var embed = new EmbedBuilder();

        embed.setTitle("User Role(s) " + changeType)
            .setColor(Color.YELLOW)
            .addField("User:", target.getUser().getAsTag(), true)
            .setFooter("User ID: " + target.getUser().getId(), target.getEffectiveAvatarUrl())
            .setTimestamp(entry.getTimeCreated());

        jda.retrieveUserById(entry.getUserIdLong())
            .onErrorMap(ErrorResponse.UNKNOWN_USER::test, e -> {
                LOGGER.warn("Could not retrieve editor user with ID {} for log entry {}", entry.getUserId(), entry);
                return null;
            })
            .queue(editor -> {
                if (editor == null) {
                    embed.addField("Editor:", editor.getAsTag(), true);
                }

                embed.addField("Previous Role(s):", mentionsOrEmpty(previousRoles), true)
                    .addField(changeType + " Role(s):", mentionsOrEmpty(modifiedRoles), true);

                LoggingType.ROLE_EVENTS.getChannels(target.getGuild().getIdLong()).forEach(id -> {
                    final var ch = id.resolve(idL -> jda.getChannelById(MessageChannel.class, idL));
                    if (ch != null) {
                        ch.sendMessageEmbeds(embed.build()).queue();
                    }
                });
            });
    }

    public static String mentionsOrEmpty(List<? extends IMentionable> list) {
        final String str = list.stream().map(IMentionable::getAsMention).collect(Collectors.joining(" "));
        return str.isBlank() ? "_None_" : str;
    }
}
