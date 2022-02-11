package com.mcmoddev.mmdbot.dashboard.common.listener;

import com.mcmoddev.mmdbot.dashboard.common.Packet;

@FunctionalInterface
public interface PacketListener {

    void onPacket(Packet packet);

}
