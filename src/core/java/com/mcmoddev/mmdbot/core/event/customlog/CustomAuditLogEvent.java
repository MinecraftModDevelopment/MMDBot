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
package com.mcmoddev.mmdbot.core.event.customlog;

import io.github.matyrobbrt.eventdispatcher.Event;

/**
 * The base class for custom audit log events. <br>
 * These events are fired on the {@link com.mcmoddev.mmdbot.core.event.Events#CUSTOM_AUDIT_LOG_BUS}.
 */
public abstract class CustomAuditLogEvent implements Event {
    protected final long guildId;
    protected final long responsibleUserId;

    protected CustomAuditLogEvent(final long guildId, final long responsibleUserId) {
        this.guildId = guildId;
        this.responsibleUserId = responsibleUserId;
    }

    /**
     * Gets the ID of the guild in which this event happened.
     * @return the ID of the guild in which this event happened
     */
    public long getGuildId() {
        return guildId;
    }

    /**
     * Gets the ID of the user responsible for this event.
     * @return the ID of the user responsible for this event
     */
    public long getResponsibleUserId() {
        return responsibleUserId;
    }

}
