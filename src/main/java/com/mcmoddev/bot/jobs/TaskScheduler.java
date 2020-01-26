package com.mcmoddev.bot.jobs;

import com.mcmoddev.bot.MMDBot;

import java.util.Timer;

public class TaskScheduler {
    public static Timer timer = new Timer();
    public static void init() {

        try {
            // Check every 12 hours
            timer.scheduleAtFixedRate(new ForgeUpdateNotifier(), 0, 1000 * 60 * 60 * 12);
        } catch (Exception e) {
            MMDBot.LOGGER.error("Unable to schedule job Forge Update Notifier", e);
            e.printStackTrace();
        }
    }
}
