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
package com.mcmoddev.mmdbot.dashboard.common.packet;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.mcmoddev.mmdbot.dashboard.common.listener.PacketListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
public final class PacketHandler implements Listener {

    private final PacketListener listener;
    private final boolean logReceive;
    private final Executor threadpool = Executors.newSingleThreadExecutor(r -> {
        final var thread = new Thread(r, "PacketHandler");
        thread.setDaemon(true);
        return thread;
    });

    public PacketHandler(final PacketListener listener, final boolean logReceive) {
        this.listener = listener;
        this.logReceive = logReceive;
    }

    public PacketHandler(final PacketListener listener) {
        this(listener, true);
    }

    @Override
    public void received(final Connection connection, final Object o) {
        threadpool.execute(() -> {
            if (o instanceof Packet packet) {
                if (logReceive && log.isDebugEnabled()) {
                    log.debug("Received packet {} from {}", packet, connection.getRemoteAddressTCP());
                }
                final var ctx = PacketContext.fromConnection(connection);
                listener.onPacketAndThen(packet, ctx, () -> packet.handle(ctx));
            }
        });
    }
}
