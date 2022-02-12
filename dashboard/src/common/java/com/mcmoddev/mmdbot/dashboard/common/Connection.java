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

import com.mcmoddev.mmdbot.dashboard.common.packet.Packet;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class Connection {

    private final AbstractBootstrap<?, ?> bootstrap;
    private final Channel channel;

    private Connection(final AbstractBootstrap<?, ?> bootstrap, final Channel channel) {
        this.bootstrap = bootstrap;
        this.channel = channel;
    }

    public static Connection fromServer(ServerBootstrap bootstrap) {
        return new Connection(bootstrap, bootstrap.bind().syncUninterruptibly().channel());
    }

    public static Connection fromClient(Bootstrap bootstrap, InetSocketAddress address) {
        return new Connection(bootstrap, bootstrap.connect(address.getAddress(), address.getPort()).syncUninterruptibly().channel());
    }

    public void sendPacket(Packet packet) {
        try {
            channel.writeAndFlush(packet).get();
        } catch (Exception e) {
            log.error("Exception while trying to send packet!", e);
        }
    }

    public Channel getChannel() {
        return channel;
    }
}
