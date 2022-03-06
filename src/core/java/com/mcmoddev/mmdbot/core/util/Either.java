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

import lombok.NonNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNullableByDefault;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Optional.ofNullable;

// TODO documentation
@ParametersAreNullableByDefault
@SuppressWarnings({"unused", "ClassCanBeRecord"})
public final class Either<L, R> {

    public static <L, R> Either<L, R> left(@NonNull L value) {
        return new Either<>(value, null, Side.LEFT, true);
    }

    public static <L, R> Either<L, R> right(@NonNull R value) {
        return new Either<>(null, value, Side.RIGHT, true);
    }

    public static <L, R> Either<L, R> both(@NonNull L left, @NonNull R right) {
        return new Either<>(left, right, Side.BOTH, true);
    }

    public static <L, R> Either<L, R> neither() {
        return new Either<>(null, null, Side.NEITHER, true);
    }

    public static <L, R> Either<L, R> empty() {
        return neither();
    }

    /**
     * Creates an {@link Either} based on the nullable parameters provided, with the {@link Either#side} being different
     * depending on which values are null.
     * <ul>
     *     <li>if both {@code left} and {@code right} are {@code null}, the side is {@link Side#NEITHER} (empty)</li>
     *     <li>if neither {@code left} nor {@code right} are {@code null}, the side is {@link Side#BOTH}</li>
     *     <li>if {@code right} isn't {@code null}, but {@code left} is, the side is {@link Side#RIGHT}</li>
     *     <li>if {@code left} isn't {@code null}, but {@code right} is, the side is {@link Side#LEFT}</li>
     * </ul>
     *
     * @param left  the left value
     * @param right the right value
     * @param <L>   the type of the left value
     * @param <R>   the type of the right value
     * @return the {@link Either}
     */
    @NonNull
    public static <L, R> Either<L, R> byNullables(L left, R right) {
        if (left != null) {
            if (right == null) {
                return left(left);
            } else {
                return both(left, right);
            }
        } else {
            if (right != null) {
                return right(right);
            }
        }
        return empty();
    }

    @Nullable
    private final L left;
    @Nullable
    private final R right;
    @NonNull
    private final Side side;
    private final boolean throwOnInvalidAccess;

    private Either(L left, R right, @NonNull Side side, final boolean throwOnInvalidAccess) {
        this.left = left;
        this.right = right;
        this.side = side;
        this.throwOnInvalidAccess = throwOnInvalidAccess;
    }

    @Nullable
    public L left() {
        if (!isLeft() && throwOnInvalidAccess) {
            throw SideException.invalidAccess(Side.LEFT);
        }
        return left;
    }

    public Optional<L> leftOptional() {
        return ofNullable(isLeft() ? left : null);
    }

    @Nullable
    public R right() {
        if (!isRight() && throwOnInvalidAccess) {
            throw SideException.invalidAccess(Side.RIGHT);
        }
        return right;
    }

    public Optional<R> rightOptional() {
        return ofNullable(isRight() ? right : null);
    }

    /**
     * Accepts the consumers on the values. The consumers can be {@code null}.
     *
     * @param ifLeft  the consumer to accept on the left value, if present
     * @param ifRight the consumer to accept on the right value, if present
     */
    public void accept(Consumer<? super L> ifLeft, Consumer<? super R> ifRight) {
        if (isLeft() && ifLeft != null) {
            ifLeft.accept(left);
        }
        if (isRight() && ifRight != null) {
            ifRight.accept(right);
        }
    }

    public void acceptLeft(Consumer<? super L> consumer) {
        accept(consumer, null);
    }

    public void acceptRight(Consumer<? super R> consumer) {
        accept(null, consumer);
    }

    @Nullable
    public <T> T map(final Function<? super L, ? extends T> left, Function<? super R, ? extends T> right) {
        if (isLeft() && left != null) {
            return left.apply(this.left);
        }
        if (isRight() && right != null) {
            return right.apply(this.right);
        }
        return null;
    }

    public <T> Either<T, R> mapLeft(@NonNull final Function<? super L, ? extends T> left) {
        return map(t -> left(left.apply(t)), Either::right);
    }

    public <T> Either<L, T> mapRight(@NonNull final Function<? super R, ? extends T> right) {
        if (isBoth()) {
            return byNullables(this.left, right.apply(this.right));
        }
        return map(Either::left, t -> right(right.apply(t)));
    }

    public <C, D> Either<C, D> mapBoth(@NonNull final Function<? super L, ? extends C> leftMapper, @NonNull final Function<? super R, ? extends D> rightMapper) {
        if (isBoth()) {
            return both(leftMapper.apply(left), rightMapper.apply(right));
        }
        if (isLeft()) {
            return left(leftMapper.apply(left));
        }
        if (isRight()) {
            return right(rightMapper.apply(right));
        }
        return empty();
    }

    public <C, D> Either<C, D> flatMap(final Function<? super L, Either<C, D>> ifLeft, final Function<? super R, Either<C, D>> ifRight) {
        if (isLeft() && ifLeft != null) {
            return ifLeft.apply(left);
        }
        if (isRight() && ifRight != null) {
            return ifRight.apply(right);
        }
        return empty();
    }

    public Pair<L, R> toPairLR() {
        return Pair.of(left(), right());
    }

    public Pair<R, L> toPairRL() {
        return Pair.of(right(), left());
    }

    public boolean isLeft() {
        return side.isLeft();
    }

    public boolean isRight() {
        return side.isRight();
    }

    public boolean isNeither() {
        return side == Side.NEITHER;
    }

    public boolean isEmpty() {
        return isNeither();
    }

    public boolean isBoth() {
        return side == Side.BOTH;
    }

    public enum Side {
        LEFT, RIGHT,

        NEITHER, BOTH;

        public boolean isLeft() {
            return this == LEFT || this == BOTH;
        }

        public boolean isRight() {
            return this == RIGHT || this == BOTH;
        }
    }

    private static final class SideException extends RuntimeException {
        private SideException(String message) {
            super(message);
        }

        private static SideException invalidAccess(Side side) {
            return new SideException("Invalid access on side: " + side);
        }
    }
}
