package com.mcmoddev.mmdbot.core.dfu;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import lombok.experimental.Delegate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("ClassCanBeRecord")
final class ExtendedDynamicOpsImpl<T> implements ExtendedDynamicOps<T> {
    @Delegate
    private final DynamicOps<T> delegate;

    ExtendedDynamicOpsImpl(final DynamicOps<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public DataResult<OpsMap<T>> getOpsMap(final T input) {
        return getMap(input).map(l -> OpsMap.from(l, this));
    }

    static final class OpsMapImpl<T> implements OpsMap<T> {
        @Delegate
        private final Map<T, T> delegate;
        private final DynamicOps<T> ops;

        OpsMapImpl(final Map<T, T> delegate, final DynamicOps<T> ops) {
            this.delegate = delegate;
            this.ops = ops;
        }

        @Override
        public OpsMap<T> put(final String key, final T value) {
            delegate.put(ops.createString(key), value);
            return this;
        }

        @Override
        public T get(final String key) {
            return delegate.get(ops.createString(key));
        }

        @Override
        public DataResult<String> getAsString(final String key) {
            return ops.getStringValue(get(key));
        }

        @Override
        public DataResult<Number> getAsNumber(final String key) {
            return ops.getNumberValue(get(key));
        }

        @Override
        public <A> DataResult<List<? extends A>> getAsList(final String key, final Function<? super T, ? extends A> mapper) {
            final var k = ops.createString(key);
            if (delegate.containsKey(k)) {
                final var list = new ArrayList<A>();
                return ops.getList(delegate.get(k)).map(cons -> {
                    cons.accept(t -> list.add(mapper.apply(t)));
                    return list;
                });
            }
            return DataResult.error("Key doesn't exist", new ArrayList<>());
        }
    }
}
