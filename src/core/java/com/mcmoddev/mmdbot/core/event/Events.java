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
package com.mcmoddev.mmdbot.core.event;

import com.mcmoddev.mmdbot.core.annotation.RegisterEventListener;
import com.mcmoddev.mmdbot.core.event.customlog.CustomAuditLogEvent;
import com.mcmoddev.mmdbot.core.event.moderation.ModerationEvent;
import com.mcmoddev.mmdbot.core.util.ReflectionsUtils;
import io.github.matyrobbrt.eventdispatcher.Event;
import io.github.matyrobbrt.eventdispatcher.EventBus;
import io.github.matyrobbrt.eventdispatcher.reflections.AnnotationProvider;
import io.github.matyrobbrt.eventdispatcher.reflections.AnnotationProvider.AnnotationFilter;
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
        .addAnnotationProvider(forBusProvider(RegisterEventListener.BusType.MODERATION))
        .build();

    /**
     * Bus used for miscellaneous events.
     */
    public static final EventBus MISC_BUS = EventBus.builder("Misc")
        .baseEventType(Event.class)
        .addAnnotationProvider(forBusProvider(RegisterEventListener.BusType.MISCELLANEOUS))
        .build();

    /**
     * Bus used for custom audit log events, like creating / deleting tricks.
     */
    public static final EventBus CUSTOM_AUDIT_LOG_BUS = EventBus.builder("CustomAuditLog")
        .baseEventType(CustomAuditLogEvent.class)
        .walksEventHierarcy(true)
        .addAnnotationProvider(forBusProvider(RegisterEventListener.BusType.CUSTOM_AUDIT_LOG))
        .build();

    private static AnnotationProvider forBusProvider(final RegisterEventListener.BusType bus) {
        return new AnnotationProvider(() -> ReflectionsUtils.REFLECTIONS, new AnnotationFilter<>(RegisterEventListener.class, l -> l.bus() == bus));
    }
}
