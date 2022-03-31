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
package com.mcmoddev.mmdbot.core.commands.component;

import java.util.List;
import java.util.UUID;

/**
 * A component has an ID, and represents an {@link net.dv8tion.jda.api.interactions.components.ItemComponent}.<br>
 * It is used for saving the arguments that an {@link net.dv8tion.jda.api.interactions.components.ItemComponent} needs for later use.
 */
public record Component(String featureId, UUID uuid, List<String> arguments, Lifespan lifespan) {

    /**
     * Button IDs will be split on this string, and only the first one will be used as the component ID,
     * in order to allow other arguments in the button itself, or to allow multiple buttons
     * with the same component ID.
     */
    public static final String ID_SPLITTER = "//";

    /**
     * Creates a button ID with the specified {@code id} as the component ID, and
     * the other arguments being split from the component ID using the {@link #ID_SPLITTER}.
     *
     * @param id        the component id
     * @param arguments other arguments
     * @return a composed ID, with the arguments being split from the component ID using the {@link #ID_SPLITTER}
     */
    public static String createIdWithArguments(final String id, final Object... arguments) {
        StringBuilder actualId = new StringBuilder(id);
        for (final var arg : arguments) {
            actualId.append(ID_SPLITTER).append(arg);
        }
        return actualId.toString();
    }

    /**
     * Creates a component with a {@link Lifespan#TEMPORARY temporary lifespan}.
     *
     * @param featureId the ID of the feature that owns the component
     * @param uuid      the ID of the component
     * @param arguments the arguments used by the component
     */
    public Component(String featureId, UUID uuid, List<String> arguments) {
        this(featureId, uuid, arguments, Lifespan.TEMPORARY);
    }

    /**
     * An enum representing the lifespan of a component. <br>
     */
    public enum Lifespan {
        /**
         * The component will not be deleted on a regular schedule.
         */
        PERMANENT,
        /**
         * The component will be deleted on a regular schedule.
         */
        TEMPORARY
    }
}
