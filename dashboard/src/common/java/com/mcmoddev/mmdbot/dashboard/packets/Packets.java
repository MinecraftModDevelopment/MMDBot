package com.mcmoddev.mmdbot.dashboard.packets;

import com.mcmoddev.mmdbot.dashboard.common.packet.PacketSet;

public class Packets {

    public static final PacketSet SET = new PacketSet()
        .addPacket(GenericResponsePacket.class)
        .addPacket(CheckAuthorizedPacket.class)
            .addPacket(CheckAuthorizedPacket.Response.class)
        .addPacket(RequestLoadedBotTypesPacket.class)
            .addPacket(RequestLoadedBotTypesPacket.Response.class)
        .addPacket(ShutdownBotPacket.class);

}
