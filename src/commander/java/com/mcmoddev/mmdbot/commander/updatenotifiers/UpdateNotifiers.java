package com.mcmoddev.mmdbot.commander.updatenotifiers;

import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.updatenotifiers.fabric.FabricApiUpdateNotifier;
import com.mcmoddev.mmdbot.commander.updatenotifiers.forge.ForgeUpdateNotifier;
import com.mcmoddev.mmdbot.commander.updatenotifiers.minecraft.MinecraftUpdateNotifier;
import com.mcmoddev.mmdbot.core.event.Events;
import com.mcmoddev.mmdbot.core.util.TaskScheduler;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Update checkers for notifiers.
 */
@Slf4j
public class UpdateNotifiers {

    public static final Logger LOGGER = LoggerFactory.getLogger(UpdateNotifiers.class);
    public static final Marker MARKER = MarkerFactory.getMarker("UpdateNotifiers");

    /**
     * If a listener for the {@link com.mcmoddev.mmdbot.core.util.TaskScheduler.CollectTasksEvent}
     * was registered already.
     */
    private static boolean wasRegistered;

    /**
     * If the listener for the {@link com.mcmoddev.mmdbot.core.util.TaskScheduler.CollectTasksEvent}
     * hasn't previously been registered, registers it to the bus.
     */
    public static void register() {
        if (wasRegistered) {
            return;
        }
        wasRegistered = true;
        Events.MISC_BUS.addListener((TaskScheduler.CollectTasksEvent event) -> {
            try {
                log.error("Debugging: Checking for Forge updates every 15 min...");
                event.addTask(new TaskScheduler.Task(new ForgeUpdateNotifier(), 0, 15, TimeUnit.MINUTES));
            } catch (Exception ex) {
                log.error("Unable to schedule job Forge Update Notifier", ex);
                ex.printStackTrace();
            }
            log.error("Debugging: Checking for Minecraft and Fabric updates every 15 min...");
            event.addTask(new TaskScheduler.Task(new MinecraftUpdateNotifier(), 0, 15, TimeUnit.MINUTES));
            event.addTask(new TaskScheduler.Task(new FabricApiUpdateNotifier(), 0, 15, TimeUnit.MINUTES));
        });
    }
}
