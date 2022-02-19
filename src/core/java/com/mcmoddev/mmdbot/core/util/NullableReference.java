package com.mcmoddev.mmdbot.core.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class NullableReference<T> implements Supplier<T> {

    private T value;

    public NullableReference(T initialValue) {
        value = initialValue;
    }

    @Override
    public T get() {
        return value;
    }

    public void set(T newValue) {
        value = newValue;
    }

    public boolean isNull() {
        return value == null;
    }

    public void executeIfNotNull(Consumer<T> consumer) {
        if (!isNull()) {
            consumer.accept(value);
        }
    }
}
