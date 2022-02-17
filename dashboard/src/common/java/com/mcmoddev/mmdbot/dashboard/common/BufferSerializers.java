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
