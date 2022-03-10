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
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketSet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.IOException;
import java.util.List;

@Slf4j
public class PacketDecoder extends ByteToMessageDecoder {

    private static final Marker MARKER = MarkerFactory.getMarker("PACKET_RECEIVED");

    private final PacketSet packetSet;

    public PacketDecoder(final PacketSet packetSet) {
        this.packetSet = packetSet;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int i = in.readableBytes();
        if (i != 0) {
            ByteBuffer buffer = new ByteBuffer(in);
            int j = buffer.readVarInt();
            final var packet = packetSet.createPacket(j, buffer);
            if (packet == null) {
                throw new IOException("Bad packet id " + j);
            } else {
                if (buffer.readableBytes() > 0) {
                    throw new IOException("Packet " + j + " (" + packet.getClass().getSimpleName() + ") was larger than I expected, found " + buffer.readableBytes() + " bytes extra whilst reading packet " + j);
                } else {
                    out.add(packet);
                    if (log.isDebugEnabled()) {
                        log.debug(MARKER, " IN: [{}] {}", j, packet.getClass().getName());
                    }
                }
            }
        }
    }
}
