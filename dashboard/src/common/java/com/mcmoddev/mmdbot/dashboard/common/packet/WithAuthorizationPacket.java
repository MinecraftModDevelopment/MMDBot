package com.mcmoddev.mmdbot.dashboard.common.packet;

import com.mcmoddev.mmdbot.dashboard.ServerBridge;

public interface WithAuthorizationPacket extends Packet {

    @Override
    default void handle(PacketContext context) {
        ServerBridge.executeOnInstance(b -> {
            if (b.isUserAuthenticated(context.getSenderAddress())) {
                handle1(context);
            }
        });
    }

    void handle1(PacketContext context);
}
