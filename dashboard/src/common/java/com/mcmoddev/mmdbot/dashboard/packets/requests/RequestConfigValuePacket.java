package com.mcmoddev.mmdbot.dashboard.packets.requests;

import com.mcmoddev.mmdbot.dashboard.BotTypeEnum;
import com.mcmoddev.mmdbot.dashboard.ServerBridge;
import com.mcmoddev.mmdbot.dashboard.common.packet.HasIDPacket;
import com.mcmoddev.mmdbot.dashboard.common.packet.HasResponse;
import com.mcmoddev.mmdbot.dashboard.common.packet.Packet;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketContext;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketID;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketInputBuffer;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketOutputBuffer;
import com.mcmoddev.mmdbot.dashboard.common.packet.WithAuthorizationPacket;
import com.mcmoddev.mmdbot.dashboard.util.DashConfigType;

public record RequestConfigValuePacket(PacketID packetID, BotTypeEnum botType, DashConfigType configType, String configName, String path)
    implements WithAuthorizationPacket, HasResponse<RequestConfigValuePacket.Response> {

    public static RequestConfigValuePacket decode(PacketInputBuffer buffer) {
        final var id = buffer.readPacketID();
        final var botType = buffer.readEnum(BotTypeEnum.class);
        final var cfgType = buffer.readEnum(DashConfigType.class);
        final var name = buffer.readString();
        final var path = buffer.readString();
        return new RequestConfigValuePacket(id, botType, cfgType, name, path);
    }

    @Override
    public void encode(final PacketOutputBuffer buffer) {
        buffer.write(packetID);
        buffer.writeEnum(botType);
        buffer.writeEnum(configType);
        buffer.writeString(configName);
        buffer.writeString(path);
    }

    @Override
    public void handle1(final PacketContext context) {
        ServerBridge.executeOnInstance(bridge -> context.reply(new Response(packetID(), configType(),
            bridge.getConfigValue(botType(), configName(), path()))));
    }

    @Override
    public Class<Response> getResponsePacketClass() {
        return Response.class;
    }

    public record Response(PacketID packetID, DashConfigType configType, Object value) implements Packet, HasIDPacket {

        public static Response decode(PacketInputBuffer buffer) {
            final var id = buffer.readPacketID();
            final var cfgType = buffer.readEnum(DashConfigType.class);
            final var value = cfgType.tryConvert(cfgType.decode(buffer));
            return new Response(id, cfgType, value);
        }

        @Override
        public PacketID getPacketID() {
            return packetID();
        }

        @Override
        public void handle(final PacketContext context) {
            // Await-only
        }

        @Override
        public void encode(final PacketOutputBuffer buffer) {
            buffer.write(packetID);
            buffer.writeEnum(configType);
            configType().encode(buffer, configType.tryConvert(value));
        }
    }
}
