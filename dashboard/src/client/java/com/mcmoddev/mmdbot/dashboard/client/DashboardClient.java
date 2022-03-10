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
package com.mcmoddev.mmdbot.dashboard.client;

import com.google.common.collect.Lists;
import com.mcmoddev.mmdbot.dashboard.BotTypeEnum;
import com.mcmoddev.mmdbot.dashboard.client.util.NullableReference;
import com.mcmoddev.mmdbot.dashboard.common.encode.ChannelInitializer;
import com.mcmoddev.mmdbot.dashboard.common.listener.MultiPacketListener;
import com.mcmoddev.mmdbot.dashboard.common.listener.PacketListener;
import com.mcmoddev.mmdbot.dashboard.common.listener.PacketWaiter;
import com.mcmoddev.mmdbot.dashboard.common.packet.HasIDPacket;
import com.mcmoddev.mmdbot.dashboard.common.packet.HasResponse;
import com.mcmoddev.mmdbot.dashboard.common.packet.Packet;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketID;
import com.mcmoddev.mmdbot.dashboard.packets.GenericResponsePacket;
import com.mcmoddev.mmdbot.dashboard.packets.Packets;
import com.mcmoddev.mmdbot.dashboard.util.Credentials;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Slf4j
@UtilityClass
public class DashboardClient {

    public static final PacketWaiter PACKET_WAITER = new PacketWaiter();
    public static List<BotTypeEnum> botTypes = new ArrayList<>();
    private static final NullableReference<Channel> CHANNEL = new NullableReference<>(false);
    private static EventLoopGroup clientGroup;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(DashboardClient::shutdown, "DashboardClientCloser"));
    }

    /**
     * The last credentials used to log in
     */
    public static Credentials credentials;

    public static void setup(InetSocketAddress address, PacketListener... extraListeners) {
        try {
            final List<PacketListener> listeners = Lists.newArrayList(extraListeners);
            listeners.add(PACKET_WAITER);
            clientGroup = new NioEventLoopGroup();
            final var boostrap = new Bootstrap()
                .group(clientGroup)
                .channel(NioSocketChannel.class)
                .remoteAddress(address.getAddress(), address.getPort())
                .handler(new ChannelInitializer(Packets.SET, new MultiPacketListener(listeners)));
            CHANNEL.set(boostrap.connect().syncUninterruptibly().channel());

            log.warn("Dashboard connection has been established with {}:{}", address.getAddress().getHostAddress(), address.getPort());
        } catch (Exception e) {
            log.error("Error while trying to connect to the dashboard!", e);
        }
    }

    public static void sendPacket(Packet packet) {
        CHANNEL.invokeIfNotNull(c -> c.writeAndFlush(packet));
    }

    public static <R extends Packet, P extends HasResponse<R> & Packet> PacketProcessorBuilder<R> sendAndAwaitResponseWithID(Function<PacketID, P> packetCreator) {
        final var packetID = PacketID.generateRandom();
        final var pkt = packetCreator.apply(packetID);
        return new PacketProcessorBuilder<>(pkt, pkt.getResponsePacketClass())
            .withPredicate(p -> {
                if (p instanceof HasIDPacket withId) {
                    return packetID.equals(withId.getPacketID());
                }
                return true;
            });
    }

    public static <R extends Packet, P extends HasResponse<R> & Packet> PacketProcessorBuilder<R> sendAndAwaitResponse(P packet) {
        return new PacketProcessorBuilder<>(packet, packet.getResponsePacketClass());
    }

    public static <P extends Packet> PacketProcessorBuilder<GenericResponsePacket> sendAndAwaitGenericResponse(Function<PacketID, P> packet) {
        final var packetID = PacketID.generateRandom();
        return new PacketProcessorBuilder<>(packet.apply(packetID), GenericResponsePacket.class)
            .withPredicate(p -> p.originalPacketID().equals(packetID));
    }

    public static void shutdown() {
        if (clientGroup != null) {
            clientGroup.shutdownGracefully();
            clientGroup = null;
        }
    }

}
