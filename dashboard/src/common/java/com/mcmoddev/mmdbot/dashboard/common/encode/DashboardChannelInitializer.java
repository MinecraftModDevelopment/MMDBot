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
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketReceiver;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketRegistry;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;

public class DashboardChannelInitializer extends ChannelInitializer<Channel> {

    private final PacketRegistry.PacketSet packetSet;
    private final SimpleChannelInboundHandler<Packet> handler;

    public DashboardChannelInitializer(final PacketRegistry.PacketSet packetSet,  final SimpleChannelInboundHandler<Packet> handler) {
        this.packetSet = packetSet;
        this.handler = handler;
    }

    public DashboardChannelInitializer(final PacketRegistry.PacketSet packetSet, final PacketReceiver receiver, final PacketListener listener) {
        this(packetSet, new PacketHandler(receiver, listener));
    }

    @Override
    protected void initChannel(final Channel ch) throws Exception {
        ch.config().setOption(ChannelOption.TCP_NODELAY, true);
        final var pipeline = ch.pipeline();
        pipeline //.addLast("timeout", new ReadTimeoutHandler(30))
            .addLast("splitter", new FrameDecoder())
            .addLast("decoder", new PacketDecoder(packetSet))
            .addLast("prepender", new PacketPrepender())
            .addLast("encoder", new PacketEncoder(packetSet))
            .addLast("packet_handler", handler);
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        System.out.println("Channel active");
    }
}
