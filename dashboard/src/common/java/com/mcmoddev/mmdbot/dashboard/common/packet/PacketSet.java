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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mcmoddev.mmdbot.dashboard.common.BufferDecoder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.function.Consumer;

public class PacketSet {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    static final Logger LOG = LoggerFactory.getLogger(PacketSet.class);

    final Object2IntMap<Class<? extends Packet>> classToId = make(new Object2IntOpenHashMap<>(), map -> map.defaultReturnValue(-1));
    private final List<BufferDecoder<? extends Packet>> idToDeserializer = Lists.newArrayList();

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
     * The packet class <b>HAS TO HAVE</b> a constructor with a {@link PacketInputBuffer}
     * as the parameter. <br>
     * <strong>Due to this method using reflection to find the constructor,
     * it may <i>slightly</i> reduce performance.</strong>
     *
     * @param packetClass the packet class
     * @param <P>         the type of the packet
     * @return the current packet set
     */
    @SuppressWarnings("unchecked")
    public <P extends Packet> PacketSet addPacket(Class<P> packetClass) {
        final var methodType = MethodType.methodType(void.class, PacketInputBuffer.class);
        try {
            final var handle = LOOKUP.findConstructor(packetClass, methodType);
            return addPacket(packetClass, buffer -> {
                try {
                    return (P) handle.invokeWithArguments(buffer);
                } catch (Throwable e) {
                    LOG.error("Exception while trying to construct packet {}!", packetClass, e);
                    throw new RuntimeException(e);
                }
            });
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException("The packet class " + packetClass + " does not have a constructor with a PacketInputBuffer as a parameter. Did you mean to use #addPacket(Class, Function)?");
        }
    }

    @Nullable
    public Integer getId(Class<?> pktClazz) {
        int i = this.classToId.getInt(pktClazz);
        return i == -1 ? null : i;
    }

    @Nullable
    public Packet createPacket(int pktId, PacketInputBuffer buffer) {
        final var function = this.idToDeserializer.get(pktId);
        return function != null ? function.decode(buffer) : null;
    }

    public void applyToKryo(Kryo kryo) {
        classToId.keySet().stream()
            .map(clz -> new SimplePair<>(clz, getId(clz)))
            .forEach(pair -> {
                final var id = pair.second();
                kryo.register(pair.first(), new Serializer<Packet>() {
                    @Override
                    public void write(final Kryo kryo, final Output output, final Packet object) {
                        object.encode(PacketOutputBuffer.fromOutput(output));
                    }

                    @Override
                    public Packet read(final Kryo kryo, final Input input, final Class type) {
                        return createPacket(id, PacketInputBuffer.fromInput(input));
                    }
                    // register from ID 100
                }, id + 100);
            });
    }

    public Iterable<Class<? extends Packet>> getAllPackets() {
        return Iterables.unmodifiableIterable(this.classToId.keySet());
    }

    private record SimplePair<F, S>(F first, S second) {
    }

    private static <T> T make(T obj, Consumer<T> consumer) {
        consumer.accept(obj);
        return obj;
    }
}
