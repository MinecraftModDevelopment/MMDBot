package com.mcmoddev.mmdbot.dashboard.packets;

import com.mcmoddev.mmdbot.dashboard.BotTypeEnum;
import com.mcmoddev.mmdbot.dashboard.ServerBridge;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketContext;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketID;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketInputBuffer;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketOutputBuffer;
import com.mcmoddev.mmdbot.dashboard.common.packet.WithAuthorizationPacket;
import com.mcmoddev.mmdbot.dashboard.util.DashConfigType;
import com.mcmoddev.mmdbot.dashboard.util.UpdateConfigContext;

public record UpdateConfigPacket(PacketID packetID, BotTypeEnum botType, DashConfigType configType, String configName, String path, Object newValue) implements WithAuthorizationPacket {

    public static UpdateConfigPacket decode(PacketInputBuffer buffer) {
        final var id = buffer.readPacketID();
        final var botType = buffer.readEnum(BotTypeEnum.class);
        final var cfgType = buffer.readEnum(DashConfigType.class);
        final var name = buffer.readString();
        final var path = buffer.readString();
        final var newValue = cfgType.tryConvert(cfgType.decode(buffer));
        return new UpdateConfigPacket(id, botType, cfgType, name, path, newValue);
    }

    @Override
    public void encode(final PacketOutputBuffer buffer) {
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
