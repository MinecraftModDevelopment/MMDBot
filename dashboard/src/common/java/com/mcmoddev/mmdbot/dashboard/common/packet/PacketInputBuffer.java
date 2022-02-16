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

public interface PacketInputBuffer {

    int readInt();

    String readString();

    /**
     * Reads an enum of the given type {@code T} from the buffer
     * using the ordinal encoded.
     */
    <T extends Enum<T>> T readEnum(Class<T> enumClass);

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
            public <T extends Enum<T>> T readEnum(final Class<T> enumClass) {
                return (enumClass.getEnumConstants())[input.readVarInt(true)];
            }
        };
    }
}
