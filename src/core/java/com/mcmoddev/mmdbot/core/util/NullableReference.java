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

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class NullableReference<T> implements Supplier<T> {

    private T value;

    public NullableReference(T initialValue) {
        value = initialValue;
    }

    @Override
    public T get() {
        return value;
    }

    public void set(T newValue) {
        value = newValue;
    }

    public boolean isNull() {
        return value == null;
    }

    public void executeIfNotNull(Consumer<T> consumer) {
        if (!isNull()) {
            consumer.accept(value);
        }
    }
}
