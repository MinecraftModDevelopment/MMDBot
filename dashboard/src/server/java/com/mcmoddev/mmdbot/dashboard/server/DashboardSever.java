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
package com.mcmoddev.mmdbot.dashboard.server;

import com.google.common.collect.Lists;
import com.mcmoddev.mmdbot.dashboard.common.Connection;
import com.mcmoddev.mmdbot.dashboard.common.encode.DashboardChannelInitializer;
import com.mcmoddev.mmdbot.dashboard.common.listener.MultiPacketListener;
import com.mcmoddev.mmdbot.dashboard.common.listener.PacketListener;
import com.mcmoddev.mmdbot.dashboard.common.listener.PacketWaiter;
import com.mcmoddev.mmdbot.dashboard.common.packet.Packet;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

public final class DashboardSever {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardSever.class);
    public static final PacketWaiter PACKET_WAITER = new PacketWaiter();

    private static Connection connection;

    public static void setup(InetSocketAddress address, PacketListener... extraListeners) {
        final List<PacketListener> listeners = Lists.newArrayList(extraListeners);
        listeners.add(PACKET_WAITER);
        final var group = new NioEventLoopGroup(1);
        final var boostrap = new ServerBootstrap()
            .group(group)
            .channel(NioServerSocketChannel.class)
            .handler(new LoggingHandler(LogLevel.INFO))
            .childHandler(new DashboardChannelInitializer(PacketRegistry.SET, packet -> connection.sendPacket(packet),
                new MultiPacketListener(listeners)))
            .localAddress(address.getAddress(), address.getPort());
        connection = new Connection(boostrap);
        LOG.warn("Dashboard endpoint has been created at {}:{}", address.getAddress().getHostAddress(), address.getPort());
        Runtime.getRuntime().addShutdownHook(new Thread(group::shutdownGracefully, "DashboardServerCloser"));
    }

    public static void sendPacket(Packet packet) {
        connection.sendPacket(packet);
    }

}
