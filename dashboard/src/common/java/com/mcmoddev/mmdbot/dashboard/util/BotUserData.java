package com.mcmoddev.mmdbot.dashboard.util;

import com.mcmoddev.mmdbot.dashboard.common.BufferDecoder;
import com.mcmoddev.mmdbot.dashboard.common.BufferSerializable;
import com.mcmoddev.mmdbot.dashboard.common.BufferSerializers;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketOutputBuffer;

public record BotUserData(String username, String discriminator, String avatarUrl) implements BufferSerializable {

    public static final BufferDecoder<BotUserData> DECODER = buffer -> {
        final var username = buffer.readString();
        final var discriminator = buffer.readString();
        final var avatarUrl = buffer.readString();
        return new BotUserData(username, discriminator, avatarUrl);
    };

    @Override
    public void encode(final PacketOutputBuffer buffer) {
        buffer.writeString(username);
        buffer.writeString(discriminator);
        buffer.writeString(avatarUrl);
    }
}
