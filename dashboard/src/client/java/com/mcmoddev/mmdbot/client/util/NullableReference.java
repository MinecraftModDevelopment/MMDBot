package com.mcmoddev.mmdbot.client.util;

import lombok.NonNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class NullableReference<T> implements Supplier<T> {

    private T value;
    private final boolean oneTimeSet;

    public NullableReference(T initialValue, final boolean oneTimeSet) {
        this.value = initialValue;
        this.oneTimeSet = oneTimeSet;
    }

    public NullableReference(boolean oneTimeSet) {
        this(null, oneTimeSet);
    }

    @Override
    public T get() {
        return value;
    }

    public void set(final T value) {
        if (value != null && oneTimeSet) {
            throw new UnsupportedOperationException("Current value is not null and oneTimeSet is true!");
        } else {
            this.value = value;
        }
    }

    public void invokeIfNotNull(@NonNull Consumer<T> consumer) {
        if (value != null) {
            consumer.accept(value);
        }
    }
}
