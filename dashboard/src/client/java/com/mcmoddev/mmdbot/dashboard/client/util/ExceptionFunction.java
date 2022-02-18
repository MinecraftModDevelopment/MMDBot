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
