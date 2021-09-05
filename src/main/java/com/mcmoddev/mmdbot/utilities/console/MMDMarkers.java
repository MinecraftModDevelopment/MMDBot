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
package com.mcmoddev.mmdbot.utilities.console;

import com.mcmoddev.mmdbot.modules.commands.server.moderation.CmdMute;
import com.mcmoddev.mmdbot.modules.commands.server.moderation.CmdUnmute;
import com.mcmoddev.mmdbot.modules.logging.misc.EventReactionAdded;
import com.mcmoddev.mmdbot.utilities.updatenotifiers.fabric.FabricApiUpdateNotifier;
import com.mcmoddev.mmdbot.utilities.updatenotifiers.forge.ForgeUpdateNotifier;
import com.mcmoddev.mmdbot.utilities.updatenotifiers.minecraft.MinecraftUpdateNotifier;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Class for holding the {@link Marker}s used for logging.
 *
 * @author sciwhiz12
 */
public final class MMDMarkers {

    /**
     * The {@link Marker} for the {@link FabricApiUpdateNotifier}.
     */
    public static final Marker NOTIFIER_FABRIC = MarkerFactory.getMarker("Notifier.Fabric");
    /**
     * The {@link Marker} for the {@link ForgeUpdateNotifier}.
     */
    public static final Marker NOTIFIER_FORGE = MarkerFactory.getMarker("Notifier.Forge");
    /**
     * The {@link Marker} for the {@link MinecraftUpdateNotifier}.
     */
    public static final Marker NOTIFIER_MC = MarkerFactory.getMarker("Notifier.MC");

    /**
     * The {@link Marker} for the requests removal system.
     *
     * @see EventReactionAdded
     */
    public static final Marker REQUESTS = MarkerFactory.getMarker("Requests");

    /**
     * The {@link Marker} for different guild-related events, such as role addition/removal or nickname changes.
     */
    public static final Marker EVENTS = MarkerFactory.getMarker("Events");

    /**
     * The {@link Marker} for the banning system.
     *
     * @see CmdBan
     * @see CmdUnban
     */
    public static final Marker BANNING = MarkerFactory.getMarker("Banning");

    /**
     * The {@link Marker} for the kicking system.
     *
     * @see CmdKick
     */
    public static final Marker KICKING = MarkerFactory.getMarker("Kicking");

    /**
     * The {@link Marker} for the muting system.
     *
     * @see CmdMute
     * @see CmdUnmute
     */
    public static final Marker MUTING = MarkerFactory.getMarker("Muting");

    /**
     * Instantiates a new Mmd markers.
     */
    private MMDMarkers() {
        throw new IllegalStateException("Utility class");
    }
}
