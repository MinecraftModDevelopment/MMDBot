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

import com.mcmoddev.mmdbot.core.event.customlog.TrickEvent;
import com.mcmoddev.mmdbot.thelistener.TheListener;
import com.mcmoddev.mmdbot.thelistener.util.LoggingType;
import com.mcmoddev.mmdbot.thelistener.util.Utils;
import discord4j.common.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.AllowedMentions;
import io.github.matyrobbrt.eventdispatcher.SubscribeEvent;
import lombok.NonNull;

import java.time.Instant;

public class TrickEvents {
    public static final AllowedMentions ALLOWED_MENTIONS_DATA = AllowedMentions.builder().repliedUser(false).build();
    private TrickEvents() {}

    @SubscribeEvent
    public static void onTrickAdded(@NonNull final TrickEvent.Add event) {
        if (TheListener.getClient() == null) return;
        final var guildId = Snowflake.of(event.getGuildId());
        final var type = event.getTrickType();
        TheListener.getClient().getMemberById(guildId, Snowflake.of(event.getResponsibleUserId()))
            .subscribe(member -> {
                final var embed = EmbedCreateSpec.builder()
                    .title("Trick Added")
                    .author(member.getUsername(), null, member.getAvatarUrl())
                    .description("""
                        <@%s> added a new trick of the type **%s**!

                        **Trick Content**:
                        ```%s
                        %s
                        ```"""
                        .formatted(event.getResponsibleUserId(), type, type.equals("script") ? "js" : "",
                            reduceString(event.getContent(), 5000)))
                    .addField("Trick Names", String.join(", ", event.getTrickNames()), false)
                    .timestamp(Instant.now())
                    .build();

                Utils.executeInLoggingChannel(guildId, LoggingType.TRICK_EVENTS,
                    channel -> channel.createMessage(MessageCreateSpec.builder()
                        .embeds(embed)
                        .allowedMentions(ALLOWED_MENTIONS_DATA).build()).subscribe(e -> {
                    }, t -> {}));
            });
    }

    @SubscribeEvent
    public static void onTrickRemoved(@NonNull final TrickEvent.Remove event) {
        if (TheListener.getClient() == null) return;
        final var guildId = Snowflake.of(event.getGuildId());
        final var type = event.getTrickType();
        TheListener.getClient().getMemberById(guildId, Snowflake.of(event.getResponsibleUserId()))
            .subscribe(member -> {
                final var embed = EmbedCreateSpec.builder()
                    .title("Trick Removed")
                    .author(member.getUsername(), null, member.getAvatarUrl())
                    .description("""
                        <@%s> removed a trick of the type **%s**!

                        **Old Trick Content**:
                        ```%s
                        %s
                        ```""".formatted(event.getResponsibleUserId(), type, type.equals("script") ? "js" : "",
                        reduceString(event.getContent(), 5000)))
                    .addField("Old Trick Names", String.join(", ", event.getTrickNames()), false)
                    .timestamp(Instant.now())
                    .build();

                Utils.executeInLoggingChannel(guildId, LoggingType.TRICK_EVENTS,
                    channel -> channel.createMessage(MessageCreateSpec.builder()
                        .embeds(embed)
                        .allowedMentions(ALLOWED_MENTIONS_DATA).build()).subscribe(e -> {
                    }, t -> {}));
            });
    }

    @SubscribeEvent
    public static void onTrickEdit(@NonNull final TrickEvent.Edit event) {
        if (TheListener.getClient() == null) return;
        final var guildId = Snowflake.of(event.getGuildId());
        final var newType = event.getTrickType();
        final var oldType = event.getOldTrickType();
        TheListener.getClient().getMemberById(guildId, Snowflake.of(event.getResponsibleUserId()))
            .subscribe(member -> {
                final var embed = EmbedCreateSpec.builder()
                    .title("Trick Edited")
                    .author(member.getUsername(), null, member.getAvatarUrl())
                    .description("""
                        <@%s> edited a trick whose old type was **%s**, and new type is **%s**!

                        **Old Trick Content**:
                        ```%s
                        %s
                        ```
                        **New Trick Content**:
                        ```%s
                        %s
                        ```
                        """.formatted(event.getResponsibleUserId(), oldType, newType,
                        oldType.equals("script") ? "js" : "", reduceString(event.getOldContent(), 2050),
                        newType.equals("script") ? "js" : "", reduceString(event.getContent(), 2050)))
                    .addField("Old Trick Names", String.join(", ", event.getOldTrickNames()), false)
                    .addField("New Trick Names", String.join(", ", event.getTrickNames()), false)
                    .build();

                Utils.executeInLoggingChannel(guildId, LoggingType.TRICK_EVENTS,
                    channel -> channel.createMessage(MessageCreateSpec.builder()
                        .embeds(embed)
                        .allowedMentions(ALLOWED_MENTIONS_DATA).build()).subscribe(e -> {
                    }, t -> {}));
            });
    }

    private static String reduceString(final String string, final int limit) {
        final var actualLimit = limit - 3;
        if (string.length() > actualLimit) {
            return string.substring(0, actualLimit - 1) + "...";
        }
        return string;
    }
}
