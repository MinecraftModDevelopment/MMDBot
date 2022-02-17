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
package com.mcmoddev.mmdbot.dashboard.common;

import com.mcmoddev.mmdbot.dashboard.util.GenericResponse;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BufferSerializers {

    private static final Map<Class<?>, BufferDecoder<Object>> DECODERS = Collections.synchronizedMap(new HashMap<>() {
        @Override
        public boolean remove(final Object key, final Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BufferDecoder<Object> remove(final Object key) {
            throw new UnsupportedOperationException();
        }
    });

    public static <T> BufferDecoder<T> registerDecoder(Class<T> clazz, BufferDecoder<T> decoder) {
        DECODERS.put(clazz, decoder::decode);
        return decoder;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> BufferDecoder<T> getDecoder(Class<T> clazz) {
        if (DECODERS.containsKey(clazz)) {
            return b -> (T) DECODERS.get(clazz).decode(b);
        }
        return null;
    }
}
