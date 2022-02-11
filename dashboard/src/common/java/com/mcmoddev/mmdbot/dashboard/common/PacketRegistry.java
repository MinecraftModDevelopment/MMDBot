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

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public final class PacketRegistry {

    public static final PacketSet SET = new PacketSet()
        .addPacket(TestPacket.class, buf -> new TestPacket(buf.readInt()));

    public static class PacketSet {
        static final Logger LOG = LoggerFactory.getLogger(PacketSet.class);

        final Object2IntMap<Class<? extends Packet>> classToId = make(new Object2IntOpenHashMap<>(), map -> map.defaultReturnValue(-1));
        private final List<Function<ByteBuffer, ? extends Packet>> idToDeserializer = Lists.newArrayList();

        public <P extends Packet> PacketSet addPacket(Class<P> pktClass, Function<ByteBuffer, P> deserializer) {
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

        @Nullable
        public Integer getId(Class<?> pktClazz) {
            int i = this.classToId.getInt(pktClazz);
            return i == -1 ? null : i;
        }

        @Nullable
        public Packet createPacket(int pktId, ByteBuffer buffer) {
            Function<ByteBuffer, ? extends Packet> function = this.idToDeserializer.get(pktId);
            return function != null ? function.apply(buffer) : null;
        }

        public Iterable<Class<? extends Packet>> getAllPackets() {
            return Iterables.unmodifiableIterable(this.classToId.keySet());
        }
    }

    private static <T> T make(T obj, Consumer<T> consumer) {
        consumer.accept(obj);
        return obj;
    }

    public record TestPacket(int something) implements Packet {

        @Override
        public void encode(final ByteBuffer buffer) {
            buffer.writeInt(something);
        }

        @Override
        public void handle(final PacketReceiver sender) {
            System.out.println("Packet received with something = " + something);
        }
    }

}
