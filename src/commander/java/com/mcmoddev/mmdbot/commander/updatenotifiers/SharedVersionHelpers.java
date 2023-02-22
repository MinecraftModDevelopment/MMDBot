/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2023 <MMD - MinecraftModDevelopment>
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

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Helper methods for the Fabric and Quilt mod loader.
 *
 * @author KiriCattus
 */
public class SharedVersionHelpers {


    public static InputStreamReader getReader(final String urlString) {
        final InputStream stream = getStream(urlString);
        if (stream == null) {
            return null;
        } else {
            return new InputStreamReader(stream, StandardCharsets.UTF_8);
        }
    }

    @Nullable
    public static InputStream getStream(final String urlString) {
        try {
            final var url = new URL(urlString);
            return url.openStream();
        } catch (IOException ex) {
            TheCommander.LOGGER.error("Failed to open input stream", ex);
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
