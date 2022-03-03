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
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketInputBuffer;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketOutputBuffer;

import java.util.function.BiConsumer;

public enum DashConfigType {
    STRING(String.class, (buffer, o) -> buffer.writeString(castOrException(o, String.class)), PacketInputBuffer::readString, "default.fxml", Ref.DEFAULT_CONTROLLER),
    INTEGER(int.class, (buffer, o) -> buffer.writeInt(castOrException(o, int.class)), PacketInputBuffer::readInt, "default.fxml", Ref.DEFAULT_CONTROLLER);

    private static final class Ref {
        public static final String DEFAULT_CONTROLLER = "com.mcmoddev.mmdbot.dashboard.client.controller.config.DefaultConfigBoxController";
    }

    private final BiConsumer<PacketOutputBuffer, Object> encoder;
    private final BufferDecoder<Object> decoder;
    private final String configBoxName;
    private final Class<?> targetType;
    private final String controllerClassName;

    DashConfigType(final Class<?> targetType, final BiConsumer<PacketOutputBuffer, Object> encoder, final BufferDecoder<Object> decoder, final String configBoxName, final String controllerClassName) {
        this.targetType = targetType;
        this.encoder = encoder;
        this.decoder = decoder;
        this.configBoxName = configBoxName;
        this.controllerClassName = controllerClassName;
    }

    public void encode(PacketOutputBuffer buffer, Object toEncode) {
        encoder.accept(buffer, toEncode);
    }

    public Object decode(PacketInputBuffer buffer) {
        return decoder.decode(buffer);
    }

    public Object tryConvert(Object other) {
        if (targetType == String.class) {
            return other.toString();
        }
        try {
            return targetType.cast(other);
        } catch (ClassCastException e) {
            return other;
        }
    }

    private static <T> T castOrException(Object toCast, Class<T> targetType) {
        try {
            return targetType.cast(toCast);
        } catch (ClassCastException e) {
            throw new RuntimeException("Expected %s but found %s".formatted(targetType, toCast));
        }
    }

    public String getConfigBoxName() {
        return configBoxName;
    }

    public String getControllerClassName() {
        return controllerClassName;
    }
}
