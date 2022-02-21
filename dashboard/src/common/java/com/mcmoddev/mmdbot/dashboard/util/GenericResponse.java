/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 * https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 */
package com.mcmoddev.mmdbot.dashboard.util;

import com.mcmoddev.mmdbot.dashboard.common.BufferDecoder;
import com.mcmoddev.mmdbot.dashboard.common.BufferSerializable;
import com.mcmoddev.mmdbot.dashboard.common.BufferSerializers;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketOutputBuffer;
import lombok.NonNull;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Stream;

public record GenericResponse(Type type, String message) implements BufferSerializable {

    @SuppressWarnings("unchecked")
    public static final Map<Type, GenericResponse> NO_MESSAGE = Map.ofEntries(Stream.of(Type.values())
        .map(t -> new AbstractMap.SimpleImmutableEntry<>(t, t.create(""))).toArray(Map.Entry[]::new));

    public static final GenericResponse SUCCESS = Type.SUCCESS.noMessage();

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
        UNAUTHORIZED,
        TIMED_OUT,
        SUCCESS;

        @NonNull
        public GenericResponse create(@NonNull String message) {
            return new GenericResponse(this, message);
        }

        @NonNull
        public GenericResponse createF(@NonNull String message, Object... args) {
            return create(message.formatted(args));
        }

        /**
         * @return a response with no message.
         */
        @NonNull
        public GenericResponse noMessage() {
            return NO_MESSAGE.get(this);
        }
    }
}
