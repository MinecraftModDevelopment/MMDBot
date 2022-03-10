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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketID;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.experimental.Delegate;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;

public class ByteBuffer extends ByteBuf {
    @Delegate
    private final ByteBuf source;
    public ByteBuffer(final ByteBuf source) {
        this.source = source;
    }

    /**
     * Read a collection from this buffer. First a new collection is created given the number of elements using {@code
     * collectionFactory}.
     * Then every element is read using {@code elementReader}.
     *
     * @see #writeCollection
     */
    public <T, C extends Collection<T>> C readCollection(IntFunction<C> pCollectionFactory, Function<ByteBuffer, T> pElementReader) {
        int i = this.readVarInt();
        C c = pCollectionFactory.apply(i);

        for(int j = 0; j < i; ++j) {
            c.add(pElementReader.apply(this));
        }

        return c;
    }

    /**
     * Write a collection to this buffer. Every element is encoded in order using {@code elementWriter}.
     *
     * @see #readCollection
     */
    public <T> void writeCollection(Collection<T> pCollection, BiConsumer<ByteBuffer, T> pElementWriter) {
        this.writeVarInt(pCollection.size());

        for(T t : pCollection) {
            pElementWriter.accept(this, t);
        }

    }

    /**
     * Read a List from this buffer. First a new list is created given the number of elements.
     * Then every element is read using {@code elementReader}.
     *
     * @see #writeCollection
     */
    public <T> List<T> readList(Function<ByteBuffer, T> pElementReader) {
        return this.readCollection(Lists::newArrayListWithCapacity, pElementReader);
    }

    /**
     * Read an IntList of VarInts from this buffer.
     *
     * @see #writeIntIdList
     */
    public IntList readIntIdList() {
        int i = this.readVarInt();
        IntList intlist = new IntArrayList();

        for(int j = 0; j < i; ++j) {
            intlist.add(this.readVarInt());
        }

        return intlist;
    }

    /**
     * Write an IntList to this buffer. Every element is encoded as a VarInt.
     *
     * @see #readIntIdList
     */
    public void writeIntIdList(IntList p_178346_) {
        this.writeVarInt(p_178346_.size());
        p_178346_.forEach((java.util.function.IntConsumer)this::writeVarInt);
    }

    /**
     * Read a Map from this buffer. First a new Map is created given the number of elements using {@code mapFactory}.
     * Then all keys and values are read using the given {@code keyReader} and {@code valueReader}.
     *
     * @see #writeMap
     */
    public <K, V, M extends Map<K, V>> M readMap(IntFunction<M> pMapFactory, Function<ByteBuffer, K> pKeyReader, Function<ByteBuffer, V> pValueReader) {
        int i = this.readVarInt();
        M m = pMapFactory.apply(i);

        for(int j = 0; j < i; ++j) {
            K k = pKeyReader.apply(this);
            V v = pValueReader.apply(this);
            m.put(k, v);
        }

        return m;
    }

    /**
     * Read a Map from this buffer. First a new HashMap is created.
     * Then all keys and values are read using the given {@code keyReader} and {@code valueReader}.
     *
     * @see #writeMap
     */
    public <K, V> Map<K, V> readMap(Function<ByteBuffer, K> pKeyReader, Function<ByteBuffer, V> pValueReader) {
        return this.readMap(Maps::newHashMapWithExpectedSize, pKeyReader, pValueReader);
    }

    /**
     * Write a Map to this buffer. First the size of the map is written as a VarInt.
     * Then all keys and values are written using the given {@code keyWriter} and {@code valueWriter}.
     *
     * @see #readMap
     */
    public <K, V> void writeMap(Map<K, V> pMap, BiConsumer<ByteBuffer, K> pKeyWriter, BiConsumer<ByteBuffer, V> pValueWriter) {
        this.writeVarInt(pMap.size());
        pMap.forEach((p_178362_, p_178363_) -> {
            pKeyWriter.accept(this, p_178362_);
            pValueWriter.accept(this, p_178363_);
        });
    }

    /**
     * Read a VarInt N from this buffer, then reads N values by calling {@code reader}.
     */
    public void readWithCount(Consumer<ByteBuffer> pReader) {
        int i = this.readVarInt();

        for(int j = 0; j < i; ++j) {
            pReader.accept(this);
        }

    }

    public <T> void writeOptional(Optional<T> p_182688_, BiConsumer<ByteBuffer, T> p_182689_) {
        if (p_182688_.isPresent()) {
            this.writeBoolean(true);
            p_182689_.accept(this, p_182688_.get());
        } else {
            this.writeBoolean(false);
        }

    }

    public <T> Optional<T> readOptional(Function<ByteBuffer, T> p_182699_) {
        return this.readBoolean() ? Optional.of(p_182699_.apply(this)) : Optional.empty();
    }

    public byte[] readByteArray() {
        return this.readByteArray(this.readableBytes());
    }

    public ByteBuffer writeByteArray(byte[] pArray) {
        this.writeVarInt(pArray.length);
        this.writeBytes(pArray);
        return this;
    }

    public byte[] readByteArray(int pMaxLength) {
        int i = this.readVarInt();
        if (i > pMaxLength) {
            throw new DecoderException("ByteArray with size " + i + " is bigger than allowed " + pMaxLength);
        } else {
            byte[] abyte = new byte[i];
            this.readBytes(abyte);
            return abyte;
        }
    }

    /**
     * Writes an array of VarInts to the buffer, prefixed by the length of the array (as a VarInt).
     *
     * @see #readVarIntArray
     */
    public ByteBuffer writeVarIntArray(int[] pArray) {
        this.writeVarInt(pArray.length);

        for(int i : pArray) {
            this.writeVarInt(i);
        }

        return this;
    }

    /**
     * Reads an array of VarInts from this buffer.
     *
     * @see #writeVarIntArray
     */
    public int[] readVarIntArray() {
        return this.readVarIntArray(this.readableBytes());
    }

    /**
     * Reads an array of VarInts with a maximum length from this buffer.
     *
     * @see #writeVarIntArray
     */
    public int[] readVarIntArray(int pMaxLength) {
        int i = this.readVarInt();
        if (i > pMaxLength) {
            throw new DecoderException("VarIntArray with size " + i + " is bigger than allowed " + pMaxLength);
        } else {
            int[] aint = new int[i];

            for(int j = 0; j < aint.length; ++j) {
                aint[j] = this.readVarInt();
            }

            return aint;
        }
    }

    /**
     * Writes an array of longs to the buffer, prefixed by the length of the array (as a VarInt).
     *
     * @see #readLongArray
     */
    public ByteBuffer writeLongArray(long[] pArray) {
        this.writeVarInt(pArray.length);

        for(long i : pArray) {
            this.writeLong(i);
        }

        return this;
    }

    /**
     * Reads a length-prefixed array of longs from the buffer.
     */
    public long[] readLongArray() {
        return this.readLongArray((long[])null);
    }

    /**
     * Reads a length-prefixed array of longs from the buffer.
     * Will try to use the given long[] if possible. Note that if an array with the correct size is given, maxLength is
     * ignored.
     */
    public long[] readLongArray(@Nullable long[] pArray) {
        return this.readLongArray(pArray, this.readableBytes() / 8);
    }

    /**
     * Reads a length-prefixed array of longs with a maximum length from the buffer.
     * Will try to use the given long[] if possible. Note that if an array with the correct size is given, maxLength is
     * ignored.
     */
    public long[] readLongArray(@Nullable long[] pArray, int pMaxLength) {
        int i = this.readVarInt();
        if (pArray == null || pArray.length != i) {
            if (i > pMaxLength) {
                throw new DecoderException("LongArray with size " + i + " is bigger than allowed " + pMaxLength);
            }

            pArray = new long[i];
        }

        for(int j = 0; j < pArray.length; ++j) {
            pArray[j] = this.readLong();
        }

        return pArray;
    }

    /**
     * Reads an enum of the given type T using the ordinal encoded as a VarInt from the buffer.
     *
     * @see #writeEnum
     */
    public <T extends Enum<T>> T readEnum(Class<T> pEnumClass) {
        return (pEnumClass.getEnumConstants())[this.readVarInt()];
    }

    /**
     * Writes an enum of the given type T using the ordinal encoded as a VarInt to the buffer.
     *
     * @see #readEnum
     */
    public ByteBuffer writeEnum(Enum<?> pValue) {
        return this.writeVarInt(pValue.ordinal());
    }

    /**
     * Reads a compressed int from the buffer. To do so it maximally reads 5 byte-sized chunks whose most significant bit
     * dictates whether another byte should be read.
     *
     * @see #writeVarInt
     */
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
        } while((b0 & 128) == 128);

        return i;
    }

    /**
     * Reads a compressed long from the buffer. To do so it maximally reads 10 byte-sized chunks whose most significant
     * bit dictates whether another byte should be read.
     *
     * @see #writeVarLong
     */
    public long readVarLong() {
        long i = 0L;
        int j = 0;

        byte b0;
        do {
            b0 = this.readByte();
            i |= (long)(b0 & 127) << j++ * 7;
            if (j > 10) {
                throw new RuntimeException("VarLong too big");
            }
        } while((b0 & 128) == 128);

        return i;
    }

    /**
     * Writes a compressed int to the buffer. The smallest number of bytes to fit the passed int will be written. Of each
     * such byte only 7 bits will be used to describe the actual value since its most significant bit dictates whether
     * the next byte is part of that same int. Micro-optimization for int values that are usually small.
     */
    public ByteBuffer writeVarInt(int p_130131_) {
        while((p_130131_ & -128) != 0) {
            this.writeByte(p_130131_ & 127 | 128);
            p_130131_ >>>= 7;
        }

        this.writeByte(p_130131_);
        return this;
    }

    /**
     * Writes a compressed long to the buffer. The smallest number of bytes to fit the passed long will be written. Of
     * each such byte only 7 bits will be used to describe the actual value since its most significant bit dictates
     * whether the next byte is part of that same long. Micro-optimization for long values that are usually small.
     */
    public ByteBuffer writeVarLong(long pValue) {
        while((pValue & -128L) != 0L) {
            this.writeByte((int)(pValue & 127L) | 128);
            pValue >>>= 7;
        }

        this.writeByte((int)pValue);
        return this;
    }

    /**
     * Reads a String with a maximum length of {@code Short.MAX_VALUE}.
     *
     * @see #readString(int)
     * @see #writeString
     */
    public String readString() {
        return this.readString(Short.MAX_VALUE);
    }

    /**
     * Reads a string with a maximum length from this buffer.
     *
     * @see #writeString
     */
    public String readString(int pMaxLength) {
        int i = this.readVarInt();
        if (i > pMaxLength * 4) {
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + i + " > " + pMaxLength * 4 + ")");
        } else if (i < 0) {
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        } else {
            String s = this.toString(this.readerIndex(), i, StandardCharsets.UTF_8);
            this.readerIndex(this.readerIndex() + i);
            if (s.length() > pMaxLength) {
                throw new DecoderException("The received string length is longer than maximum allowed (" + i + " > " + pMaxLength + ")");
            } else {
                return s;
            }
        }
    }

    /**
     * Writes a String with a maximum length of {@code Short.MAX_VALUE}.
     *
     * @see #readString
     */
    public ByteBuffer writeString(String pString) {
        return this.writeString(pString, Short.MAX_VALUE);
    }

    /**
     * Writes a String with a maximum length.
     *
     * @see #readString
     */
    public ByteBuffer writeString(String pString, int pMaxLength) {
        byte[] abyte = pString.getBytes(StandardCharsets.UTF_8);
        if (abyte.length > pMaxLength) {
            throw new EncoderException("String too big (was " + abyte.length + " bytes encoded, max " + pMaxLength + ")");
        } else {
            this.writeVarInt(abyte.length);
            this.writeBytes(abyte);
            return this;
        }
    }

    /**
     * Reads a {@link PacketID} from the buffer.
     */
    public PacketID readPacketID() {
        return PacketID.DECODER.decode(this);
    }

    /**
     * Reads a value from the buffer, using the specified {@code decoder}.
     *
     * @param decoder the decoder to use
     * @param <T>     the type of the object to read
     */
    public <T> T read(BufferDecoder<T> decoder) {
        return decoder.decode(this);
    }

    /**
     * Reads a value from the buffer, using the decoder associated
     * with the specified class from {@link com.mcmoddev.mmdbot.dashboard.common.BufferSerializers#DECODERS}.
     *
     * @param clazz the class of the object to read
     * @param <T>   the type of the object to read
     */
    public <T> T read(Class<T> clazz) {
        final var decoder = BufferSerializers.getDecoder(clazz);
        if (decoder != null) {
            return decoder.decode(this);
        }
        throw new RuntimeException("I do not know how to decode the " + clazz);
    }

    public void write(BufferSerializable serializable) {
        serializable.encode(this);
    }

    @Override
    public int hashCode() {
        return source.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof ByteBuffer bb && bb.source.equals(this.source);
    }

    @Override
    public String toString() {
        return null;
    }
}
