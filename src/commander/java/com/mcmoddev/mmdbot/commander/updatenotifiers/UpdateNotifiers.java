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
package com.mcmoddev.mmdbot.commander.updatenotifiers;

import com.mcmoddev.mmdbot.commander.updatenotifiers.fabric.FabricApiUpdateNotifier;
import com.mcmoddev.mmdbot.commander.updatenotifiers.forge.ForgeUpdateNotifier;
import com.mcmoddev.mmdbot.commander.updatenotifiers.minecraft.MinecraftUpdateNotifier;
import com.mcmoddev.mmdbot.commander.updatenotifiers.quilt.QuiltUpdateNotifier;
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
            event.addTask(new TaskScheduler.Task(new QuiltUpdateNotifier(), 0, 15, TimeUnit.MINUTES));
        });
    }
}
