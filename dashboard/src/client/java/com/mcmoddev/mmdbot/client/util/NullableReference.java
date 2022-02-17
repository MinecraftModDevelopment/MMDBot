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
