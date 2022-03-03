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
package com.mcmoddev.mmdbot.dashboard.util;

import com.mcmoddev.mmdbot.dashboard.common.BufferDecoder;
import com.mcmoddev.mmdbot.dashboard.common.BufferSerializable;
import com.mcmoddev.mmdbot.dashboard.common.BufferSerializers;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketOutputBuffer;

public record BotUserData(String username, String discriminator, String avatarUrl) implements BufferSerializable {

    public static final BufferDecoder<BotUserData> DECODER = buffer -> {
        final var username = buffer.readString();
        final var discriminator = buffer.readString();
        final var avatarUrl = buffer.readString();
        return new BotUserData(username, discriminator, avatarUrl);
    };

    @Override
    public void encode(final PacketOutputBuffer buffer) {
        buffer.writeString(username);
        buffer.writeString(discriminator);
        buffer.writeString(avatarUrl);
    }
}
