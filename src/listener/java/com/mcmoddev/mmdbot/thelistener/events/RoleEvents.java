/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2023 <MMD - MinecraftModDevelopment>
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
import com.mcmoddev.mmdbot.thelistener.util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GenericGuildMemberEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.mcmoddev.mmdbot.thelistener.util.Utils.mentionable;

public final class RoleEvents extends ListenerAdapter {

    @Override
    public void onGuildMemberRoleAdd(@NotNull final GuildMemberRoleAddEvent event) {
        processEvent(event, "Added", event.getRoles(), List::removeAll); // Just in case if the member has already been updated with its new role list
    }

    @Override
    public void onGuildMemberRoleRemove(@NotNull final GuildMemberRoleRemoveEvent event) {
        processEvent(event, "Removed", event.getRoles(), List::addAll); // Just in case if the member has already been updated with its new role list
    }

    private static void processEvent(GenericGuildMemberEvent event, String changeType, List<Role> modifiedRoles,
                                     BiConsumer<List<Role>, List<Role>> roleListsConsumer) {
        // The role list consumer allows the caller to update the previous roles list with the modified roles list,
        // to guard against the case where the target's roles list has been updated before we receive the event
        // We could also check JDA's impl to see if the event always fires before the target's roles list is updated and
        // get rid of this, but :shrug:

        final var roleIds = modifiedRoles.stream().map(Role::getIdLong).toList();
        final var noLoggingRoles = TheListener.getInstance().getConfigForGuild(event.getGuild().getIdLong()).getNoLoggingRoles();
        if (noLoggingRoles.stream().map(SnowflakeValue::asLong).anyMatch(roleIds::contains)) {
            return;
        }

        final List<Role> previousRoles = new ArrayList<>(event.getMember().getRoles());
        roleListsConsumer.accept(previousRoles, modifiedRoles);

        final var target = event.getMember();
        Utils.getAuditLog(event.getGuild(), target.getIdLong(), log -> log.type(ActionType.MEMBER_ROLE_UPDATE).limit(5),
            entry -> buildAndSendMessage(event, entry, target, changeType, previousRoles, modifiedRoles));
    }

    private static void buildAndSendMessage(GenericGuildMemberEvent event, AuditLogEntry entry, Member target, String changeType,
                                            List<Role> previousRoles, List<Role> modifiedRoles) {
        final var embed = new EmbedBuilder();

        embed.setTitle("User Role(s) " + changeType)
            .setColor(Color.YELLOW)
            .addField("User:", target.getUser().getAsTag(), true)
            .setFooter("User ID: " + target.getUser().getId(), target.getEffectiveAvatarUrl())
            .setTimestamp(entry.getTimeCreated());

        final var targetId = entry.getTargetIdLong();

        if (targetId != target.getIdLong()) {
            TheListener.LOGGER.warn("Inconsistency between target of retrieved audit log entry and actual role event target: " +
                "retrieved is {}, but target is {}", targetId, target);
        } else if (entry.getUser() != null) {
            final var editor = entry.getUser();
            embed.addField("Editor:", editor.getAsTag(), true);
        }

        embed.addField("Previous Role(s):", mentionsOrEmpty(previousRoles), true);
        embed.addField(changeType + " Role(s):", mentionsOrEmpty(modifiedRoles), true);

        LoggingType.ROLE_EVENTS.getChannels(event.getGuild().getIdLong()).forEach(id -> {
            final var ch = id.resolve(idL -> event.getJDA().getChannelById(MessageChannel.class, idL));
            if (ch != null) {
                ch.sendMessageEmbeds(embed.build()).queue();
            }
        });
    }

    public static String mentionsOrEmpty(List<? extends IMentionable> list) {
        final String str = list.stream().map(IMentionable::getAsMention).collect(Collectors.joining(" "));
        return str.isBlank() ? "_None_" : str;
    }
}
