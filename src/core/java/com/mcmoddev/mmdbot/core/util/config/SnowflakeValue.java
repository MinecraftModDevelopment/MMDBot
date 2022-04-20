/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * Specifically version 2.1 of the License.
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
package com.mcmoddev.mmdbot.core.util.config;

import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.function.Function;

public interface SnowflakeValue {
    SnowflakeValue EMPTY = of(0L);

    <T> T resolve(Long2ObjectFunction<? extends T> function);
    <T> T resolveAsString(Function<? super String, ? extends T> function);

    String asString();
    long asLong();

    static SnowflakeValue of(String id) {
        return new SnowflakeValue() {
            final long asLong = safeConvert(id);
            @Override
            public <T> T resolve(final Long2ObjectFunction<? extends T> function) {
                return function.apply(asLong);
            }

            @Override
            public <T> T resolveAsString(final Function<? super String, ? extends T> function) {
                return function.apply(id);
            }

            @Override
            public long asLong() {
                return asLong;
            }

            @Override
            public String asString() {
                return id;
            }
        };
    }

    static SnowflakeValue of(long id) {
        return new SnowflakeValue() {
            final String asStr = String.valueOf(id);
            @Override
            public <T> T resolve(final Long2ObjectFunction<? extends T> function) {
                return function.apply(id);
            }

            @Override
            public <T> T resolveAsString(final Function<? super String, ? extends T> function) {
                return function.apply(asStr);
            }

            @Override
            public String asString() {
                return asStr;
            }

            @Override
            public long asLong() {
                return id;
            }
        };
    }

    static long safeConvert(String id) {
        if (id.isBlank()) return 0L;
        try {
            long l = Long.parseLong(id.trim());
            if (l < 0)
                return 0L;
            return l;
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    final class Serializer implements TypeSerializer<SnowflakeValue> {

        @Override
        public SnowflakeValue deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
            final var raw = node.raw();
            if (raw instanceof Number num) {
                return of(num.longValue());
            } else if (raw instanceof String str) {
                return of(str);
            }
            return null;
        }

        @Override
        public void serialize(final Type type, @Nullable final SnowflakeValue obj, final ConfigurationNode node) throws SerializationException {
            if (obj == null) {
                node.raw(null);
                return;
            }
            node.set(obj.asLong());
        }
    }
}
