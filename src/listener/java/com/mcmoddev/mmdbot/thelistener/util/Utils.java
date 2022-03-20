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
package com.mcmoddev.mmdbot.thelistener.util;

import com.mcmoddev.mmdbot.thelistener.TheListener;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.core.object.audit.AuditLogEntry;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.AuditLogQueryFlux;
import discord4j.rest.entity.RestChannel;
import reactor.core.publisher.Mono;

import java.io.Serial;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public final class Utils {

    public static void subscribe(final GatewayDiscordClient client, EventListener... toSubscribe) {
        for (final var eventListener : toSubscribe) {
            client.getEventDispatcher().on(Event.class).subscribe(eventListener::onEvent);
        }
    }

    /**
     * Sets the thread's daemon property to the specified {@code isDaemon} and returns it
     *
     * @param thread   the thread to modify
     * @param isDaemon if the thread should be daemon
     * @return the modified thread
     */
    public static Thread setThreadDaemon(final Thread thread, final boolean isDaemon) {
        thread.setDaemon(isDaemon);
        return thread;
    }

    public static void executeInLoggingChannel(final Snowflake guild, LoggingType type, Consumer<MessageChannel> consumer) {
        if (TheListener.getClient() != null) {
            // Hardcoded until configs
            final var channels = type.getChannels(guild);
            channels.forEach(channelId -> {
                TheListener.getClient().getChannelById(channelId)
                    .subscribe(channel -> {
                       if (channel instanceof MessageChannel mc) {
                           consumer.accept(mc);
                       }
                    });
            });
        }
    }

    public static String createMessageURL(final Message message) {
        return "https://discord.com/channels/" + message.getGuildId().map(Snowflake::asString)
            .orElse("@me") + "/" + message.getChannelId().asLong()
            + "/" + message.getId().asLong();
    }

    public static String mentionAndID(final long id) {
        return "<@" + id + "> (" + id + ")";
    }

    public static void getAuditLog(final Guild guild, final long targetId, UnaryOperator<AuditLogQueryFlux> modifier, Consumer<AuditLogEntry> consumer) {
        getAuditLog(guild, targetId, modifier, consumer, () -> {
        });
    }

    public static void getAuditLog(final Guild guild, final long targetId, UnaryOperator<AuditLogQueryFlux> modifier, Consumer<AuditLogEntry> consumer, Runnable orElse) {
        modifier.apply(guild.getAuditLog())
            .map(l -> l.getEntries().stream().filter(log -> log.getTargetId().map(Snowflake::asLong).orElse(0L).equals(targetId)).findAny())
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toStream()
            .limit(1)
            .findFirst()
            .ifPresentOrElse(consumer, orElse);
    }

}
