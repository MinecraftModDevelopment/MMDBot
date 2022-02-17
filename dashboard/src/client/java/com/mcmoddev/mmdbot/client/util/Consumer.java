package com.mcmoddev.mmdbot.client.util;

import lombok.NonNull;

import javax.annotation.Nullable;
import java.util.Objects;

@FunctionalInterface
public interface Consumer<T> extends java.util.function.Consumer<T> {

    static <T> Consumer<T> make(Consumer<T> consumer) {
        return consumer;
    }

    /**
     * Accepts the consumer on multiple objects.
     * @param toAccept the objects to accept the consumer on
     */
    default void acceptOnMultiple(T... toAccept) {
        for (var t : toAccept) {
            accept(t);
        }
    }

    @Override
    default Consumer<T> andThen(@NonNull java.util.function.Consumer<? super T> after) {
        Objects.requireNonNull(after);
        return (@Nullable T t) -> {
            accept(t);
            after.accept(t);
        };
    }
}
