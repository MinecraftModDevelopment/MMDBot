package com.mcmoddev.mmdbot.dashboard.common;

public interface Packet {

    void encode(ByteBuffer buffer);

    void handle(PacketReceiver receiver);

}
