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
