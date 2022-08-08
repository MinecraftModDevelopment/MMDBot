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
package com.mcmoddev.mmdbot.commander.updatenotifiers;

import com.mcmoddev.mmdbot.commander.updatenotifiers.fabric.FabricApiUpdateNotifier;
import com.mcmoddev.mmdbot.commander.updatenotifiers.forge.ForgeUpdateNotifier;
import com.mcmoddev.mmdbot.commander.updatenotifiers.minecraft.MinecraftUpdateNotifier;
import com.mcmoddev.mmdbot.commander.updatenotifiers.parchment.ParchmentUpdateNotifier;
import com.mcmoddev.mmdbot.commander.updatenotifiers.quilt.QuiltUpdateNotifier;
import com.mcmoddev.mmdbot.core.event.Events;
import com.mcmoddev.mmdbot.core.util.TaskScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Update checkers for notifiers.
 */
public class UpdateNotifiers {

    public static final Logger LOGGER = LoggerFactory.getLogger(UpdateNotifiers.class);

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
            final var checkingPeriod = 15;
            LOGGER.info("Checking for Minecraft, Forge, Quilt and Fabric updates every {} minutes.", checkingPeriod);
            event.addTask(new MinecraftUpdateNotifier(), 0, checkingPeriod, TimeUnit.MINUTES);
            event.addTask(new ForgeUpdateNotifier(), 0, checkingPeriod, TimeUnit.MINUTES);
            event.addTask(new QuiltUpdateNotifier(), 0, checkingPeriod, TimeUnit.MINUTES);
            event.addTask(new FabricApiUpdateNotifier(), 0, checkingPeriod, TimeUnit.MINUTES);

            LOGGER.info("Checking for Parchment updates every hour.");
            event.addTask(new ParchmentUpdateNotifier(), 0, 1, TimeUnit.HOURS);
        });
    }
}
