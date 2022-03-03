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
package com.mcmoddev.mmdbot.dashboard.packets.requests;

import com.mcmoddev.mmdbot.dashboard.BotTypeEnum;
import com.mcmoddev.mmdbot.dashboard.ServerBridge;
import com.mcmoddev.mmdbot.dashboard.common.packet.HasResponse;
import com.mcmoddev.mmdbot.dashboard.common.packet.Packet;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketContext;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketInputBuffer;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketOutputBuffer;
import com.mcmoddev.mmdbot.dashboard.util.BotUserData;

public class RequestBotUserDataPacket implements Packet, HasResponse<RequestBotUserDataPacket.Response> {

    private final BotTypeEnum botType;

    public RequestBotUserDataPacket(final BotTypeEnum botType) {
        this.botType = botType;
    }

    public RequestBotUserDataPacket(final PacketInputBuffer buffer) {
        this(buffer.readEnum(BotTypeEnum.class));
    }

    @Override
    public void handle(final PacketContext context) {
        ServerBridge.executeOnInstance(bridge -> context.reply(new Response(bridge.getBotData(botType))));
    }

    @Override
    public void encode(final PacketOutputBuffer buffer) {
        buffer.writeEnum(botType);
    }

    @Override
    public Class<Response> getResponsePacketClass() {
        return Response.class;
    }

    public record Response(BotUserData data) implements Packet {

        public Response(final PacketInputBuffer buffer) {
            this(buffer.read(BotUserData.class));
        }

        @Override
        public void encode(final PacketOutputBuffer buffer) {
            if (data != null) {
                buffer.write(data);
            }
        }

        @Override
        public void handle(final PacketContext context) {
            // Nothing to handle
        }
    }
}
