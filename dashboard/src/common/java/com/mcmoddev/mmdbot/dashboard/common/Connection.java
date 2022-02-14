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

import com.mcmoddev.mmdbot.dashboard.common.encode.PacketEncoder;
import com.mcmoddev.mmdbot.dashboard.common.packet.Packet;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketRegistry;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetSocketAddress;

public class Connection {

    private final AbstractBootstrap<?, ?> bootstrap;
    private final Channel channel;

    private Connection(final AbstractBootstrap<?, ?> bootstrap, final Channel channel) {
        this.bootstrap = bootstrap;
        this.channel = channel;
    }

    public static Connection fromClient(Bootstrap bootstrap, InetSocketAddress address) {
        try {
            return new Connection(bootstrap, bootstrap.connect(address.getAddress(), address.getPort()).sync().channel());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sendPacket(Packet packet) {
        channel.writeAndFlush(packet);
    }

    public void sendPacket(Packet packet, GenericFutureListener<? extends Future<? super Void>> futureListener) {
        channel.writeAndFlush(packet).addListener(futureListener);
    }

    public Channel getChannel() {
        return channel;
    }
}
