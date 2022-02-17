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
package com.mcmoddev.mmdbot.dashboard.common.packet;

import com.mcmoddev.mmdbot.dashboard.common.BufferDecoder;
import com.mcmoddev.mmdbot.dashboard.common.BufferSerializable;
import com.mcmoddev.mmdbot.dashboard.common.BufferSerializers;

import java.util.Objects;
import java.util.Random;

public record PacketID(int intId, String stringId) implements BufferSerializable {

    private static final Random RANDOM = new Random();

    public static PacketID generateRandom() {
        return generateRandom(Short.MAX_VALUE, 4);
    }

    public static PacketID generateRandom(int intLimit, int stringLength) {
        int leftLimit = 65; // letter 'A'
        int rightLimit = 122; // letter 'z'

        final var string = RANDOM.ints(leftLimit, rightLimit + 1)
            .filter(i -> i <= 90 || i >= 97)
            .limit(stringLength)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();

        return new PacketID(RANDOM.nextInt(intLimit), string);
    }

    public static final BufferDecoder<PacketID> DECODER;

    static {
        DECODER = BufferSerializers.registerDecoder(PacketID.class,
            buffer -> new PacketID(buffer.readVarInt(true), buffer.readString()));
    }

    @Override
    public void encode(final PacketOutputBuffer buffer) {
        buffer.writeVarInt(intId, true);
        buffer.writeString(stringId);
    }

    @Override
    public String toString() {
        return intId + stringId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PacketID packetID = (PacketID) o;
        return intId == packetID.intId && Objects.equals(stringId, packetID.stringId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(intId, stringId);
    }
}
