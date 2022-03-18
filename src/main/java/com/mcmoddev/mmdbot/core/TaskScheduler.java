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
package com.mcmoddev.mmdbot.core;

import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.event.Events;
import com.mcmoddev.mmdbot.core.util.TaskScheduler.CollectTasksEvent;
import com.mcmoddev.mmdbot.core.util.TaskScheduler.Task;
import com.mcmoddev.mmdbot.modules.logging.misc.ScamDetector;
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
            event.addTask(new Task(() -> {
                if (ScamDetector.setupScamLinks()) {
                    MMDBot.LOGGER.error("Successfully refreshed scam links");
                } else {
                    MMDBot.LOGGER.error("Scam links could not be automatically refreshed");
                }
            }, 0, 14, TimeUnit.DAYS));
        });
    }

    public static void scheduleTask(Runnable toRun, long delay, TimeUnit unit) {
        com.mcmoddev.mmdbot.core.util.TaskScheduler.scheduleTask(toRun, delay, unit);
    }
}
