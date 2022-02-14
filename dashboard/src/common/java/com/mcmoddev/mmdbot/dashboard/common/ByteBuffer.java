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
package com.mcmoddev.mmdbot.dashboard.common;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import lombok.experimental.Delegate;

import java.nio.charset.StandardCharsets;

public class ByteBuffer extends ByteBuf {

    @Delegate
    private final ByteBuf source;

    public ByteBuffer(final ByteBuf source) {
        this.source = source;
    }

    @Override
    public int hashCode() {
        return this.source.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return this.source.equals(other) && other instanceof ByteBuffer;
    }

    @Override
    public String toString() {
        return this.source.toString();
    }

    public int readVarInt() {
        int i = 0;
        int j = 0;

        byte b0;
        do {
            b0 = this.readByte();
            i |= (b0 & 127) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while ((b0 & 128) == 128);

        return i;
    }

    public ByteBuffer writeVarInt(int value) {
        while ((value & -128) != 0) {
            this.writeByte(value & 127 | 128);
            value >>>= 7;
        }

        this.writeByte(value);
        return this;
    }

    /**
     * Calculates the number of bytes ({@code [1-5]}) required to fit the supplied int if it were to be read/written
     * using readVarInt/writeVarInt
     */
    public static int getVarIntSize(int pInput) {
        for(int i = 1; i < 5; ++i) {
            if ((pInput & -1 << i * 7) == 0) {
                return i;
            }
        }

        return 5;
    }

    /**
     * Reads a string from the buffer with a maximum length of {@code Short.MAX_VALUE}.
     *
     * @see #readUtf(int)
     * @see #writeUtf
     */
    public String readUtf() {
        return readUtf(32767);
    }

    /**
     * Reads a string from the buffer with a maximum length from this buffer.
     *
     * @see #writeUtf
     */
    public String readUtf(int maxLength) {
        int i = this.readVarInt();
        if (i > maxLength * 4) {
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + i + " > " + maxLength * 4 + ")");
        } else if (i < 0) {
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        } else {
            String s = this.toString(this.readerIndex(), i, StandardCharsets.UTF_8);
            this.readerIndex(this.readerIndex() + i);
            if (s.length() > maxLength) {
                throw new DecoderException("The received string length is longer than maximum allowed (" + i + " > " + maxLength + ")");
            } else {
                return s;
            }
        }
    }

    /**
     * Writes a string to the buffer with a maximum length of {@code Short.MAX_VALUE}.
     *
     * @see #readUtf
     */
    public ByteBuf writeUtf(String str) {
        return writeUtf(str, 32767);
    }

    /**
     * Writes a string to the buffer with a maximum length.
     *
     * @see #readUtf
     */
    public ByteBuffer writeUtf(String str, int maxLength) {
        byte[] abyte = str.getBytes(StandardCharsets.UTF_8);
        if (abyte.length > maxLength) {
            throw new EncoderException("String too big (was " + abyte.length + " bytes encoded, max " + maxLength + ")");
        } else {
            this.writeVarInt(abyte.length);
            this.writeBytes(abyte);
            return this;
        }
    }

    /**
     * Writes an enum of the given type to the buffer
     * using the ordinal encoded.
     *
     * @see #readEnum
     */
    public ByteBuffer writeEnum(Enum<?> value) {
        return writeVarInt(value.ordinal());
    }

    /**
     * Reads an enum of the given type {@code T} from the buffer
     * using the ordinal encoded.
     *
     * @see #writeEnum
     */
    public <T extends Enum<T>> T readEnum(Class<T> enumClass) {
        return (enumClass.getEnumConstants())[this.readVarInt()];
    }

}
