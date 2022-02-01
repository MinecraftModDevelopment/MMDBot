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
import com.mcmoddev.mmdbot.modules.logging.misc.ScamDetector;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.oldchannels.ChannelMessageChecker;
import com.mcmoddev.mmdbot.utilities.updatenotifiers.fabric.FabricApiUpdateNotifier;
import com.mcmoddev.mmdbot.utilities.updatenotifiers.forge.ForgeUpdateNotifier;
import com.mcmoddev.mmdbot.utilities.updatenotifiers.minecraft.MinecraftUpdateNotifier;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The type Task scheduler.
 *
 * @author Antoine Gagnon
 */
public final class TaskScheduler {

    /**
     * The constant TIMER.
     */
    private static final ScheduledExecutorService TIMER = Executors.newSingleThreadScheduledExecutor(r ->
        Utils.setThreadDaemon(new Thread(r, "TaskScheduler"), true));

    /**
     * Instantiates a new Task scheduler.
     */
    private TaskScheduler() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Init.
     */
    public static void init() {
        try {
            TIMER.scheduleAtFixedRate(new ForgeUpdateNotifier(), 0, 15, TimeUnit.MINUTES);
        } catch (Exception ex) {
            MMDBot.LOGGER.error("Unable to schedule job Forge Update Notifier", ex);
            ex.printStackTrace();
        }
        TIMER.scheduleAtFixedRate(new MinecraftUpdateNotifier(), 0, 15, TimeUnit.MINUTES);
        TIMER.scheduleAtFixedRate(new FabricApiUpdateNotifier(), 0, 15, TimeUnit.MINUTES);
        TIMER.scheduleAtFixedRate(new ChannelMessageChecker(), 0, 1, TimeUnit.DAYS);
        TIMER.scheduleAtFixedRate(() -> {
            if (ScamDetector.setupScamLinks()) {
                MMDBot.LOGGER.info("Successfully refreshed scam links");
            } else {
                MMDBot.LOGGER.warn("Scam links could not be automatically refreshed");
            }
        }, 0, 14, TimeUnit.DAYS);
    }

    public static void scheduleTask(Runnable toRun, long delay, TimeUnit unit) {
        TIMER.schedule(toRun, delay, unit);
    }
}
