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
 * The type Minecraft forge version.
 *
 * @author Antoine Gagnon
 */
public final class MinecraftForgeVersion {

    /**
     * The Forge version.
     */
    private final ForgeVersion forgeVersion;
    /**
     * The Mc version.
     */
    private final String mcVersion;

    /**
     * Instantiates a new Minecraft forge version.
     *
     * @param mcVersionIn    the mc version in
     * @param forgeVersionIn the forge version in
     */
    public MinecraftForgeVersion(final String mcVersionIn, final ForgeVersion forgeVersionIn) {
        this.mcVersion = mcVersionIn;
        this.forgeVersion = forgeVersionIn;
    }

    /**
     * Gets forge version.
     *
     * @return ForgeVersion. forge version
     */
    public ForgeVersion getForgeVersion() {
        return forgeVersion;
    }

    /**
     * Gets mc version.
     *
     * @return String. mc version
     */
    public String getMcVersion() {
        return mcVersion;
    }
}
