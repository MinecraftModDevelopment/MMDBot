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
import net.dv8tion.jda.api.entities.ISnowflake;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents the ID of a snowflake entity.
 */
public interface SnowflakeValue extends Predicate<ISnowflake> {

    /**
     * The empty snowflake ID. Has the value of 0.
     */
    SnowflakeValue EMPTY = of(0L);

    /**
     * Resolves this snowflake using the specified {@code function}
     * that will take its ID, as a long value, as the input parameter.
     *
     * @param function the function that will resolve the snowflake
     * @param <T>      the type of the resolved snowflake
     * @return the resolved snowflake
     */
    <T> T resolve(Long2ObjectFunction<? extends T> function);

    /**
     * Resolves this snowflake using the specified {@code function}
     * that will take its ID, as a string value, as the input parameter.
     *
     * @param function the function that will resolve the snowflake
     * @param <T>      the type of the resolved snowflake
     * @return the resolved snowflake
     */
    <T> T resolveAsString(Function<? super String, ? extends T> function);

    /**
     * Gets this snowflake ID as a string.
     *
     * @return the ID as a string
     */
    String asString();

    /**
     * Gets this snowflake ID as a long value.
     *
     * @return the ID as a long value
     */
    long asLong();

    /**
     * Tests the ID of this snowflake against the {@code snowflake}'s ID.
     *
     * @param snowflake the snowflake whose ID to test
     * @return if the ID matches
     */
    @Override
    boolean test(ISnowflake snowflake);

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

            @Override
            public boolean test(final ISnowflake snowflake) {
                return snowflake.getId().equals(id);
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

            @Override
            public boolean test(final ISnowflake snowflake) {
                return snowflake.getIdLong() == id;
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
