package com.mcmoddev.mmdbot.core.util;

import com.mcmoddev.mmdbot.core.event.Events;
import io.github.matyrobbrt.eventdispatcher.Event;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Class used for scheduling different tasks.
 */
@UtilityClass
public final class TaskScheduler {

    private static final ScheduledExecutorService TIMER = Executors.newSingleThreadScheduledExecutor(r ->
        Utils.setThreadDaemon(new Thread(r, "TaskScheduler"), true));

    /**
     * Initializes the scheduler, by collecting all the tasks which will be periodically run on it. <br>
     * An {@link CollectTasksEvent} will be fired on the {@link com.mcmoddev.mmdbot.core.event.Events#MISC_BUS}
     * for collecting the tasks.
     */
    public static void init() {
        final var event = new CollectTasksEvent();
        Events.MISC_BUS.post(event);
        event.tasks.forEach(t -> TIMER.scheduleAtFixedRate(t.command(), t.initialDelay(), t.period(), t.unit()));
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
    }

    public record Task(Runnable command,
                       long initialDelay,
                       long period,
                       TimeUnit unit) {

    }
}
