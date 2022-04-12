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
package com.mcmoddev.mmdbot.core.database;

import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import net.dv8tion.jda.api.entities.ISnowflake;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record SnowflakeStorage<T>(Map<Long, T> data) {

    public SnowflakeStorage() {
        this(Collections.synchronizedMap(new HashMap<>()));
    }

    public T get(final long id) {
        synchronized (data) {
            return data.get(id);
        }
    }

    public T get(ISnowflake snowflake) {
        synchronized (data) {
            return get(snowflake.getIdLong());
        }
    }

    public T computeIfAbsent(final long id, Long2ObjectFunction<T> mappingFunction) {
        synchronized (data) {
            return data.computeIfAbsent(id, mappingFunction);
        }
    }

    public T computeIfAbsent(final ISnowflake snowflake, Long2ObjectFunction<T> mappingFunction) {
        return computeIfAbsent(snowflake.getIdLong(), mappingFunction);
    }

    public void forEach(final LongAndObjectBiConsumer<? super T> consumer) {
        synchronized (data) {
            data.forEach(consumer::accept);
        }
    }

    public Optional<T> put(ISnowflake snowflake, T val) {
        synchronized (data) {
            return Optional.ofNullable(data.put(snowflake.getIdLong(), val));
        }
    }

    public Map<Long, T> snapshot() {
        synchronized (data) {
            return new HashMap<>(data);
        }
    }

    @FunctionalInterface
    public interface LongAndObjectBiConsumer<T> {
        void accept(long a, T b);
    }

}
