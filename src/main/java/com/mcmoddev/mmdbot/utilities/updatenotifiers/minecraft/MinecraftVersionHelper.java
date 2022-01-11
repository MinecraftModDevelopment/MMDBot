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
package com.mcmoddev.mmdbot.utilities.updatenotifiers.minecraft;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mcmoddev.mmdbot.MMDBot;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * The type Minecraft version helper.
 *
 * @author
 */
public final class MinecraftVersionHelper {

    /**
     * The constant API_URL.
     */
    private static final String API_URL = "https://meta.fabricmc.net/v2/versions/game";
    //private static final Duration timeUntilOutdated = Duration.ofMinutes(20);

    /**
     * The constant latest.
     */
    private static String latest;

    /**
     * The constant latestStable.
     */
    private static String latestStable;
    //private static Instant lastUpdated;

    static {
        update();
    }

    /**
     * Instantiates a new Minecraft version helper.
     */
    private MinecraftVersionHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Gets latest.
     *
     * @return String. latest
     */
    public static String getLatest() {
        if (latest == null) {
            update();
        }
        return latest;
    }

    /**
     * Gets latest stable.
     *
     * @return String. latest stable
     */
    public static String getLatestStable() {
        if (latestStable == null) {
            update();
        }
        return latestStable;
    }

    /**
     * Update.
     */
    public static void update() {
        final InputStreamReader reader = openUrl();
        if (reader == null) {
            return;
        }
        final TypeToken<List<MinecraftVersionInfo>> token = new TypeToken<>() {
        };
        final List<MinecraftVersionInfo> versions = new Gson().fromJson(reader, token.getType());

        latest = versions.get(0).version;
        latestStable = versions.stream().filter(it -> it.stable).findFirst().map(it -> it.version).orElse(latest);
        //lastUpdated = Instant.now();
    }

    /**
     * Open url input stream reader.
     *
     * @return InputStreamReader. input stream reader
     */
    private static InputStreamReader openUrl() {
        try {
            final var url = new URL(API_URL);
            return new InputStreamReader(url.openStream(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            MMDBot.LOGGER.error("Failed to get minecraft version", ex);
            return null;
        }
    }

    /**
     * The type Minecraft version info.
     */
    private static class MinecraftVersionInfo {

        /**
         * The Version.
         */
        public String version;

        /**
         * The Stable.
         */
        public boolean stable;
    }
}
