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

import com.mcmoddev.mmdbot.dashboard.common.listener.PacketListener;
import com.mcmoddev.mmdbot.dashboard.common.packet.Packet;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketSet;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;

public class ChannelInitializer extends io.netty.channel.ChannelInitializer<SocketChannel> {

    protected final PacketSet packetSet;
    protected final SimpleChannelInboundHandler<Packet> handler;

    public ChannelInitializer(final PacketSet packetSet, final SimpleChannelInboundHandler<Packet> handler) {
        this.packetSet = packetSet;
        this.handler = handler;
    }

    public ChannelInitializer(final PacketSet packetSet, final PacketListener listener) {
        this(packetSet, new PacketHandler(listener));
    }

    @Override
    protected void initChannel(final SocketChannel ch) throws Exception {
        ch.config().setOption(ChannelOption.TCP_NODELAY, true);
        final var pipeline = ch.pipeline();
        pipeline
            .addLast("encoder", new PacketEncoder(packetSet))
            .addLast("decoder", new PacketDecoder(packetSet))
            .addLast("packet_handler", handler);
    }
}
