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
package com.mcmoddev.mmdbot.logging.events;

import com.mcmoddev.mmdbot.logging.util.ListenerAdapter;
import com.mcmoddev.mmdbot.logging.util.LoggingType;
import com.mcmoddev.mmdbot.logging.util.Utils;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.PartialMember;
import discord4j.core.object.entity.Role;
import discord4j.core.spec.EmbedCreateSpec;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.Arrays;

public final class LeaveJoinEvents extends ListenerAdapter {

    @Override
    public void onMemberJoin(final MemberJoinEvent event) {
        final var embed = EmbedCreateSpec.builder()
            .timestamp(Instant.now())
            .title("User Joined")
            .footer("User ID: " + event.getMember().getId().asLong(), null)
            .addField("User:", event.getMember().getTag(), true)
            .thumbnail(event.getMember().getAvatarUrl())
            .addField("Joined Discord:", "<t:%s:f>".formatted(event.getMember().getId().getTimestamp()
                .getEpochSecond()), true)
            .build();

        Utils.executeInLoggingChannel(event.getGuildId(), LoggingType.LEAVE_JOIN_EVENTS, c ->
            c.createMessage(embed.asRequest()).subscribe());
    }

    @Override
    public void onMemberLeave(final MemberLeaveEvent event) {
        final var embed = EmbedCreateSpec.builder()
            .timestamp(Instant.now())
            .title("User Left")
            .footer("User ID: " + event.getUser().getId().asLong(), null)
            .addField("User:", event.getUser().getTag(), true)
            .addField("Join Time:", event.getMember().flatMap(PartialMember::getJoinTime)
                .map(i -> "<t:%s:f>".formatted(i.getEpochSecond())).orElse("Join time could not be determined!"), true)
            .addField("Roles", Arrays.toString(event.getMember().map(Member::getRoles)
                .map(roleFlux -> roleFlux.map(Role::getMention)).orElse(Flux.empty()).toStream().toArray(String[]::new)), false)
            .thumbnail(event.getUser().getAvatarUrl())
            .build();

        Utils.executeInLoggingChannel(event.getGuildId(), LoggingType.LEAVE_JOIN_EVENTS, c ->
            c.createMessage(embed.asRequest()).subscribe());
    }
}
