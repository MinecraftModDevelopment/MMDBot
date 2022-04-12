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

/**
 * The type Forge version.
 *
 * @author Antoine Gagnon
 */
public final class ForgeVersion {

    /**
     * The Recommended.
     */
    private String recommended;

    /**
     * The Latest.
     */
    private String latest;

    /**
     * Instantiates a new Forge version.
     */
    public ForgeVersion() {
        this.recommended = "(unspecified)";
        this.latest = "(unspecified)";
    }

    /**
     * Instantiates a new Forge version.
     *
     * @param recommendedIn the recommended in
     * @param latestIn      the latest in
     */
    public ForgeVersion(final String recommendedIn, final String latestIn) {
        this.recommended = recommendedIn;
        this.latest = latestIn;
    }

    /**
     * Gets recommended.
     *
     * @return String. recommended
     */
    public String getRecommended() {
        return recommended;
    }

    /**
     * Sets recommended.
     *
     * @param recommendedIn the recommended in
     */
    public void setRecommended(final String recommendedIn) {
        this.recommended = recommendedIn;
    }

    /**
     * Gets latest.
     *
     * @return String. latest
     */
    public String getLatest() {
        return latest;
    }

    /**
     * Sets latest.
     *
     * @param latestIn the latest in
     */
    public void setLatest(final String latestIn) {
        this.latest = latestIn;
    }
}
