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
package com.mcmoddev.mmdbot.dashboard.packets;

import com.mcmoddev.mmdbot.dashboard.common.packet.PacketSet;
import com.mcmoddev.mmdbot.dashboard.packets.requests.RequestBotUserDataPacket;
import com.mcmoddev.mmdbot.dashboard.packets.requests.RequestConfigValuePacket;
import com.mcmoddev.mmdbot.dashboard.packets.requests.RequestLoadedBotTypesPacket;

public class Packets {

    public static final PacketSet SET = new PacketSet()
        .addPacket(GenericResponsePacket.class)
        .addPacket(CheckAuthorizedPacket.class)
            .addPacket(CheckAuthorizedPacket.Response.class)
        .addPacket(RequestLoadedBotTypesPacket.class)
            .addPacket(RequestLoadedBotTypesPacket.Response.class)
        .addPacket(ShutdownBotPacket.class)
        .addPacket(RequestBotUserDataPacket.class)
            .addPacket(RequestBotUserDataPacket.Response.class)

        // Config stuff
        .addPacket(UpdateConfigPacket.class, UpdateConfigPacket::decode)
        .addPacket(RequestConfigValuePacket.class, RequestConfigValuePacket::decode)
            .addPacket(RequestConfigValuePacket.Response.class, RequestConfigValuePacket.Response::decode)

        // Finish off by making it immutable, just in case
        .immutable();
}
