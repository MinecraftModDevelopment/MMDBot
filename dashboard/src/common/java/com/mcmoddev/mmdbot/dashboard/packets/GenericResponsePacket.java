package com.mcmoddev.mmdbot.dashboard.packets;

import com.mcmoddev.mmdbot.dashboard.common.packet.Packet;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketContext;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketID;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketInputBuffer;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketOutputBuffer;
import com.mcmoddev.mmdbot.dashboard.util.GenericResponse;

public record GenericResponsePacket(PacketID originalPacketID, GenericResponse response) implements Packet {

    public GenericResponsePacket(PacketInputBuffer buffer) {
        this(buffer.readPacketID(), buffer.read(GenericResponse.DECODER));
    }

    @Override
    public void encode(final PacketOutputBuffer buffer) {
        buffer.write(originalPacketID);
        buffer.write(response);
    }

    @Override
    public void handle(final PacketContext context) {
        // Nothing to handle. Packet is supposed to be awaited
    }
}
