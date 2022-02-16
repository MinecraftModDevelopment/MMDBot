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
