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
package com.mcmoddev.mmdbot.thelistener.util;

import com.mcmoddev.mmdbot.thelistener.TheListener;
import discord4j.core.event.domain.Event;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public record ThreadedEventListener(EventListener listener, Executor threadPool) implements EventListener {

    public ThreadedEventListener(final EventListener listener) {
        this(listener, Executors.newSingleThreadExecutor(r -> Utils.setThreadDaemon(new Thread(r), true)));
    }

    @Override
    public void onEvent(final Event event) {
        if (listener != null) {
            threadPool.execute(() -> {
                try {
                    listener.onEvent(event);
                } catch (Exception e) {
                    TheListener.LOGGER.error("Error while executing threaded event!", e);
                }
            });
        }
    }

}
