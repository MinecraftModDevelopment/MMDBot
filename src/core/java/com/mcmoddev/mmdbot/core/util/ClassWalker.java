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

import javax.annotation.Nonnull;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

public final class ClassWalker implements Iterable<Class<?>> {

    private final Class<?> clazz;
    private final Class<?> end;

    private ClassWalker(Class<?> clazz) {
        this(clazz, Object.class);
    }

    private ClassWalker(Class<?> clazz, Class<?> end) {
        this.clazz = clazz;
        this.end = end;
    }

    public static ClassWalker range(Class<?> start, Class<?> end) {
        return new ClassWalker(start, end);
    }

    public static ClassWalker walk(Class<?> start) {
        return new ClassWalker(start);
    }

    @Nonnull
    @Override
    public Iterator<Class<?>> iterator() {
        return new Iterator<>() {
            private final Set<Class<?>> done = new HashSet<>();
            private final Deque<Class<?>> inProgress = new LinkedList<>();

            {
                inProgress.addLast(clazz);
                done.add(end);
            }

            @Override
            public boolean hasNext() {
                return !inProgress.isEmpty();
            }

            @Override
            public Class<?> next() {
                Class<?> current = inProgress.removeFirst();
                done.add(current);
                for (Class<?> parent : current.getInterfaces()) {
                    if (!done.contains(parent)) {
                        inProgress.addLast(parent);
                    }
                }

                Class<?> parent = current.getSuperclass();
                if (parent != null && !done.contains(parent)) {
                    inProgress.addLast(parent);
                }
                return current;
            }
        };
    }

}
