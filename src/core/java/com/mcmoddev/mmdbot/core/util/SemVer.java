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
package com.mcmoddev.mmdbot.core.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a semantic version
 *
 * @author Antoine Gagnon
 * @author matyrobbrt
 */
public record SemVer(int major, int minor, @Nullable Integer patch) implements Comparable<SemVer> {

    public static SemVer from(final String versionString) {
        final String[] vs = versionString.split("\\.");
        final var major = Integer.parseInt(vs[0]);
        final var minor = Integer.parseInt(vs[1]);

        Integer patch = null;
        if (vs.length == 3) {
            patch = Integer.parseInt(vs[2]);
        }
        return new SemVer(major, minor, patch);
    }

    @Override
    public int compareTo(@NotNull final SemVer other) {
        if (this.major > other.major) {
            return 1;
        }
        if (this.major < other.major) {
            return -1;
        }


        if (this.minor > other.minor) {
            return 1;
        }
        if (this.minor < other.minor) {
            return -1;
        }

        if (this.patch == null && other.patch != null) {
            return -1;
        }
        if (this.patch != null && other.patch == null) {
            return 1;
        }

        if (patch == null) return 0;

        return this.patch.compareTo(other.patch);
    }

    @Override
    public String toString() {
        if (this.patch == null) {
            return String.format("%d.%d", major, minor);
        } else {
            return String.format("%d.%d.%d", major, minor, patch);
        }
    }
}
