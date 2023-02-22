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
package com.mcmoddev.mmdbot.thelistener.util;

import com.mcmoddev.mmdbot.core.util.config.SnowflakeValue;
import com.mcmoddev.mmdbot.thelistener.TheListener;

import java.util.List;

public enum LoggingType {

    MESSAGE_EVENTS("message_events", configGetter(0)),
    LEAVE_JOIN_EVENTS("leave_join_events", configGetter(1)),
    MODERATION_EVENTS("moderation_events", configGetter(2)),
    ROLE_EVENTS("role_events", configGetter(3)),
    TRICK_EVENTS("trick_events", configGetter(4));

    private final String name;
    private final ChannelGetter channelGetter;

    LoggingType(final String name, final ChannelGetter channelGetter) {
        this.name = name;
        this.channelGetter = channelGetter;
    }

    public List<SnowflakeValue> getChannels(long guild) {
        return channelGetter.getChannels(guild);
    }

    public String getName() {
        return name;
    }

    @FunctionalInterface
    public interface ChannelGetter {

        List<SnowflakeValue> getChannels(long guild);

    }

    private static ChannelGetter configGetter(int index) {
        return s -> TheListener.getInstance().getConfigForGuild(s).getChannelsForLogging(values()[index]);
    }
}
