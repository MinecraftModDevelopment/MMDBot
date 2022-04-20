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
package com.mcmoddev.mmdbot.core.event.moderation;

import io.github.matyrobbrt.eventdispatcher.Event;

public class ModerationEvent implements Event {
    private final long guildId;
    private final long moderatorId;
    private final long targetId;

    public ModerationEvent(final long guildId, final long moderatorId, final long targetId) {
        this.guildId = guildId;
        this.moderatorId = moderatorId;
        this.targetId = targetId;
    }

    public long getModeratorId() {
        return moderatorId;
    }

    public long getTargetId() {
        return targetId;
    }

    public long getGuildId() {
        return guildId;
    }
}
