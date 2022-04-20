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
package com.mcmoddev.mmdbot.commander.updatenotifiers.forge;

import org.jetbrains.annotations.NotNull;

/**
 * The type Sem ver.
 *
 * @author Antoine Gagnon
 */
public final class SemVer implements Comparable<SemVer> {

    /**
     * The Major.
     */
    private final Integer major;

    /**
     * The Minor.
     */
    private final Integer minor;

    /**
     * The Patch.
     */
    private Integer patch;

    /**
     * Instantiates a new Sem ver.
     *
     * @param versionString the version string
     */
    public SemVer(final String versionString) {
        final String[] vs = versionString.split("\\.");
        this.major = Integer.parseInt(vs[0]);
        this.minor = Integer.parseInt(vs[1]);

        if (vs.length == 3) {
            this.patch = Integer.parseInt(vs[2]);
        }
    }

    /**
     * Gets major.
     *
     * @return int. major
     */
    public int getMajor() {
        return major;
    }

    /**
     * Gets minor.
     *
     * @return int. minor
     */
    public int getMinor() {
        return minor;
    }

    /**
     * Gets patch.
     *
     * @return int. patch
     */
    public int getPatch() {
        return patch;
    }

    /**
     * Compare to int.
     *
     * @param other the other
     * @return the int
     */
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

        if (this.patch > other.patch) {
            return 1;
        }
        if (this.patch < other.patch) {
            return -1;
        }

        return 0;
    }

    /**
     * To string string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        if (this.patch == null) {
            return String.format("%d.%d", major, minor);
        } else {
            return String.format("%d.%d.%d", major, minor, patch);
        }
    }
}
