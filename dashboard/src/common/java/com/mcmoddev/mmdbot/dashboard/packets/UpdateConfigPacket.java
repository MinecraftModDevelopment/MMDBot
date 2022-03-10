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
import com.mcmoddev.mmdbot.dashboard.common.ByteBuffer;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketContext;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketID;
import com.mcmoddev.mmdbot.dashboard.common.packet.WithAuthorizationPacket;
import com.mcmoddev.mmdbot.dashboard.util.DashConfigType;
import com.mcmoddev.mmdbot.dashboard.util.UpdateConfigContext;

public record UpdateConfigPacket(PacketID packetID, BotTypeEnum botType, DashConfigType configType, String configName, String path, Object newValue) implements WithAuthorizationPacket {

    public static UpdateConfigPacket decode(ByteBuffer buffer) {
        final var id = buffer.readPacketID();
        final var botType = buffer.readEnum(BotTypeEnum.class);
        final var cfgType = buffer.readEnum(DashConfigType.class);
        final var name = buffer.readString();
        final var path = buffer.readString();
        final var newValue = cfgType.tryConvert(cfgType.decode(buffer));
        return new UpdateConfigPacket(id, botType, cfgType, name, path, newValue);
    }

    @Override
    public void encode(final ByteBuffer buffer) {
        buffer.write(packetID);
        buffer.writeEnum(botType);
        buffer.writeEnum(configType);
        buffer.writeString(configName);
        buffer.writeString(path);
        configType.encode(buffer, configType.tryConvert(newValue));
    }

    @Override
    public void handle1(final PacketContext context) {
        ServerBridge.executeOnInstance(bridge -> context.replyGeneric(packetID,
            bridge.updateConfig(new UpdateConfigContext(botType, configType, configName, path, newValue), context)));
    }
}
