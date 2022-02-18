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

import discord4j.common.util.Snowflake;

import java.util.Set;

public enum LoggingType {

    // TODO yes, config
    MESSAGE_EVENTS("message_events", s -> Set.of()),
    LEAVE_JOIN_EVENTS("leave_join_events", s -> Set.of()),
    MODERATION_EVENTS("moderation_events", s -> Set.of()),
    ROLE_EVENTS("role_events", s -> Set.of());

    private final String name;
    private final ChannelGetter channelGetter;

    LoggingType(final String name, final ChannelGetter channelGetter) {
        this.name = name;
        this.channelGetter = channelGetter;
    }

    public Set<Snowflake> getChannels(Snowflake guild) {
        return channelGetter.getChannels(guild);
    }

    public String getName() {
        return name;
    }

    @FunctionalInterface
    public interface ChannelGetter {

        Set<Snowflake> getChannels(Snowflake guild);

    }
}
