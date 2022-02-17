package com.mcmoddev.mmdbot.dashboard.common;

import com.mcmoddev.mmdbot.dashboard.common.packet.PacketOutputBuffer;

public interface BufferSerializable {

    void encode(PacketOutputBuffer buffer);

}
