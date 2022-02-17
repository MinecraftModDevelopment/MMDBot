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

import com.mcmoddev.mmdbot.dashboard.BotTypeEnum;
import com.mcmoddev.mmdbot.dashboard.ServerBridge;
import com.mcmoddev.mmdbot.dashboard.common.packet.HasResponse;
import com.mcmoddev.mmdbot.dashboard.common.packet.Packet;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketContext;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketInputBuffer;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketOutputBuffer;

import java.util.List;

public final class RequestLoadedBotTypesPacket implements Packet, HasResponse<RequestLoadedBotTypesPacket.Response> {

    public RequestLoadedBotTypesPacket() {}
    public RequestLoadedBotTypesPacket(PacketInputBuffer buffer) {}

    @Override
    public void encode(final PacketOutputBuffer buffer) {
        // Nothing to encode
    }

    @Override
    public void handle(final PacketContext context) {
        ServerBridge.executeOnInstance(bridge -> context.reply(new Response(bridge.getLoadedBotTypes())));
    }

    @Override
    public Class<Response> getResponsePacketClass() {
        return Response.class;
    }

    public static final class Response implements Packet {

        private final List<BotTypeEnum> types;

        public Response(final List<BotTypeEnum> types) {
            this.types = types;
        }

        public Response(final PacketInputBuffer buffer) {
            this.types = buffer.readList(b -> b.readEnum(BotTypeEnum.class));
        }

        @Override
        public void handle(final PacketContext context) {
            // Nothing to handle
        }

        @Override
        public void encode(final PacketOutputBuffer buffer) {
            buffer.writeList(types, (e, b) -> b.writeEnum(e));
        }

        public List<BotTypeEnum> getTypes() {
            return types;
        }
    }

}
