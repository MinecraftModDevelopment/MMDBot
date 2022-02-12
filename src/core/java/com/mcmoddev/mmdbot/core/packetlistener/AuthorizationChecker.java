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
package com.mcmoddev.mmdbot.core.packetlistener;

import com.mcmoddev.mmdbot.core.RunBots;
import com.mcmoddev.mmdbot.dashboard.common.listener.PacketListener;
import com.mcmoddev.mmdbot.dashboard.common.packet.Packet;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketReceiver;
import com.mcmoddev.mmdbot.dashboard.common.packet.impl.CheckAuthorizedPacket;

import java.util.Objects;
import java.util.stream.Stream;

public record AuthorizationChecker(RunBots.DashboardConfig.Account[] accounts) implements PacketListener {

    @Override
    public void onPacket(final Packet packet, final PacketReceiver receiver) {
        if (!(packet instanceof CheckAuthorizedPacket authPacket)) {
            return;
        }

        if (Stream.of(accounts).anyMatch(acc -> Objects.equals(acc.username, authPacket.getUsername())
            && Objects.equals(acc.password, authPacket.getPassword()))) {
            receiver.reply(new CheckAuthorizedPacket.Response(CheckAuthorizedPacket.ResponseType.AUTHORIZED));
        } else {
            receiver.reply(new CheckAuthorizedPacket.Response(CheckAuthorizedPacket.ResponseType.DENIED));
        }
    }
}
