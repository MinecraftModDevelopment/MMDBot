package com.mcmoddev.mmdbot.dashboard.packets;

import com.mcmoddev.mmdbot.dashboard.ServerBridge;
import com.mcmoddev.mmdbot.dashboard.common.packet.Packet;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketContext;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketID;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketInputBuffer;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketOutputBuffer;

public record ShutdownBotPacket(PacketID id, String botName) implements Packet {

    public ShutdownBotPacket(PacketInputBuffer buffer) {
        this(buffer.readPacketID(), buffer.readString());
    }

    @Override
    public void encode(final PacketOutputBuffer buffer) {
        buffer.write(id);
        buffer.writeString(botName);
    }

    @Override
    public void handle(final PacketContext context) {
        ServerBridge.executeOnInstance(bridge -> context.replyGeneric(id, bridge.shutdownBot(context, botName)));
    }
}
