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

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import com.google.common.collect.Lists;
import com.mcmoddev.mmdbot.dashboard.common.listener.MultiPacketListener;
import com.mcmoddev.mmdbot.dashboard.common.listener.PacketListener;
import com.mcmoddev.mmdbot.dashboard.common.listener.PacketWaiter;
import com.mcmoddev.mmdbot.dashboard.common.packet.Packet;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketHandler;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketRegistry;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
@UtilityClass
public final class DashboardSever {

    public static final PacketWaiter PACKET_WAITER = new PacketWaiter();
    private static Server server;

    public static void setup(InetSocketAddress address, PacketListener... extraListeners) {
        try {
            final List<PacketListener> listeners = Lists.newArrayList(extraListeners);
            listeners.add(PACKET_WAITER);

            server = new Server();
            server.bind(address, null);
            server.start();

            PacketRegistry.SET.applyToKryo(server.getKryo());

            server.addListener(new PacketHandler(new MultiPacketListener(listeners)));
            server.addListener(new Listener() {
                @Override
                public void connected(final Connection connection) {
                    log.warn("{} connected to the dashboard!", connection.getRemoteAddressTCP());
                }

                @Override
                public void disconnected(final Connection connection) {
                    log.warn("{} disconnected from the dashboard!", connection.getRemoteAddressTCP());
                }
            });

            Runtime.getRuntime().addShutdownHook(new Thread(server::close, "ServerDashboardCloser"));
            log.warn("Dashboard endpoint created at {}:{}!", address.getAddress(), address.getPort());
        } catch (Exception e) {
            log.error("Exception while trying to create dashboard endpoint!", e);
        }
    }

    public static void sendToAll(Packet packet) {
        server.sendToAllTCP(packet);
    }
}
