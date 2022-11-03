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
package com.mcmoddev.mmdbot.core.util;

import com.mcmoddev.mmdbot.core.event.Events;
import io.github.matyrobbrt.eventdispatcher.Event;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Class used for scheduling different tasks.
 */
@Slf4j
@UtilityClass
public final class TaskScheduler {

    private static final ScheduledExecutorService TIMER = Executors.newScheduledThreadPool(2, r ->
        Utils.setThreadDaemon(new Thread(r, "TaskScheduler"), true));

    /**
     * Initializes the scheduler, by collecting all the tasks which will be periodically run on it. <br>
     * An {@link CollectTasksEvent} will be fired on the {@link com.mcmoddev.mmdbot.core.event.Events#MISC_BUS}
     * for collecting the tasks.
     */
    public static void init() {
        final var event = new CollectTasksEvent();
        Events.MISC_BUS.post(event);
        event.tasks.forEach(t -> TIMER.scheduleAtFixedRate(t.wrappedCommand(), t.initialDelay(), t.period(), t.unit()));
    }

    /**
     * Schedules a task for execution.
     *
     * @param toRun the task to execute
     * @param delay the time from now to delay execution
     * @param unit  the time unit of the delay parameter
     */
    public static void scheduleTask(Runnable toRun, long delay, TimeUnit unit) {
        TIMER.schedule(toRun, delay, unit);
    }

    /**
     * Event fired on the {@link com.mcmoddev.mmdbot.core.event.Events#MISC_BUS} when {@link #init()} is called,
     * in order to collect the tasks that should be run at a fixed rate.
     */
    public static final class CollectTasksEvent implements Event {
        private final List<Task> tasks = new ArrayList<>();

        private CollectTasksEvent() {

        }

        /**
         * Registers a task that should be run at a fixed rate.
         *
         * @param task the task to register
         */
        public void addTask(Task task) {
            tasks.add(task);
        }

        /**
         * Registers a task that should be run at a fixed rate.
         */
        public void addTask(Runnable command,
                            long initialDelay,
                            long period,
                            TimeUnit unit) {
            tasks.add(new Task(command, initialDelay, period, unit));
        }

        /**
         * Registers a task that should be run at a fixed rate.
         */
        public void addTask(Runnable command,
                            Date startTime, long period,
                            TimeUnit unit) {
            addTask(command, startTime.getTime() - System.currentTimeMillis(), unit.toMillis(period), TimeUnit.MILLISECONDS);
        }
    }

    public record Task(Runnable command,
                       long initialDelay,
                       long period,
                       TimeUnit unit) {
        public Runnable wrappedCommand() {
            return () -> {
                try {
                    command.run();
                } catch (Exception exception) {
                    log.error("Encountered exception trying to run task '{}' scheduled every {} {}: ", command, period, unit, exception);
                }
            };
        }
    }
}
