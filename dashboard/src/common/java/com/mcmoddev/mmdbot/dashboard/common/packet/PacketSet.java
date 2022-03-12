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
import com.mcmoddev.mmdbot.dashboard.common.ByteBuffer;
import io.github.matyrobbrt.asmutils.wrapper.ConstructorWrapper;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandles;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class PacketSet implements Iterable<Map.Entry<Class<? extends Packet>, Integer>> {
    static final Logger LOG = LoggerFactory.getLogger(PacketSet.class);

    final Object2IntMap<Class<? extends Packet>> classToId;
    private final List<BufferDecoder<? extends Packet>> idToDeserializer;

    public PacketSet() {
        this(make(new Object2IntOpenHashMap<>(), map -> map.defaultReturnValue(-1)), new ArrayList<>());
    }

    PacketSet(final Object2IntMap<Class<? extends Packet>> classToId, final List<BufferDecoder<? extends Packet>> idToDeserializer) {
        this.classToId = classToId;
        this.idToDeserializer = idToDeserializer;
    }

    public <P extends Packet> PacketSet addPacket(Class<P> pktClass, BufferDecoder<P> deserializer) {
        int i = this.idToDeserializer.size();
        int j = this.classToId.put(pktClass, i);
        if (j != -1) {
            String s = "Packet " + pktClass + " is already registered to ID " + j;
            LOG.error(s);
            throw new IllegalArgumentException(s);
        } else {
            this.idToDeserializer.add(deserializer);
            return this;
        }
    }

    /**
     * Registers a packet of the specified {@code packetClass}. <br>
     * The packet class <b>HAS TO HAVE</b> a constructor with a {@link ByteBuffer}
     * as the parameter, or an empty constructor. <br>
     * <strong>Due to this method using reflection to find the constructor,
     * it may <i>slightly</i> reduce performance.</strong>
     *
     * @param packetClass the packet class
     * @param <P>         the type of the packet
     * @return the current packet set
     */
    public <P extends Packet> PacketSet addPacket(Class<P> packetClass) {
        final var hasPacketBufferConstructor = hasPacketBufferConstructor(packetClass);
        try {
            final var wrapper = ConstructorWrapper.wrap(packetClass, hasPacketBufferConstructor ? new Class<?>[]{ByteBuffer.class} : new Class<?>[]{});
            if (hasPacketBufferConstructor) {
                addPacket(packetClass, wrapper::invoke);
            } else {
                addPacket(packetClass, b -> wrapper.invoke());
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("The packet class " + packetClass + " does not have a constructor with a ByteBuffer as a parameter, or an empty constructor. Did you mean to use #addPacket(Class, Function)?");
        }
        return this;
    }

    private static boolean hasPacketBufferConstructor(Class<? extends Packet> clazz) {
        try {
            clazz.getDeclaredConstructor(ByteBuffer.class);
            return true;
        } catch (NoSuchMethodException ignored) {
            return false;
        }
    }

    @Nullable
    public Integer getId(Class<?> pktClazz) {
        int i = this.classToId.getInt(pktClazz);
        return i == -1 ? null : i;
    }

    @Nullable
    public Packet createPacket(int pktId, ByteBuffer buffer) {
        final var function = this.idToDeserializer.get(pktId);
        return function != null ? function.decode(buffer) : null;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Iterator<Map.Entry<Class<? extends Packet>, Integer>> iterator() {
        return classToId.keySet().stream().map(c -> new AbstractMap.SimpleImmutableEntry(c, getId(c)))
            .map(s -> (Map.Entry<Class<? extends Packet>, Integer>) s).iterator();
    }

    /**
     * Wraps this packet set into an immutable implementation.
     *
     * @return an immutable implementation of this set.
     */
    public Immutable immutable() {
        return new Immutable(this);
    }

    public static final class Immutable extends PacketSet {

        public Immutable(PacketSet other) {
            super(other.classToId, List.copyOf(other.idToDeserializer));
        }

        @Override
        public <P extends Packet> PacketSet addPacket(final Class<P> packetClass) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <P extends Packet> PacketSet addPacket(final Class<P> pktClass, final BufferDecoder<P> deserializer) {
            throw new UnsupportedOperationException();
        }
    }

    private static <T> T make(T obj, Consumer<T> consumer) {
        consumer.accept(obj);
        return obj;
    }
}
