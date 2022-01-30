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
package com.mcmoddev.mmdbot.utilities;

import com.mcmoddev.mmdbot.MMDBot;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;

import javax.annotation.Nonnull;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public record ThreadedEventListener(EventListener listener, Executor threadPool) implements EventListener {

    public ThreadedEventListener(@Nonnull final EventListener listener) {
        this(listener, Executors.newSingleThreadExecutor(r -> Utils.setThreadDaemon(new Thread(r), true)));
    }

    @Override
    public void onEvent(GenericEvent event) {
        try {
            threadPool.execute(() -> listener.onEvent(event));
        } catch (Exception e) {
            MMDBot.LOGGER.error("Error while executing threaded event!", e);
        }
    }

}
