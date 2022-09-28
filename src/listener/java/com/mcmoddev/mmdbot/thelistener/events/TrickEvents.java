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

import com.mcmoddev.mmdbot.core.event.customlog.TrickEvent;
import com.mcmoddev.mmdbot.thelistener.TheListener;
import com.mcmoddev.mmdbot.thelistener.util.LoggingType;
import io.github.matyrobbrt.eventdispatcher.SubscribeEvent;
import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.time.Instant;

public class TrickEvents {

    private TrickEvents() {
    }

    @SubscribeEvent
    public static void onTrickAdded(@NonNull final TrickEvent.Add event) {
        if (TheListener.getInstance() == null) return;
        final var type = event.getTrickType();
        TheListener.getInstance().getJDA().retrieveUserById(event.getResponsibleUserId())
            .queue(member -> {
                final var embed = new EmbedBuilder()
                    .setTitle("Trick Added")
                    .setAuthor(member.getName(), null, member.getAvatarUrl())
                    .setDescription("""
                        <@%s> added a new trick of the type **%s**!

                        **Trick Content**:
                        ```%s
                        %s
                        ```"""
                        .formatted(event.getResponsibleUserId(), type, type.equals("script") ? "js" : "",
                            reduceString(event.getContent(), 5000)))
                    .addField("Trick Names", String.join(", ", event.getTrickNames()), false)
                    .setTimestamp(Instant.now())
                    .build();

                TheListener.getInstance().getConfigForGuild(event.getGuildId())
                    .getChannelsForLogging(LoggingType.TRICK_EVENTS)
                    .forEach(snowflakeValue -> {
                        final var ch = snowflakeValue.resolve(id -> member.getJDA().getChannelById(MessageChannel.class, id));
                        if (ch != null) {
                            ch.sendMessageEmbeds(embed).queue();
                        }
                    });
            });
    }

    @SubscribeEvent
    public static void onTrickRemoved(@NonNull final TrickEvent.Remove event) {
        if (TheListener.getInstance() == null) return;
        final var type = event.getTrickType();
        TheListener.getInstance().getJDA().retrieveUserById(event.getResponsibleUserId())
            .queue(member -> {
                final var embed = new EmbedBuilder()
                    .setTitle("Trick Removed")
                    .setAuthor(member.getName(), null, member.getAvatarUrl())
                    .setDescription("""
                        <@%s> removed a trick of the type **%s**!

                        **Old Trick Content**:
                        ```%s
                        %s
                        ```""".formatted(event.getResponsibleUserId(), type, type.equals("script") ? "js" : "",
                        reduceString(event.getContent(), 5000)))
                    .addField("Old Trick Names", String.join(", ", event.getTrickNames()), false)
                    .setTimestamp(Instant.now())
                    .build();

                TheListener.getInstance().getConfigForGuild(event.getGuildId())
                    .getChannelsForLogging(LoggingType.TRICK_EVENTS)
                    .forEach(snowflakeValue -> {
                        final var ch = snowflakeValue.resolve(id -> member.getJDA().getChannelById(MessageChannel.class, id));
                        if (ch != null) {
                            ch.sendMessageEmbeds(embed).queue();
                        }
                    });
            });
    }

    @SubscribeEvent
    public static void onTrickEdit(@NonNull final TrickEvent.Edit event) {
        if (TheListener.getInstance() == null) return;
        final var newType = event.getTrickType();
        final var oldType = event.getOldTrickType();
        TheListener.getInstance().getJDA().retrieveUserById(event.getResponsibleUserId())
            .queue(member -> {
                final var embed = new EmbedBuilder()
                    .setTitle("Trick Edited")
                    .setAuthor(member.getName(), null, member.getAvatarUrl())
                    .setDescription("""
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

                TheListener.getInstance().getConfigForGuild(event.getGuildId())
                    .getChannelsForLogging(LoggingType.TRICK_EVENTS)
                    .forEach(snowflakeValue -> {
                        final var ch = snowflakeValue.resolve(id -> member.getJDA().getChannelById(MessageChannel.class, id));
                        if (ch != null) {
                            ch.sendMessageEmbeds(embed).queue();
                        }
                    });
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
