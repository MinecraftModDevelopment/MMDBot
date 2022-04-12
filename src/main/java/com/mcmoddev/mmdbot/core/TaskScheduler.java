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
package com.mcmoddev.mmdbot.core;

import com.mcmoddev.mmdbot.core.event.Events;
import com.mcmoddev.mmdbot.core.util.TaskScheduler.CollectTasksEvent;
import com.mcmoddev.mmdbot.core.util.TaskScheduler.Task;
import com.mcmoddev.mmdbot.utilities.oldchannels.ChannelMessageChecker;

import java.util.concurrent.TimeUnit;

public final class TaskScheduler {

    /**
     * Init.
     * TODO: move into another bot
     */
    public static void init() {
        Events.MISC_BUS.addListener(-1, (CollectTasksEvent event) -> {
            event.addTask(new Task(new ChannelMessageChecker(), 0, 1, TimeUnit.DAYS));
        });
    }

    public static void scheduleTask(Runnable toRun, long delay, TimeUnit unit) {
        com.mcmoddev.mmdbot.core.util.TaskScheduler.scheduleTask(toRun, delay, unit);
    }
}
