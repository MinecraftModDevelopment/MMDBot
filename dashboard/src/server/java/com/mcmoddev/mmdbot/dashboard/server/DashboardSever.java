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
import com.mcmoddev.mmdbot.dashboard.ServerBridge;
import com.mcmoddev.mmdbot.dashboard.common.encode.ChannelInitializer;
import com.mcmoddev.mmdbot.dashboard.common.listener.MultiPacketListener;
import com.mcmoddev.mmdbot.dashboard.common.listener.PacketListener;
import com.mcmoddev.mmdbot.dashboard.common.listener.PacketWaiter;
import com.mcmoddev.mmdbot.dashboard.packets.CheckAuthorizedPacket;
import com.mcmoddev.mmdbot.dashboard.packets.Packets;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@UtilityClass
public final class DashboardSever {

    public static final PacketWaiter PACKET_WAITER = new PacketWaiter();
    public static final Map<InetSocketAddress, String> USERS = new HashMap<>();

    public static void setup(InetSocketAddress address, PacketListener... extraListeners) {
        try {
            final List<PacketListener> listeners = Lists.newArrayList(extraListeners);
            listeners.add(PACKET_WAITER);

            listeners.add((packet, context) -> {
                if (packet instanceof CheckAuthorizedPacket authPacket) {
                    ServerBridge.executeOnInstance(bridge -> {
                        if (bridge.checkAuthorized(authPacket.getCredentials()).isAuthorized()) {
                            USERS.put(context.getSenderAddress(), authPacket.getUsername());
                        }
                    });
                }
            });

            final var bossGroup = new NioEventLoopGroup();
            final var bootstrap = new ServerBootstrap()
                .group(bossGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer(Packets.SET, new MultiPacketListener(listeners)) {
                    @Override
                    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
                        log.warn("{} connected to the dashboard!", ctx.channel().remoteAddress());
                        super.channelActive(ctx);
                    }

                    @Override
                    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
                        log.warn("{} disconnected from the dashboard!", ctx.channel().remoteAddress());
                        super.channelInactive(ctx);
                    }
                })
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .localAddress(address.getAddress(), address.getPort());
            bootstrap.bind(address.getAddress(), address.getPort()).syncUninterruptibly();

            Runtime.getRuntime().addShutdownHook(new Thread(bossGroup::shutdownGracefully, "ServerDashboardCloser"));
            log.warn("Dashboard endpoint created at {}:{}!", address.getAddress(), address.getPort());
        } catch (Exception e) {
            log.error("Exception while trying to create dashboard endpoint!", e);
        }
    }
}
