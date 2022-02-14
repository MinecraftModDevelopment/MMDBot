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
import com.mcmoddev.mmdbot.dashboard.common.listener.MultiPacketListener;
import com.mcmoddev.mmdbot.dashboard.common.listener.PacketListener;
import com.mcmoddev.mmdbot.dashboard.common.listener.PacketWaiter;
import com.mcmoddev.mmdbot.dashboard.common.packet.Packet;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketRegistry;
import com.mcmoddev.mmdbot.dashboard.common.util.LazyLoadedValue;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
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

    private static ServerInitializer packetHandler;

    public static void setup(InetSocketAddress address, PacketListener... extraListeners) {
        final List<PacketListener> listeners = Lists.newArrayList(extraListeners);
        listeners.add(PACKET_WAITER);

        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        packetHandler = new ServerInitializer(PacketRegistry.SET, DashboardSever::sendPacketToAll,
            new MultiPacketListener(listeners));
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .handler(new LoggingHandler(LogLevel.INFO))
            .childHandler(packetHandler)
            .childOption(ChannelOption.SO_KEEPALIVE, true);

        // Start the server.
        ChannelFuture f = b.bind(address.getAddress(), address.getPort()).syncUninterruptibly();

        f.channel().closeFuture().syncUninterruptibly();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }, "DashboardServerCloser"));
    }

    public static void setup1(InetSocketAddress address, PacketListener... extraListeners) {
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
        packetHandler = new ServerInitializer(PacketRegistry.SET, DashboardSever::sendPacketToAll, new MultiPacketListener(listeners));
        final var boostrap = new ServerBootstrap()
            .group(eventGroup.get())
            .channel(channelClz)
            .handler(new LoggingHandler(LogLevel.WARN))
            .childHandler(packetHandler)
            .childOption(ChannelOption.SO_KEEPALIVE, true);
        // Start the server.
        ChannelFuture f = boostrap.bind(address.getAddress(), address.getPort()).syncUninterruptibly();

        // Wait until the server socket is closed.
        f.channel().closeFuture().syncUninterruptibly();
        log.warn("Dashboard endpoint has been created at {}:{}", address.getAddress().getHostAddress(), address.getPort());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> eventGroup.get().shutdownGracefully(), "DashboardServerCloser"));
    }

    public static void sendPacketToAll(Packet packet) {
        packetHandler.sendPacketToAll(packet);
    }

}
