package com.mcmoddev.mmdbot.dashboard.common.util;

import com.google.common.base.Suppliers;

import java.util.function.Supplier;

public class LazyLoadedValue<T> implements Supplier<T> {
    private final Supplier<T> factory;

    public LazyLoadedValue(Supplier<T> factory) {
        this.factory = Suppliers.memoize(factory::get);
    }

    @Override
    public T get() {
        return this.factory.get();
    }
}
