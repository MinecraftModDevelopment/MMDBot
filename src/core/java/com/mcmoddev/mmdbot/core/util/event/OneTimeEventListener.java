/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * Specifically version 2.1 of the License.
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
package com.mcmoddev.mmdbot.core.util.event;

import io.github.matyrobbrt.eventdispatcher.Event;
import io.github.matyrobbrt.eventdispatcher.EventBus;

import java.util.function.Consumer;

/**
 * An event listener which can only be registered once.
 */
public final class OneTimeEventListener<E extends Event> {
    private boolean registered;
    private final int priority;
    private final Consumer<E> eventListener;

    public OneTimeEventListener(final int priority, final Consumer<E> eventListener) {
        this.priority = priority;
        this.eventListener = eventListener;
    }

    public OneTimeEventListener(final Consumer<E> eventListener) {
        this(0, eventListener);
    }

    public void register(EventBus bus) {
        if (registered) return;
        registered = true;
        bus.addListener(priority, eventListener);
    }
}
