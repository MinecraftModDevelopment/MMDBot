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

public interface PacketOutputBuffer {

    void writeInt(int value);

    void writeString(String str);

    /**
     * Writes an enum of the given type to the buffer
     * using the ordinal encoded.
     */
    void writeEnum(Enum<?> value);

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
            public void writeEnum(final Enum<?> value) {
                output.writeVarInt(value.ordinal(), true);
            }
        };
    }
}
