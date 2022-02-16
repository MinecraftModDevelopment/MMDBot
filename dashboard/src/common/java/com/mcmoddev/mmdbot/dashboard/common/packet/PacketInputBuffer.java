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

import com.esotericsoftware.kryo.io.Input;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public interface PacketInputBuffer {

    /**
     * Reads the next int from the buffer.
     */
    int readInt();

    /**
     * Reads a 1-5 byte int. It is guaranteed that a variable length encoding will be used.
     */
    int readVarInt(boolean optimizePositive);

    /**
     * Reads the next string from the buffer.
     */
    String readString();

    /**
     * Reads an enum of the given type {@code T} from the buffer
     * using the ordinal encoded.
     */
    default <T extends Enum<T>> T readEnum(Class<T> enumClass) {
        return (enumClass.getEnumConstants())[readVarInt(true)];
    }

    /**
     * Reads a list from the buffer, using the specified {@code decoder}. <br>
     * The returned list is <b>immutable</b>.
     */
    default <T> List<T> readList(Function<PacketInputBuffer, T> decoder) {
        final var size = readVarInt(true);
        final var list = new ArrayList<T>();
        for (int i = 0; i < size; i++) {
            list.add(decoder.apply(this));
        }
        return List.copyOf(list);
    }

    static PacketInputBuffer fromInput(Input input) {
        return new PacketInputBuffer() {
            @Override
            public int readInt() {
                return input.readInt();
            }

            @Override
            public String readString() {
                return input.readString();
            }

            @Override
            public int readVarInt(final boolean optimizePositive) {
                return input.readVarInt(optimizePositive);
            }
        };
    }
}
