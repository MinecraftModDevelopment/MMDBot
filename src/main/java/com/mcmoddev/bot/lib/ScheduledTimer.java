package com.mcmoddev.bot.lib;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class ScheduledTimer {

    private final Timer timer;

    public ScheduledTimer () {

        this.timer = new Timer();
    }

    public void scheduleAndRunHourly (int hours, Runnable task) {

        this.scheduleAndRun(TimeUnit.HOURS.toMillis(hours), task);
    }

    public void scheduleAndRun (long delay, Runnable task) {

        this.schedule(delay, task);
        task.run();
    }

    public void schedule (Date date, Runnable task) {

        this.timer.schedule(new RunnableTask(task), date);
    }

    public void schedule (long delay, Runnable task) {

        this.timer.schedule(new RunnableTask(task), delay);
    }

    private static class RunnableTask extends TimerTask {

        private final Runnable task;

        public RunnableTask (Runnable task) {

            this.task = task;
        }

        @Override
        public void run () {

            this.task.run();
        }
    }
}