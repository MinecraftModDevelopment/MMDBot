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
package com.mcmoddev.mmdbot.core.util;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.matyrobbrt.curseforgeapi.util.gson.RecordTypeAdapterFactory;
import org.spongepowered.configurate.reference.WatchServiceListener;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * A class holding common constants
 */
public final class Constants {

    public static final Joiner LINE_JOINER = Joiner.on(System.lineSeparator());

    public static final WatchServiceListener CONFIG_WATCH_SERVICE = io.github.matyrobbrt.curseforgeapi.util.Utils.rethrowSupplier(() -> WatchServiceListener
        .builder()
        .threadFactory(r -> Utils.setThreadDaemon(new Thread(r, "ConfigListener"), true))
        .build()).get();

    public static final ScheduledExecutorService TIMER = Executors.newSingleThreadScheduledExecutor(r -> Utils.setThreadDaemon(new Thread(r, "Timer"), true));

    public static final class Gsons {
        public static final Gson NO_PRETTY_PRINTING = new GsonBuilder()
            .registerTypeAdapterFactory(new RecordTypeAdapterFactory())
            .disableHtmlEscaping()
            .setLenient()
            .create();
        public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapterFactory(new RecordTypeAdapterFactory())
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    }
}
