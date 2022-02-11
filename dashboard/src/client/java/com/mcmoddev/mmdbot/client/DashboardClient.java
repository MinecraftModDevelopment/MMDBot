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

import com.mcmoddev.mmdbot.dashboard.common.Packet;
import com.mcmoddev.mmdbot.dashboard.common.listener.PacketListener;
import com.mcmoddev.mmdbot.dashboard.common.PacketReceiver;
import com.mcmoddev.mmdbot.dashboard.common.PacketRegistry;
import com.mcmoddev.mmdbot.dashboard.common.encode.DashboardChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.Scanner;

public class DashboardClient {

    private static Channel channel;

    public static void setup(InetSocketAddress address, PacketListener listener) {
        final var group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new DashboardChannelInitializer(PacketRegistry.SET, new PacketReceiver() {
                    @Override
                    public void reply(final Packet packet) {

                    }
                }, listener));
            channel = bootstrap.connect(address.getAddress(), address.getPort()).sync().channel();
        } catch (Exception ignored) {

        }
    }

    public static void main(String[] args) {
        setup(new InetSocketAddress("localhost", 8912), packet -> {

        });
        channel.writeAndFlush(new PacketRegistry.TestPacket(120912));
        final var in = new Scanner(System.in);
        while (true) {
            channel.writeAndFlush(new PacketRegistry.TestPacket(in.nextInt()));
        }
    }

}
