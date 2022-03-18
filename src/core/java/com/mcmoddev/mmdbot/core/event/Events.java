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
package com.mcmoddev.mmdbot.core.event;

import com.mcmoddev.mmdbot.core.event.moderation.ModerationEvent;
import io.github.matyrobbrt.eventdispatcher.Event;
import io.github.matyrobbrt.eventdispatcher.EventBus;
import lombok.experimental.UtilityClass;

/**
 * Class containing different event buses.
 */
@UtilityClass
public class Events {

    /**
     * The bus on which moderation events will be fired.
     */
    public static final EventBus MODERATION_BUS = EventBus.builder("Moderation")
        .baseEventType(ModerationEvent.class)
        .build();

    /**
     * Bus used for miscellaneous events.
     */
    public static final EventBus MISC_BUS = EventBus.builder("Misc")
        .baseEventType(Event.class)
        .build();
}
