package com.mcmoddev.mmdbot.client.util;

import java.util.function.Consumer;

@FunctionalInterface
@SuppressWarnings("unchecked")
public interface ExceptionFunction<T, R, E extends Exception> {

    static <T, R, E extends Exception> ExceptionFunction<T, R, E> make(ExceptionFunction<T, R, E> function) {
        return function;
    }

    R apply(T t) throws E;

    default R applyAndCatchException(T t, Consumer<E> onException) {
        try {
            return apply(t);
        } catch (Exception e) {
            try {
                onException.accept((E) e);
            } catch (ClassCastException ignored) {} // This means that the exception is not the required type
        }
        return null;
    }
}
