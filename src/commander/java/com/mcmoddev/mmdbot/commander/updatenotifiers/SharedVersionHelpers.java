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

import com.mcmoddev.mmdbot.commander.TheCommander;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

/**
 * Helper methods for the Fabric and Quilt mod loader.
 *
 * @author KiriCattus
 */
public class SharedVersionHelpers {

    public static Instant lastUpdatedTime;
    private static final int THIRTY_MINUTES = 30;
    public static final Duration TIME_UNTIL_OUTDATED = Duration.ofMinutes(THIRTY_MINUTES);

    /**
     * Checks if we need to look for new data or not.
     *
     * @return true or false.
     */
    public static boolean isOutdated() {
        return lastUpdatedTime.plus(TIME_UNTIL_OUTDATED).isBefore(Instant.now());
    }



    /**
     * Gets reader.
     *
     * @param urlString the url string
     * @return InputStreamReader. reader
     */
    public static InputStreamReader getReader(final String urlString) {
        final InputStream stream = getStream(urlString);
        if (stream == null) {
            return null;
        } else {
            return new InputStreamReader(stream, StandardCharsets.UTF_8);
        }
    }

    /**
     * Gets stream.
     *
     * @param urlString the url string
     * @return InputStream. stream
     */
    public static InputStream getStream(final String urlString) {
        try {
            final var url = new URL(urlString);
            return url.openStream();
        } catch (IOException ex) {
            TheCommander.LOGGER.error("Failed to get minecraft version", ex);
            return null;
        }
    }

    /**
     * The type Shared Version Info.
     *
     * @author williambl
     * @author KiriCattus
     */
    public static class SharedVersionInfo {

        /**
         * The game version.
         */
        public String gameVersion;

        /**
         * The build.
         */
        public int build;

        /**
         * The version.
         */
        public String version;
    }

    /**
     * The type Loader version info.
     *
     * @author williambl
     * @author KiriCattus
     */
    public static class LoaderVersionInfo {

        /**
         * The build.
         */
        public int build;

        /**
         * The version.
         */
        public String version;
    }
}
