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
package com.mcmoddev.mmdbot.logging.util;

import com.mcmoddev.mmdbot.logging.LoggingBot;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.core.object.entity.Message;
import discord4j.rest.entity.RestChannel;

import java.util.function.Consumer;

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

    public static void executeInLoggingChannel(final Snowflake guild, LoggingType type, Consumer<RestChannel> consumer) {
        if (LoggingBot.getClient() != null) {
            // Hardcoded until configs
            final var channels = type.getChannels(guild);
            channels.forEach(channelId -> {
                final var channel = LoggingBot.getClient().getChannelById(channelId);
                consumer.accept(channel);
            });
        }
    }

    public static String createMessageURL(final Message message) {
        return "https://discord.com/channels/" + message.getGuildId().map(Snowflake::asString)
            .orElse("@me")+ "/" + message.getChannelId().asLong()
            + "/" + message.getId().asLong();
    }

}