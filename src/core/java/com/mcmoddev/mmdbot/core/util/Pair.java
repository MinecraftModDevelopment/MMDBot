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
package com.mcmoddev.mmdbot.core.util;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

@ParametersAreNonnullByDefault
public record Pair<F, S>(F first, S second) {

    /**
     * Makes a pair from the given value
     *
     * @param first  the first value
     * @param second the second value
     * @param <F>    the type of the first value
     * @param <S>    the type of the second value
     * @return the newly created pair
     */
    public static <F, S> Pair<F, S> of(@Nullable F first, @Nullable S second) {
        return new Pair<>(first, second);
    }

    /**
     * Makes an optional pair from the given optionals. <br>
     * The optional is empty if either {@code first} or {@code second}
     * are empty.
     *
     * @param first  an optional containing the first value
     * @param second an optional containing the second value
     * @param <F>    the type of the first value
     * @param <S>    the type of the second value
     * @return an optional containing the newly created pair. (or empty, if the conditions given above are not met)
     */
    public static <F, S> Optional<Pair<F, S>> makeOptional(Optional<F> first, Optional<S> second) {
        return of(first.orElse(null), second.orElse(null)).toOptional();
    }

    /**
     * Accepts the given {@code consumer} on the values from the pair
     *
     * @param consumer the consumer
     */
    public void accept(BiConsumer<F, S> consumer) {
        consumer.accept(first(), second());
    }

    /**
     * Accepts the given {@code consumer} on the first value of the pair.
     *
     * @param consumer the consumer
     */
    public void acceptFirst(Consumer<F> consumer) {
        consumer.accept(first());
    }

    /**
     * Accepts the given {@code consumer} on the second value of the pair.
     *
     * @param consumer the consumer
     */
    public void acceptSecond(Consumer<S> consumer) {
        consumer.accept(second());
    }

    /**
     * Maps the pair to another value
     *
     * @param mapper the mapper
     * @param <T>    the return type
     * @return the mapped value
     */
    public <T> T map(BiFunction<F, S, T> mapper) {
        return mapper.apply(first(), second());
    }

    /**
     * Maps the first value to another value
     *
     * @param mapper      the mapper
     * @param <NEW_FIRST> the new first value type
     * @return the mapped pair
     */
    public <NEW_FIRST> Pair<NEW_FIRST, S> mapFirst(Function<F, NEW_FIRST> mapper) {
        return of(mapper.apply(first()), second());
    }

    /**
     * Maps the second value to another value
     *
     * @param mapper       the mapper
     * @param <NEW_SECOND> the new first value type
     * @return the mapped pair
     */
    public <NEW_SECOND> Pair<F, NEW_SECOND> mapSecond(Function<S, NEW_SECOND> mapper) {
        return of(first(), mapper.apply(second()));
    }

    /**
     * Wraps the pair in an optional.
     * Said optional is empty if either the first or the second
     * value is null.
     *
     * @return the optional
     */
    public Optional<Pair<F, S>> toOptional() {
        if (first() == null || second() == null) {
            return Optional.empty();
        }
        return Optional.of(this);
    }

}
