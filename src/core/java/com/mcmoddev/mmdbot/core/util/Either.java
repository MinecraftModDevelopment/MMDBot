package com.mcmoddev.mmdbot.core.util;

import lombok.NonNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNullableByDefault;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@ParametersAreNullableByDefault
@SuppressWarnings({"unused", "ClassCanBeRecord"})
public final class Either<L, R> {

    public static <L, R> Either<L, R> left(L value) {
        return new Either<>(value, null, Side.LEFT, true);
    }

    public static <L, R> Either<L, R> right(R value) {
        return new Either<>(null, value, Side.RIGHT, true);
    }

    public static <L, R> Either<L, R> both(L left, R right) {
        return new Either<>(left, right, Side.BOTH, true);
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
        return ofNullable(left);
    }

    @Nullable
    public R right() {
        if (!isRight() && throwOnInvalidAccess) {
            throw SideException.invalidAccess(Side.RIGHT);
        }
        return right;
    }

    public Optional<R> rightOptional() {
        return ofNullable(right);
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
