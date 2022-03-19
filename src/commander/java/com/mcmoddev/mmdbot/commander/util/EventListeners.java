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
package com.mcmoddev.mmdbot.commander.util;

import com.mcmoddev.mmdbot.core.util.event.ThreadedEventListener;
import com.mcmoddev.mmdbot.core.util.Utils;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.hooks.EventListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@UtilityClass
public class EventListeners {

    public static final ThreadedEventListener MISC_LISTENER;
    public static final ThreadedEventListener COMMANDS_LISTENER;

    static {
        final var mainGroup = new ThreadGroup("The Commander");

        // Misc
        {
            final var group = new ThreadGroup(mainGroup, "Misc Listeners");
            final var poll = (ThreadPoolExecutor) Executors.newFixedThreadPool(2, r ->
                Utils.setThreadDaemon(new Thread(group, r, "MiscListener #%s".formatted(group.activeCount())),
                    true));
            poll.setKeepAliveTime(30, TimeUnit.MINUTES);
            poll.allowCoreThreadTimeOut(true);
            MISC_LISTENER = new ThreadedEventListener(poll);
        }

        // Commands
        {
            final var group = new ThreadGroup(mainGroup, "Command Listeners");
            final var poll = (ThreadPoolExecutor) Executors.newFixedThreadPool(2, r ->
                Utils.setThreadDaemon(new Thread(group, r, "CommandListener #%s".formatted(group.activeCount())),
                    true));
            poll.setKeepAliveTime(30, TimeUnit.MINUTES);
            poll.allowCoreThreadTimeOut(true);
            COMMANDS_LISTENER = new ThreadedEventListener(poll);
        }
    }

    /**
     * Registers all the threaded listeners to the {@code registerer}.
     *
     * @param registerer the consumer that register the listeners
     */
    public static void register(Consumer<EventListener> registerer) {
        registerer.accept(MISC_LISTENER);
        registerer.accept(COMMANDS_LISTENER);
    }

    /**
     * Clears all the listeners from the threaded listeners.
     */
    public static void clear() {
        MISC_LISTENER.clear();
        COMMANDS_LISTENER.clear();
    }
}
