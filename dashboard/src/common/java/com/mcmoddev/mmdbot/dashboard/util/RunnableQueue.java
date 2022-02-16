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
package com.mcmoddev.mmdbot.dashboard.util;

import java.util.LinkedList;
import java.util.function.Consumer;

public final class RunnableQueue<T> implements Runnable {

    private final Consumer<T> runner;
    private final LinkedList<T> queue = new LinkedList<>();

    private RunnableQueue(final Consumer<T> runner) {
        this.runner = runner;
    }

    public static <T> RunnableQueue<T> create(Consumer<T> runner) {
        return new RunnableQueue<>(runner);
    }

    public static RunnableQueue<Runnable> createRunnable() {
        return new RunnableQueue<>(Runnable::run);
    }

    @SafeVarargs
    public final RunnableQueue<T> addLast(T... toRun) {
        for (var r : toRun) {
            queue.addLast(r);
        }
        return this;
    }

    public RunnableQueue<T> addFirst(T toRun) {
        queue.addFirst(toRun);
        return this;
    }

    @Override
    public void run() {
        while (!queue.isEmpty()) {
            runner.accept(queue.poll());
        }
    }
}
