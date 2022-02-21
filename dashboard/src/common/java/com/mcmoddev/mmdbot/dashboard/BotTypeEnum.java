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
package com.mcmoddev.mmdbot.dashboard;

import javax.annotation.Nullable;

public enum BotTypeEnum {
    THE_COMMANDER("thecommander", "The Commander"),
    THE_LISTENER("thelistener", "The Listener"),
    THE_WATCHER("thewatcher", "The Watcher"),

    /**
     * @deprecated The bot split
     */
    @Deprecated(forRemoval = true)
    MMDBOT("mmdbot", "MMDBot");

    private final String name;
    private final String niceName;

    BotTypeEnum(final String name, final String niceName) {
        this.name = name;
        this.niceName = niceName;
    }

    public String getName() {
        return name;
    }

    public String getNiceName() {
        return niceName;
    }

    @Nullable
    public static BotTypeEnum byName(String name) {
        for (var type : values()) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return null;
    }
}
