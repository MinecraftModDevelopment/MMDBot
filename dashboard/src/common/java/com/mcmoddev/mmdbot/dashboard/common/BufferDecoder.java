package com.mcmoddev.mmdbot.dashboard.common;

import com.mcmoddev.mmdbot.dashboard.common.packet.PacketInputBuffer;

@FunctionalInterface
public interface BufferDecoder<T> {

    T decode(PacketInputBuffer buffer);

}
