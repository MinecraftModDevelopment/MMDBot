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

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.google.common.collect.Lists;
import com.mcmoddev.mmdbot.client.util.NullableReference;
import com.mcmoddev.mmdbot.dashboard.BotTypeEnum;
import com.mcmoddev.mmdbot.dashboard.common.listener.MultiPacketListener;
import com.mcmoddev.mmdbot.dashboard.common.listener.PacketListener;
import com.mcmoddev.mmdbot.dashboard.common.listener.PacketWaiter;
import com.mcmoddev.mmdbot.dashboard.common.packet.HasResponse;
import com.mcmoddev.mmdbot.dashboard.common.packet.Packet;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketHandler;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketID;
import com.mcmoddev.mmdbot.dashboard.packets.GenericResponsePacket;
import com.mcmoddev.mmdbot.dashboard.packets.Packets;
import com.mcmoddev.mmdbot.dashboard.util.Credentials;
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
    private static final NullableReference<Client> CLIENT = new NullableReference<>(false);

    /**
     * The last credentials used to log in
     */
    public static Credentials credentials;

    public static void setup(InetSocketAddress address, PacketListener... extraListeners) {
        try {
            final List<PacketListener> listeners = Lists.newArrayList(extraListeners);
            listeners.add(PACKET_WAITER);

            final var client = new Client();
            client.start();
            client.connect(5000, address.getAddress().getHostAddress(), address.getPort());

            Packets.SET.applyToKryo(client.getKryo());

            client.addListener(new PacketHandler(new MultiPacketListener(listeners)));
            client.addListener(new Listener() {
                @Override
                public void connected(final Connection connection) {
                    log.warn("{} connected!", connection.getRemoteAddressTCP());
                }

                @Override
                public void disconnected(final Connection connection) {
                    log.warn("{} disconnected!", connection.getRemoteAddressTCP());
                }
            });

            CLIENT.set(client);

            Runtime.getRuntime().addShutdownHook(new Thread(DashboardClient::shutdown, "DashboardClientCloser"));
            log.warn("Dashboard connection has been established with {}:{}", address.getAddress().getHostAddress(), address.getPort());
        } catch (Exception e) {
            log.error("Error while trying to connect to the dashboard!", e);
        }
    }

    public static void sendPacket(Packet packet) {
        CLIENT.invokeIfNotNull(c -> c.sendTCP(packet));
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
        CLIENT.invokeIfNotNull(Client::stop);
    }

}
