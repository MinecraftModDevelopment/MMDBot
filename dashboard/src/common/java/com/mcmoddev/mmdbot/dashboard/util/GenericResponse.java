package com.mcmoddev.mmdbot.dashboard.util;

import com.mcmoddev.mmdbot.dashboard.common.BufferDecoder;
import com.mcmoddev.mmdbot.dashboard.common.BufferSerializable;
import com.mcmoddev.mmdbot.dashboard.common.BufferSerializers;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketOutputBuffer;

public record GenericResponse(Type type, String message) implements BufferSerializable {

    public static final BufferDecoder<GenericResponse> DECODER;

    static {
        DECODER = BufferSerializers.registerDecoder(GenericResponse.class, buffer -> {
           final var type = buffer.readEnum(Type.class);
           final var message = buffer.readString();
           return new GenericResponse(type, message);
        });
    }

    @Override
    public void encode(final PacketOutputBuffer buffer) {
        buffer.writeEnum(type);
        buffer.writeString(message);
    }

    @Override
    public String toString() {
        if (type == Type.SUCCESS) {
            return type.toString();
        }
        return "%s: %s".formatted(type.toString(), message);
    }

    public enum Type {
        INVALID_REQUEST,
        TIMED_OUT,
        SUCCESS;

        public GenericResponse create(String message) {
            return new GenericResponse(this, message);
        }

        public GenericResponse createF(String message, Object... args) {
            return create(message.formatted(args));
        }

        public GenericResponse noMessage() {
            return create("");
        }
    }
}
