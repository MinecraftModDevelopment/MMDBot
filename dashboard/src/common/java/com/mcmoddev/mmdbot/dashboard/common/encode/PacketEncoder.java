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
package com.mcmoddev.mmdbot.dashboard.common.encode;

import com.mcmoddev.mmdbot.dashboard.common.ByteBuffer;
import com.mcmoddev.mmdbot.dashboard.common.Packet;
import com.mcmoddev.mmdbot.dashboard.common.PacketRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.IOException;

public final class PacketEncoder extends MessageToByteEncoder<Packet> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PacketDecoder.class);
    private static final Marker MARKER = MarkerFactory.getMarker("PACKET_SENT");

    private final PacketRegistry.PacketSet packetSet;

    public PacketEncoder(final PacketRegistry.PacketSet packetSet) {
        this.packetSet = packetSet;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) throws Exception {
        Integer integer = packetSet.getId(packet.getClass());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MARKER, "OUT: [{}] {}", integer, packet.getClass().getName());
        }

        if (integer == null) {
            throw new IOException("Can't serialize unregistered packet");
        } else {
            ByteBuffer byteBuf = new ByteBuffer(out);
            byteBuf.writeVarInt(integer);

            try {
                int i = byteBuf.writerIndex();
                packet.encode(byteBuf);
                int j = byteBuf.writerIndex() - i;
                if (j > 8388608) {
                    throw new IllegalArgumentException("Packet too big (is " + j + ", should be less than 8388608): " + packet);
                }
            } catch (Throwable throwable) {
                LOGGER.error("Error encoding packet", throwable);
            }
        }
    }
}
