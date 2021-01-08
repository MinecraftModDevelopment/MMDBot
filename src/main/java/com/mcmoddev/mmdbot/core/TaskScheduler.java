package com.mcmoddev.mmdbot.core;

import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.oldchannels.ChannelMessageChecker;
import com.mcmoddev.mmdbot.updatenotifiers.fabric.FabricApiUpdateNotifier;
import com.mcmoddev.mmdbot.updatenotifiers.forge.ForgeUpdateNotifier;
import com.mcmoddev.mmdbot.updatenotifiers.minecraft.MinecraftUpdateNotifier;

import java.util.Timer;

/**
 *
 * @author
 *
 */
public final class TaskScheduler {

    /**
    *
    */
   private static final Timer TIMER = new Timer();

    /**
     *
     */
    private TaskScheduler() {
        throw new IllegalStateException("Utility class");
    }

    /**
     *
     */
    public static void init() {
        //Check each every 3 hours. 1000 * 60 * 60 * 3
        //Check every 15 min. 15 * 60 * 1000
        try {
            TIMER.scheduleAtFixedRate(new ForgeUpdateNotifier(), 0, 15 * 60 * 1000);
        } catch (Exception ex) {
            MMDBot.LOGGER.error("Unable to schedule job Forge Update Notifier", ex);
            ex.printStackTrace();
        }
        TIMER.scheduleAtFixedRate(new MinecraftUpdateNotifier(), 0, 15 * 60 * 1000);
        TIMER.scheduleAtFixedRate(new FabricApiUpdateNotifier(), 0, 15 * 60 * 1000);
        TIMER.scheduleAtFixedRate(new ChannelMessageChecker(), 0, 1000 * 60 * 60 * 24);
    }
}
