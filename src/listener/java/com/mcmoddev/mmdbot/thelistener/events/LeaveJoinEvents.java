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
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.time.Instant;

public final class LeaveJoinEvents extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(@NotNull final GuildMemberJoinEvent event) {
        final var embed = new EmbedBuilder()
            .setTimestamp(Instant.now())
            .setColor(Color.GREEN)
            .setTitle("User Joined")
            .setFooter("User ID: " + event.getMember().getId())
            .addField("User:", "%s (%s)".formatted(event.getMember().getUser().getAsTag(), event.getMember().getAsMention()), true)
            .setThumbnail(event.getMember().getEffectiveAvatarUrl())
            .addField("Joined Discord:", TimeFormat.DATE_TIME_SHORT.format(event.getMember().getUser().getTimeCreated()), true)
            .build();

        TheListener.getInstance().getConfigForGuild(event.getGuild().getIdLong())
            .getChannelsForLogging(LoggingType.LEAVE_JOIN_EVENTS)
            .forEach(snowflakeValue -> {
                final var ch = snowflakeValue.resolve(id -> event.getJDA().getChannelById(MessageChannel.class, id));
                if (ch != null) {
                    ch.sendMessageEmbeds(embed).queue();
                }
            });
    }

    @Override
    public void onGuildMemberRemove(@NotNull final GuildMemberRemoveEvent event) {
        if (event.getMember() == null) return;
        final var embed = new EmbedBuilder()
            .setTimestamp(Instant.now())
            .setColor(Color.RED)
            .setTitle("User Left")
            .setFooter("User ID: " + event.getMember().getId(), event.getMember().getEffectiveAvatarUrl())
            .addField("User:", event.getUser().getAsTag(), true)
            .addField("Join Time:", TimeFormat.DATE_TIME_SHORT.format(event.getMember().getUser().getTimeCreated()), true)
            .addField("Roles", String.join(" ", event.getMember().getRoles().stream().map(Role::getAsMention).toList()), false)
            .setThumbnail(event.getUser().getAvatarUrl())
            .build();

        TheListener.getInstance().getConfigForGuild(event.getGuild().getIdLong())
            .getChannelsForLogging(LoggingType.LEAVE_JOIN_EVENTS)
            .forEach(snowflakeValue -> {
                final var ch = snowflakeValue.resolve(id -> event.getJDA().getChannelById(MessageChannel.class, id));
                if (ch != null) {
                    ch.sendMessageEmbeds(embed).queue();
                }
            });
    }
}
