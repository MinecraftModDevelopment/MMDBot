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

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.util.Objects;

/**
 * Represents a Forge version
 *
 * @author Antoine Gagnon
 * @author matyrobbrt
 * @see MinecraftForgeVersion
 */
public final class ForgeVersion {

    /**
     * The Recommended version
     */
    private String recommended;

    /**
     * The Latest version
     */
    private String latest;

    public ForgeVersion() {
        this.recommended = "(unspecified)";
        this.latest = "(unspecified)";
    }

    /**
     * Gets the recommended version.
     */
    public String getRecommended() {
        return recommended;
    }

    @CanIgnoreReturnValue
    public ForgeVersion setRecommended(final String recommendedIn) {
        this.recommended = recommendedIn;
        return this;
    }

    /**
     * Gets the latest version.
     */
    public String getLatest() {
        return latest;
    }

    @CanIgnoreReturnValue
    public ForgeVersion setLatest(final String latestIn) {
        this.latest = latestIn;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForgeVersion that = (ForgeVersion) o;
        return Objects.equals(recommended, that.recommended) && Objects.equals(latest, that.latest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recommended, latest);
    }

    @Override
    public String toString() {
        return "ForgeVersion[recommended=%s, latest=%s]".formatted(recommended, latest);
    }
}
