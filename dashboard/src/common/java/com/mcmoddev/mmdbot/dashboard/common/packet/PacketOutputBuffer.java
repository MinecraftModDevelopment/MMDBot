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

import com.esotericsoftware.kryo.io.Output;
import com.mcmoddev.mmdbot.dashboard.common.BufferSerializable;

import java.util.List;
import java.util.function.BiConsumer;

public interface PacketOutputBuffer {

    void writeInt(int value);

    void writeVarInt(int value, boolean optimizePositive);

    void writeString(String str);

    /**
     * Writes an enum of the given type to the buffer
     * using the ordinal encoded.
     */
    default void writeEnum(Enum<?> value) {
        writeVarInt(value.ordinal(), true);
    }

    /**
     * Writes the list to the buffer using the {@code encoder}.
     *
     * @param list    the list to write
     * @param encoder the encoder used to write items
     */
    default <T> void writeList(List<T> list, BiConsumer<T, PacketOutputBuffer> encoder) {
        writeVarInt(list.size(), true);
        for (T t : list) {
            encoder.accept(t, this);
        }
    }

    default void write(BufferSerializable serializable) {
        serializable.encode(this);
    }

    static PacketOutputBuffer fromOutput(Output output) {
        return new PacketOutputBuffer() {
            @Override
            public void writeInt(final int value) {
                output.writeInt(value);
            }

            @Override
            public void writeString(final String str) {
                output.writeString(str);
            }

            @Override
            public void writeVarInt(final int value, final boolean optimizePositive) {
                output.writeVarInt(value, optimizePositive);
            }
        };
    }
}
