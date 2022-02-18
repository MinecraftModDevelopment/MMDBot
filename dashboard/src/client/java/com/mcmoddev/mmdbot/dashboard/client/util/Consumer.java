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
package com.mcmoddev.mmdbot.dashboard.client.util;

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
