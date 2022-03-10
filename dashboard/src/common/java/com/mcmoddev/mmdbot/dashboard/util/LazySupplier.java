package com.mcmoddev.mmdbot.dashboard.util;

import java.util.function.Supplier;

public final class LazySupplier<T> implements Supplier<T> {

    private final Supplier<T> supplier;
    private T value = null;

    private LazySupplier(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public static <T> LazySupplier<T> of(Supplier<T> supplier) {
        return new LazySupplier<>(supplier);
    }

    @Override
    public T get() {
        if (value == null) {
            value = supplier.get();
        }
        return value;
    }

}
