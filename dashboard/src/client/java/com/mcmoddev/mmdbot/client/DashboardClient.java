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
package com.mcmoddev.mmdbot.client;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mcmoddev.mmdbot.dashboard.common.Connection;
import com.mcmoddev.mmdbot.dashboard.common.encode.DashboardChannelInitializer;
import com.mcmoddev.mmdbot.dashboard.common.listener.MultiPacketListener;
import com.mcmoddev.mmdbot.dashboard.common.listener.PacketListener;
import com.mcmoddev.mmdbot.dashboard.common.listener.PacketWaiter;
import com.mcmoddev.mmdbot.dashboard.common.packet.Packet;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketRegistry;
import com.mcmoddev.mmdbot.dashboard.common.util.LazyLoadedValue;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

public class DashboardClient {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardClient.class);
    public static final PacketWaiter PACKET_WAITER = new PacketWaiter();
    private static Connection connection;
    private static Runnable runWhenStopped = () -> {};

    public static final LazyLoadedValue<NioEventLoopGroup> NETWORK_WORKER_GROUP = new LazyLoadedValue<>(() -> new NioEventLoopGroup(0,(new ThreadFactoryBuilder().setNameFormat("Netty Client IO #%d").setDaemon(true).build())));
    public static final LazyLoadedValue<EpollEventLoopGroup> NETWORK_EPOLL_WORKER_GROUP = new LazyLoadedValue<>(() -> new EpollEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Epoll Client IO #%d").setDaemon(true).build()));

    public static void setup(InetSocketAddress address, PacketListener... extraListeners) {
        final List<PacketListener> listeners = Lists.newArrayList(extraListeners);
        listeners.add(PACKET_WAITER);
        Class<? extends SocketChannel> channelClz;
        LazyLoadedValue<? extends EventLoopGroup> eventGroup;
        if (Epoll.isAvailable()) {
            channelClz = EpollSocketChannel.class;
            eventGroup = NETWORK_EPOLL_WORKER_GROUP;
            LOG.info("Using epoll channel type");
        } else {
            channelClz = NioSocketChannel.class;
            eventGroup = NETWORK_WORKER_GROUP;
            LOG.info("Using default channel type");
        }
        final var boostrap = new Bootstrap()
            .group(eventGroup.get())
            .channel(channelClz)
            .handler(new DashboardChannelInitializer(PacketRegistry.SET, packet -> connection.sendPacket(packet),
                new MultiPacketListener(listeners)));
        connection = Connection.fromClient(boostrap, address);
        LOG.warn("Dashboard connection has been established with {}:{}", address.getAddress().getHostAddress(), address.getPort());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> eventGroup.get().shutdownGracefully(), "DashboardClientCloser"));
        runWhenStopped = eventGroup.get()::shutdownGracefully;
    }

    public static void sendPacket(Packet packet) {
        connection.sendPacket(packet);
    }

    public static void shutdown() {
        runWhenStopped.run();
    }
}
