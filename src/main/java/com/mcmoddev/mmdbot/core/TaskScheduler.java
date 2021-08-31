/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2021 <MMD - MinecraftModDevelopment>
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
import com.mcmoddev.mmdbot.utilities.oldchannels.ChannelMessageChecker;
import com.mcmoddev.mmdbot.utilities.updatenotifiers.fabric.FabricApiUpdateNotifier;
import com.mcmoddev.mmdbot.utilities.updatenotifiers.forge.ForgeUpdateNotifier;
import com.mcmoddev.mmdbot.utilities.updatenotifiers.minecraft.MinecraftUpdateNotifier;

import java.util.Timer;

/**
 * The type Task scheduler.
 *
 * @author Antoine Gagnon
 */
public final class TaskScheduler {

    /**
     * The constant TIMER.
     */
    private static final Timer TIMER = new Timer();

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
        //Check each every 3 hours. 1000 * 60 * 60 * 3
        //Check every 15 min. 15 * 60 * 1000
        final long fifteenMinutes = 15 * 60 * 1000L;
        try {
            TIMER.scheduleAtFixedRate(new ForgeUpdateNotifier(), 0, fifteenMinutes);
        } catch (Exception ex) {
            MMDBot.LOGGER.error("Unable to schedule job Forge Update Notifier", ex);
            ex.printStackTrace();
        }
        TIMER.scheduleAtFixedRate(new MinecraftUpdateNotifier(), 0, fifteenMinutes);
        TIMER.scheduleAtFixedRate(new FabricApiUpdateNotifier(), 0, fifteenMinutes);
        TIMER.scheduleAtFixedRate(new ChannelMessageChecker(), 0, 1000 * 60 * 60 * 24);
    }
}
