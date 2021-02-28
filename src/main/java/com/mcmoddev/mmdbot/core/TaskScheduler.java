package com.mcmoddev.mmdbot.core;

import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.updatenotifiers.fabric.FabricApiUpdateNotifier;
import com.mcmoddev.mmdbot.updatenotifiers.forge.ForgeUpdateNotifier;
import com.mcmoddev.mmdbot.updatenotifiers.minecraft.MinecraftUpdateNotifier;

import java.util.Timer;

public class TaskScheduler {
    public static Timer timer = new Timer();

    public static void init() {
        //Check each every 3 hours. 1000 * 60 * 60 * 3
        //Check every 15 min. 15 * 60 * 1000
        try {
            timer.scheduleAtFixedRate(new ForgeUpdateNotifier(), 0, 15 * 60 * 1000);
        } catch (Exception e) {
            MMDBot.LOGGER.error("Unable to schedule job Forge Update Notifier", e);
            e.printStackTrace();
        }
        timer.scheduleAtFixedRate(new MinecraftUpdateNotifier(), 0, 15 * 60 * 1000);
        timer.scheduleAtFixedRate(new FabricApiUpdateNotifier(), 0, 15 * 60 * 1000);
    }
}
