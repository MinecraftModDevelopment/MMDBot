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
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mcmoddev.mmdbot.dashboard.common.Connection;
import com.mcmoddev.mmdbot.dashboard.common.encode.DashboardChannelInitializer;
import com.mcmoddev.mmdbot.dashboard.common.listener.MultiPacketListener;
import com.mcmoddev.mmdbot.dashboard.common.listener.PacketListener;
import com.mcmoddev.mmdbot.dashboard.common.listener.PacketWaiter;
import com.mcmoddev.mmdbot.dashboard.common.packet.Packet;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketRegistry;
import com.mcmoddev.mmdbot.dashboard.common.util.LazyLoadedValue;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public final class DashboardSever {

    public static final PacketWaiter PACKET_WAITER = new PacketWaiter();

    private static final LazyLoadedValue<NioEventLoopGroup> SERVER_EVENT_GROUP = new LazyLoadedValue<>(() -> new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Server IO #%d").setDaemon(true).build()));
    private static final LazyLoadedValue<EpollEventLoopGroup> SERVER_EPOLL_EVENT_GROUP = new LazyLoadedValue<>(() -> new EpollEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Epoll Server IO #%d").setDaemon(true).build()));

    private static Connection connection;

    public static void setup(InetSocketAddress address, PacketListener... extraListeners) {
        final List<PacketListener> listeners = Lists.newArrayList(extraListeners);
        listeners.add(PACKET_WAITER);
        Class<? extends ServerSocketChannel> channelClz;
        LazyLoadedValue<? extends EventLoopGroup> eventGroup;
        if (Epoll.isAvailable()) {
            channelClz = EpollServerSocketChannel.class;
            eventGroup = SERVER_EPOLL_EVENT_GROUP;
            log.info("Using epoll channel type");
        } else {
            channelClz = NioServerSocketChannel.class;
            eventGroup = SERVER_EVENT_GROUP;
            log.info("Using default channel type");
        }
        final var boostrap = new ServerBootstrap()
            .group(eventGroup.get())
            .channel(channelClz)
            .handler(new LoggingHandler(LogLevel.WARN))
            .childHandler(new DashboardChannelInitializer(PacketRegistry.SET, DashboardSever::sendPacket,
                new MultiPacketListener(listeners)))
            .localAddress(address.getAddress(), address.getPort())
            .option(ChannelOption.SO_BACKLOG, 128)
            .childOption(ChannelOption.SO_KEEPALIVE, true);
        connection = Connection.fromServer(boostrap);
        log.warn("Dashboard endpoint has been created at {}:{}", address.getAddress().getHostAddress(), address.getPort());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> eventGroup.get().shutdownGracefully(), "DashboardServerCloser"));
    }

    public static void sendPacket(Packet packet) {
        connection.sendPacket(packet);
    }

}
